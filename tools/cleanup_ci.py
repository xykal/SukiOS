#!/usr/bin/env python3
"""Pembersih jejak CI untuk repo SukiOS.

Menghapus, setelah hasil run dicatat:
  1. artifact milik run yang ditargetkan
  2. riwayat/log run itu sendiri
  3. entri cache di repo ini (mode 'all' — dipakai setelah rilis; build biasa memakai 'none')

TIDAK pernah menghapus: release, tag, branch, run milik workflow lain,
riwayat run orang lain, atau run yang gagal (kecuali diminta eksplisit) —
sesuai policy cleanup: log kegagalan dibiarkan agar bisa didiagnosis dulu.

Dipakai oleh .github/workflows/cleanup.yml. Bisa dijalankan manual:

    GH_TOKEN=... REPO=owner/repo RUN_ID=123 python3 tools/cleanup_ci.py

Variabel lingkungan:
    GH_TOKEN          token dengan izin actions:write (wajib)
    REPO              owner/repo (wajib)
    RUN_ID            id run yang dibersihkan (wajib)
    CURRENT_RUN_ID    id run pembersih ini (untuk prune riwayatnya sendiri)
    PURGE_MODE        "none" (default, cache dibiarkan) atau "all" (hapus semua entri cache repo)
    KEEP_RUN          "true" untuk hanya menghapus artifact (default false)
    INCLUDE_FAILED    "true" untuk tetap membersihkan run yang gagal (default false)
    KEEP_CLEANUP_RUNS jumlah riwayat run pembersih yang disimpan (default 2)
"""
from __future__ import annotations

import json
import os
import sys
import urllib.error
import urllib.request
from datetime import datetime, timezone

API = "https://api.github.com"
CLEANUP_WORKFLOW_FILE = "cleanup.yml"

TOKEN = os.environ.get("GH_TOKEN") or os.environ.get("GITHUB_TOKEN") or ""
REPO = os.environ.get("REPO") or os.environ.get("GITHUB_REPOSITORY") or ""
RUN_ID = (os.environ.get("RUN_ID") or "").strip()
CURRENT_RUN_ID = (os.environ.get("CURRENT_RUN_ID") or "").strip()
_purge_raw = (os.environ.get("PURGE_MODE") or os.environ.get("PURGE_CACHES") or "none").strip().lower()
# none : cache dibiarkan — dipakai bersama antar-run agar build berikutnya cepat (default build)
# all  : semua entri cache di repo ini dihapus — dipakai setelah rilis, sesuai permintaan kall
#        (aman karena repo ini hanya punya workflow milik kita sendiri)
PURGE_MODE = "all" if _purge_raw in ("all", "true", "1", "yes") else "none"
PURGE_CACHES = PURGE_MODE == "all"  # nama lama, dipertahankan untuk kompatibilitas
KEEP_RUN = (os.environ.get("KEEP_RUN", "false").strip().lower() == "true")
INCLUDE_FAILED = (os.environ.get("INCLUDE_FAILED", "false").strip().lower() == "true")
try:
    KEEP_CLEANUP_RUNS = max(0, int(os.environ.get("KEEP_CLEANUP_RUNS", "2")))
except ValueError:
    KEEP_CLEANUP_RUNS = 2

log_lines: list[str] = []


def log(msg: str) -> None:
    print(msg, flush=True)
    log_lines.append(msg)


def api(method: str, path: str, ok=(200, 201, 204)):
    """Panggilan REST API GitHub. Mengembalikan (status, payload atau None)."""
    url = path if path.startswith("http") else f"{API}{path}"
    request = urllib.request.Request(url, method=method)
    request.add_header("Authorization", f"Bearer {TOKEN}")
    request.add_header("Accept", "application/vnd.github+json")
    request.add_header("X-GitHub-Api-Version", "2022-11-28")
    try:
        with urllib.request.urlopen(request, timeout=30) as response:
            body = response.read()
            if response.status not in ok:
                raise RuntimeError(f"{method} {url} -> {response.status}")
            return response.status, (json.loads(body) if body else None)
    except urllib.error.HTTPError as error:
        return error.code, None
    except urllib.error.URLError as error:
        raise RuntimeError(f"{method} {url} gagal: {error.reason}") from error


