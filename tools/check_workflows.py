#!/usr/bin/env python3
"""Pemeriksa keamanan workflow GitHub Actions: kontrol rantai pasok tanpa dependensi.

Aturan (pelanggaran apa pun menggagalkan CI):
  W1  setiap `uses:` pihak ketiga dipin ke SHA commit penuh (40 heksadesimal)
  W2  tidak ada ekspresi ${{ ... }} berisi input yang bisa dikendalikan penyerang di dalam `run:`
      (nama tag, branch, input dispatch, isi event); lewatkan lewat `env:`
  W3  workflow punya blok `permissions:` tingkat atas
  W4  setiap job punya `timeout-minutes`

Pakai:
  python3 tools/check_workflows.py               periksa .github/workflows/*.yml
  python3 tools/check_workflows.py --self-test   buktikan pemeriksa ini benar-benar menolak contoh buruk

Pemeriksa membaca baris demi baris (bukan parser YAML penuh) dan hanya memahami bentuk YAML yang
dipakai repo ini. Kalau workflow ditulis dengan bentuk lain, ubah pemeriksa ini, jangan dilonggarkan.
"""
from __future__ import annotations

import pathlib
import re
import sys
from collections.abc import Iterator

SHA = re.compile(r"^[0-9a-f]{40}$")
USES = re.compile(r"^\s*(?:-\s+)?uses:\s*['\"]?([^\s'\"#]+)")
RUN = re.compile(r"^(\s*)(-\s+)?run:\s*(.*)$")
UNSAFE = re.compile(
    r"\$\{\{[^}]*\b(github\.event\.|github\.head_ref|github\.ref_name|github\.ref\b|inputs\.)[^}]*\}\}"
)
JOB_KEY = re.compile(r"^  ([A-Za-z0-9_-]+):\s*$")
TIMEOUT = re.compile(r"^    timeout-minutes:\s*\d+")

Problem = tuple[int, str, str]


def run_scripts(lines: list[str]) -> Iterator[tuple[int, str]]:
    """Setiap baris yang menjadi isi skrip `run:` (satu baris atau blok | dan >)."""
    i, n = 0, len(lines)
    while i < n:
        m = RUN.match(lines[i])
        if not m or lines[i].lstrip().startswith("#"):
            i += 1
            continue
        key_indent = len(m.group(1)) + (len(m.group(2)) if m.group(2) else 0)
        rest = m.group(3)
        if rest[:1] in ("|", ">"):
            j = i + 1
            while j < n:
                line = lines[j]
                if line.strip() and len(line) - len(line.lstrip(" ")) <= key_indent:
                    break
                if line.strip():
                    yield j + 1, line
                j += 1
            i = j
        else:
            yield i + 1, rest
            i += 1


def job_blocks(lines: list[str]) -> list[tuple[str, int, int]]:
    start = next((i for i, line in enumerate(lines) if re.match(r"^jobs:\s*$", line)), None)
    if start is None:
        return []
    heads: list[tuple[str, int]] = []
    end = len(lines)
    for i in range(start + 1, len(lines)):
        if re.match(r"^\S", lines[i]):
            end = i
            break
        m = JOB_KEY.match(lines[i])
        if m:
            heads.append((m.group(1), i))
    blocks = []
    for idx, (name, at) in enumerate(heads):
        stop = heads[idx + 1][1] if idx + 1 < len(heads) else end
        blocks.append((name, at, stop))
    return blocks


def check(text: str) -> list[Problem]:
    lines = text.split("\n")
    problems: list[Problem] = []

    for i, line in enumerate(lines, 1):
        if line.lstrip().startswith("#"):
            continue
        m = USES.match(line)
        if not m or m.group(1).startswith("./"):
            continue
        ref = m.group(1)
        if "@" not in ref or not SHA.match(ref.split("@", 1)[1]):
            problems.append((i, "W1", f"action tidak dipin ke SHA commit penuh: {ref}"))

    for lineno, script in run_scripts(lines):
        if UNSAFE.search(script):
            problems.append((lineno, "W2", "input yang bisa dikendalikan penyerang masuk langsung ke skrip; pakai env:"))

    if not any(re.match(r"^permissions:", line) for line in lines):
        problems.append((1, "W3", "tidak ada permissions: tingkat atas"))

    for name, start, stop in job_blocks(lines):
        if not any(TIMEOUT.match(line) for line in lines[start:stop]):
            problems.append((start + 1, "W4", f"job '{name}' tanpa timeout-minutes"))

    return problems


_PIN = "a" * 40

_BAD_SAMPLES: dict[str, str] = {
    "W1": (
        "permissions:\n  contents: read\njobs:\n  a:\n    timeout-minutes: 5\n    steps:\n"
        "      - uses: actions/checkout@v4\n"
    ),
    "W2": (
        "permissions:\n  contents: read\njobs:\n  a:\n    timeout-minutes: 5\n    steps:\n"
        "      - run: echo \"${{ github.event.inputs.tag }}\"\n"
    ),
    "W2-block": (
        "permissions:\n  contents: read\njobs:\n  a:\n    timeout-minutes: 5\n    steps:\n"
        "      - name: x\n        run: |\n          set -e\n          TAG=\"${{ github.ref_name }}\"\n"
    ),
    "W3": "jobs:\n  a:\n    timeout-minutes: 5\n    steps:\n      - run: echo hi\n",
    "W4": "permissions:\n  contents: read\njobs:\n  a:\n    steps:\n      - run: echo hi\n",
}

_GOOD_SAMPLE = (
    "permissions:\n  contents: read\njobs:\n  a:\n    timeout-minutes: 5\n    steps:\n"
    f"      - uses: actions/checkout@{_PIN} # v4\n"
    "      - name: aman\n        env:\n          TAG: ${{ github.ref_name }}\n"
    "        run: |\n          echo \"$TAG\"\n"
)


def self_test() -> int:
    failures = []
    for rule, sample in _BAD_SAMPLES.items():
        found = {p[1] for p in check(sample)}
        if rule.split("-")[0] not in found:
            failures.append(f"contoh buruk {rule} tidak terdeteksi (terdeteksi: {sorted(found) or 'tidak ada'})")
    clean = check(_GOOD_SAMPLE)
    if clean:
        failures.append(f"contoh aman ditolak: {clean}")
    for f in failures:
        print(f"SELF-TEST GAGAL: {f}", file=sys.stderr)
    if not failures:
        print(f"self-test lulus: {len(_BAD_SAMPLES)} contoh buruk ditolak, 1 contoh aman diterima")
    return 1 if failures else 0


def main(argv: list[str]) -> int:
    if "--self-test" in argv:
        return self_test()
    root = pathlib.Path(".github/workflows")
    files = sorted(root.glob("*.yml")) + sorted(root.glob("*.yaml"))
    if not files:
        print("tidak ada workflow ditemukan di .github/workflows", file=sys.stderr)
        return 2
    total = 0
    for path in files:
        for lineno, rule, message in check(path.read_text(encoding="utf-8")):
            print(f"{path}:{lineno}: {rule}: {message}", file=sys.stderr)
            total += 1
    if total:
        print(f"{total} pelanggaran", file=sys.stderr)
        return 1
    print(f"{len(files)} workflow bersih (W1-W4)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