def write_summary(lines: list[str]) -> None:
    path = os.environ.get("GITHUB_STEP_SUMMARY")
    if not path:
        return
    try:
        with open(path, "a", encoding="utf-8") as handle:
            handle.write("\n".join(lines) + "\n")
    except OSError as error:
        print(f"(gagal menulis job summary: {error})", file=sys.stderr)


def main() -> int:
    if not TOKEN or not REPO or not RUN_ID.isdigit():
        print("Butuh GH_TOKEN, REPO, dan RUN_ID (angka).", file=sys.stderr)
        return 2

    status, run = api("GET", f"/repos/{REPO}/actions/runs/{RUN_ID}")
    if status != 200 or not run:
        print(f"Run {RUN_ID} tidak ditemukan (HTTP {status}). Tidak ada yang dibersihkan.")
        write_summary([f"### Cleanup dilewati\nRun `{RUN_ID}` tidak ditemukan (HTTP {status})."])
        return 0

    name = run.get("name") or "-"
    conclusion = run.get("conclusion") or run.get("status")
    sha = (run.get("head_sha") or "")[:7]
    branch = run.get("head_branch")
    run_status = run.get("status")

    log("Rekaman sebelum pembersihan:")
    log(f"  run id     : {RUN_ID} (#{run.get('run_number')})")
    log(f"  workflow   : {name}")
    log(f"  kesimpulan : {conclusion}")
    log(f"  commit     : {sha} di {branch}")

    if run_status != "completed":
        log("Run belum selesai. Pembersihan dibatalkan agar tidak memotong proses.")
        write_summary([f"### Cleanup dilewati\nRun `{RUN_ID}` masih berstatus `{run_status}`."])
        return 0

    if conclusion != "success" and not INCLUDE_FAILED:
        log(
            f"Run berstatus '{conclusion}', bukan sukses. Log dibiarkan agar bisa didiagnosis "
            "(jalankan ulang dengan include_failed=true kalau memang ingin dibersihkan)."
        )
        write_summary([
            "### Cleanup dilewati (run tidak sukses)",
            f"Run `{RUN_ID}` berakhir `{conclusion}`; riwayat dan log sengaja dibiarkan untuk diagnosis.",
        ])
        return 0

    deleted_artifacts, deleted_caches = [], []

    # --- 1. artifact ---
    status, data = api("GET", f"/repos/{REPO}/actions/runs/{RUN_ID}/artifacts?per_page=100")
    if status == 200 and data:
        for artifact in data.get("artifacts", []):
            artifact_id = artifact.get("id")
            artifact_name = artifact.get("name")
            size = artifact.get("size_in_bytes", 0)
            dstatus, _ = api("DELETE", f"/repos/{REPO}/actions/artifacts/{artifact_id}")
            if dstatus == 204:
                deleted_artifacts.append(artifact_name)
                log(f"  artifact dihapus: {artifact_name} ({size} byte)")
            else:
                log(f"  GAGAL menghapus artifact {artifact_name} (HTTP {dstatus})")
    else:
        log(f"  tidak ada artifact terbaca (HTTP {status})")

    # --- 2. cache ---
    if PURGE_MODE == "all":
        status, data = api("GET", f"/repos/{REPO}/actions/caches?per_page=100")
        if status == 200 and data:
            entries = data.get("actions_caches", [])
            total_bytes = sum(c.get("size_in_bytes", 0) for c in entries)
            log(f"  cache mode=all: {len(entries)} entri, total {total_bytes / 1048576:.1f} MB")
            for cache in entries:
                cache_id = cache.get("id")
                dstatus, _ = api("DELETE", f"/repos/{REPO}/actions/caches/{cache_id}")
                if dstatus == 204:
                    deleted_caches.append(cache.get("key"))
                    log(f"  cache dihapus: {cache.get('key')} ({cache.get('size_in_bytes', 0)} byte, ref {cache.get('ref')})")
                else:
                    log(f"  GAGAL menghapus cache {cache.get('key')} (HTTP {dstatus})")
            # verifikasi
            vstatus, vdata = api("GET", f"/repos/{REPO}/actions/caches?per_page=100")
            remaining = len((vdata or {}).get("actions_caches", [])) if vstatus == 200 else -1
            log(f"  verifikasi: sisa entri cache = {remaining}")
            if remaining > 0:
                log("  PERINGATAN: masih ada entri cache yang tersisa")
        else:
            log(f"  daftar cache tidak terbaca (HTTP {status})")
    else:
        log("  cache mode=none — dibiarkan agar build berikutnya cepat "
            "(mode 'all' dipakai setelah rilis).")

    # --- 3. riwayat run ---
    run_deleted = False
    if KEEP_RUN:
        log("  riwayat run dipertahankan (keep_run=true)")
    else:
        dstatus, _ = api("DELETE", f"/repos/{REPO}/actions/runs/{RUN_ID}")
        if dstatus == 204:
            verify_status, _ = api("GET", f"/repos/{REPO}/actions/runs/{RUN_ID}")
            if verify_status == 404:
                run_deleted = True
                log("  riwayat run dihapus (verifikasi: GET mengembalikan 404)")
            else:
                log(f"  PERINGATAN: permintaan hapus sukses, tapi verifikasi memberi HTTP {verify_status}")
        else:
            log(f"  GAGAL menghapus riwayat run (HTTP {dstatus})")

    # --- 4. prune riwayat workflow pembersih sendiri (dibatasi) ---
    pruned = []
    if CURRENT_RUN_ID.isdigit() and KEEP_CLEANUP_RUNS >= 0:
        status, data = api(
            "GET",
            f"/repos/{REPO}/actions/workflows/{CLEANUP_WORKFLOW_FILE}/runs?per_page=30",
        )
        if status == 200 and data:
            runs = sorted(data.get("workflow_runs", []), key=lambda r: r.get("id", 0), reverse=True)
            keep_ids = {r.get("id") for r in runs[:KEEP_CLEANUP_RUNS]}
            keep_ids.add(int(CURRENT_RUN_ID))
            for candidate in runs:
                cid = candidate.get("id")
                if cid in keep_ids or cid >= int(CURRENT_RUN_ID):
                    continue
                pstatus, _ = api("DELETE", f"/repos/{REPO}/actions/runs/{cid}")
                if pstatus == 204:
                    pruned.append(cid)
                    log(f"  prune riwayat pembersih lama: run {cid}")

    summary = [
        "### Rekaman pembersihan",
        f"- run `{RUN_ID}` (#{run.get('run_number')}) — `{name}`",
        f"- commit `{sha}` di `{branch}` — kesimpulan **{conclusion}**",
        f"- artifact dihapus: {len(deleted_artifacts)}"
        + (f" ({', '.join(deleted_artifacts)})" if deleted_artifacts else ""),
        f"- cache: mode `{PURGE_MODE}`, entri dihapus: {len(deleted_caches)}"
        + (f" ({', '.join(str(c) for c in deleted_caches)})" if deleted_caches else ""),
        f"- riwayat run dihapus: {'ya' if run_deleted else 'tidak'}",
        f"- riwayat pembersih lama dipangkas: {len(pruned)}",
        "",
        "URL run yang dihapus tidak bisa dibuka lagi. Angka di atas adalah rekaman yang tersisa.",
    ]
    write_summary(summary)

    log("Selesai. Ringkas: artifact=%d, cache=%d, run_dihapus=%s, prune=%d"
        % (len(deleted_artifacts), len(deleted_caches), run_deleted, len(pruned)))
    print("\n".join(summary))
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except RuntimeError as error:
        print(f"ERROR: {error}", file=sys.stderr)
        raise SystemExit(1)
