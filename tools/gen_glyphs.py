#!/usr/bin/env python3
"""Pembuat set ikon SukiOS (Aurora).

Satu-satunya sumber ikon: berkas ini. Ia menulis `SukiGlyphData.kt` (enum GlyphKind berisi data path SVG
pada kisi 24x24) dan, bila diminta, lembar pratinjau PNG. Semua bentuk digambar sendiri dari primitif
(garis, lingkaran, persegi bulat, busur); tidak ada ikon pihak ketiga yang disalin atau dilacak.

Pakai:
  python3 tools/gen_glyphs.py            # tulis SukiGlyphData.kt
  python3 tools/gen_glyphs.py --check    # gagal (kode 1) bila berkas di repo berbeda dari hasil generator
  python3 tools/gen_glyphs.py --preview docs/glyphs.png
"""
import math
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "sukios/app/src/main/java/app/sukios/SukiGlyphData.kt")
K = 0.5522847498


def n(v):
    s = ("%.2f" % v).rstrip("0").rstrip(".")
    return "0" if s in ("-0", "") else s


def M(x, y): return "M%s %s" % (n(x), n(y))
def L(x, y): return "L%s %s" % (n(x), n(y))
def C(a, b, c, d, e, f): return "C%s %s %s %s %s %s" % tuple(n(v) for v in (a, b, c, d, e, f))


def line(x1, y1, x2, y2):
    return " ".join([M(x1, y1), L(x2, y2)])


def poly(pts, close=False):
    parts = [M(*pts[0])] + [L(*p) for p in pts[1:]]
    if close:
        parts.append("Z")
    return " ".join(parts)


def rpoly(pts, r, close=False):
    """Poligon/garis patah dengan sudut dibulatkan (jari-jari r, dibatasi setengah panjang sisi)."""
    cnt = len(pts)
    out = []
    idx = range(cnt) if close else range(1, cnt - 1)
    first = None
    for i in idx:
        p0, p1, p2 = pts[(i - 1) % cnt], pts[i], pts[(i + 1) % cnt]
        v1 = (p0[0] - p1[0], p0[1] - p1[1])
        v2 = (p2[0] - p1[0], p2[1] - p1[1])
        l1, l2 = math.hypot(*v1), math.hypot(*v2)
        rr = min(r, l1 / 2.0, l2 / 2.0)
        a = (p1[0] + v1[0] / l1 * rr, p1[1] + v1[1] / l1 * rr)
        b = (p1[0] + v2[0] / l2 * rr, p1[1] + v2[1] / l2 * rr)
        c1 = (a[0] + (p1[0] - a[0]) * 2 / 3, a[1] + (p1[1] - a[1]) * 2 / 3)
        c2 = (b[0] + (p1[0] - b[0]) * 2 / 3, b[1] + (p1[1] - b[1]) * 2 / 3)
        if first is None:
            first = a
            out.append(M(*a) if close else M(*pts[0]))
            if not close:
                out.append(L(*a))
        else:
            out.append(L(*a))
        out.append(C(c1[0], c1[1], c2[0], c2[1], b[0], b[1]))
    if not close:
        out.append(L(*pts[-1]))
    else:
        out.append("Z")
    return " ".join(out)


def circle(cx, cy, r):
    k = K * r
    return " ".join([
        M(cx + r, cy),
        C(cx + r, cy + k, cx + k, cy + r, cx, cy + r),
        C(cx - k, cy + r, cx - r, cy + k, cx - r, cy),
        C(cx - r, cy - k, cx - k, cy - r, cx, cy - r),
        C(cx + k, cy - r, cx + r, cy - k, cx + r, cy),
        "Z",
    ])


def rrect(x, y, w, h, r):
    r = min(r, w / 2.0, h / 2.0)
    k = K * r
    return " ".join([
        M(x + r, y), L(x + w - r, y),
        C(x + w - r + k, y, x + w, y + r - k, x + w, y + r), L(x + w, y + h - r),
        C(x + w, y + h - r + k, x + w - r + k, y + h, x + w - r, y + h), L(x + r, y + h),
        C(x + r - k, y + h, x, y + h - r + k, x, y + h - r), L(x, y + r),
        C(x, y + r - k, x + r - k, y, x + r, y), "Z",
    ])


def arc(cx, cy, r, a0, a1, move=True):
    """Busur lingkaran; sudut dalam derajat, 0 = kanan, 90 = bawah (sumbu y ke bawah)."""
    sweep = a1 - a0
    segs = max(1, int(math.ceil(abs(sweep) / 90.0)))
    step = math.radians(sweep) / segs
    t = 4.0 / 3.0 * math.tan(step / 4.0)
    out = []
    a = math.radians(a0)
    p = (cx + r * math.cos(a), cy + r * math.sin(a))
    if move:
        out.append(M(*p))
    for _ in range(segs):
        b = a + step
        q = (cx + r * math.cos(b), cy + r * math.sin(b))
        c1 = (p[0] - t * r * math.sin(a), p[1] + t * r * math.cos(a))
        c2 = (q[0] + t * r * math.sin(b), q[1] - t * r * math.cos(b))
        out.append(C(c1[0], c1[1], c2[0], c2[1], q[0], q[1]))
        a, p = b, q
    return " ".join(out)


def arc_end(cx, cy, r, a1):
    a = math.radians(a1)
    return (cx + r * math.cos(a), cy + r * math.sin(a))


def head(px, py, dx, dy, size=3.2, spread=38):
    """Kepala panah di (px,py) menghadap arah (dx,dy)."""
    ln = math.hypot(dx, dy)
    dx, dy = dx / ln, dy / ln
    out = []
    for s in (-1, 1):
        t = math.radians(180 + s * spread)
        rx = dx * math.cos(t) - dy * math.sin(t)
        ry = dx * math.sin(t) + dy * math.cos(t)
        out.append(poly([(px + rx * size, py + ry * size), (px, py)]))
    return " ".join(out)


def gear(cx, cy, r_in, r_out, teeth):
    pts = []
    step = 2 * math.pi / teeth
    for i in range(teeth):
        a = i * step - math.pi / 2
        for off, rad in ((-0.30, r_in), (-0.17, r_out), (0.17, r_out), (0.30, r_in)):
            t = a + off * step * 1.6
            pts.append((cx + rad * math.cos(t), cy + rad * math.sin(t)))
    return rpoly(pts, 0.7, close=True)


def star(cx, cy, ro, ri, pts=5):
    out = []
    for i in range(pts * 2):
        a = -math.pi / 2 + i * math.pi / pts
        r = ro if i % 2 == 0 else ri
        out.append((cx + r * math.cos(a), cy + r * math.sin(a)))
    return rpoly(out, 0.9, close=True)


def j(*parts):
    return " ".join(p for p in parts if p)


ICONS = {}


def icon(name, stroke="", fill=""):
    ICONS[name] = (stroke, fill)


icon("HOME", j(rpoly([(3.5, 11), (12, 3.8), (20.5, 11)], 1.2),
               rpoly([(5.6, 9.8), (5.6, 19.2), (18.4, 19.2), (18.4, 9.8)], 1.6),
               rpoly([(10, 19.2), (10, 14.4), (14, 14.4), (14, 19.2)], 0.6)))
icon("APPS", j(rrect(4.5, 4.5, 6, 6, 1.8), rrect(13.5, 4.5, 6, 6, 1.8),
               rrect(4.5, 13.5, 6, 6, 1.8), rrect(13.5, 13.5, 6, 6, 1.8)))
icon("SEARCH", j(circle(10.6, 10.6, 6.3), line(15.3, 15.3, 20, 20)))
icon("SETTINGS", j(gear(12, 12, 6.6, 8.9, 8), circle(12, 12, 2.7)))
icon("TERMINAL", j(rrect(3, 4.5, 18, 15, 3.2), rpoly([(7.2, 9.6), (10.4, 12), (7.2, 14.4)], 0.5),
                   line(12.6, 15, 16.8, 15)))
icon("FLASK", j(line(9.2, 3.6, 14.8, 3.6),
                "M10.4 3.6 V9.4 L5.2 18 C4.5 19.2 5.3 20.4 6.7 20.4 H17.3 C18.7 20.4 19.5 19.2 18.8 18 L13.6 9.4 V3.6",
                line(8.2, 14.6, 15.8, 14.6)))
icon("INFO", j(circle(12, 12, 9), line(12, 11, 12, 16.6)), circle(12, 7.7, 1.2))
icon("PLUS", j(line(12, 5, 12, 19), line(5, 12, 19, 12)))
icon("CHECK", rpoly([(5, 12.6), (9.8, 17.4), (19, 7.2)], 0.6))
icon("REFRESH", j(arc(12, 12, 7.6, 20, 300), head(*arc_end(12, 12, 7.6, 300), 0.866, 0.5, 3.6)))
icon("POWER", j(arc(12, 13, 7.6, -58, 238), line(12, 3.6, 12, 11.2)))
icon("PHONE_LANDSCAPE", rrect(3, 7, 18, 10, 2.6), circle(18.1, 12, 0.95))
icon("DESKTOP", j(rrect(3, 4.5, 18, 12.5, 2.6), line(8.5, 20, 15.5, 20), line(12, 17, 12, 20)))
icon("OVERLAY", j(rrect(3, 5, 18, 9.5, 2.4), rrect(6, 16.8, 12, 3, 1.5)))
icon("SHIELD", "M12 3 L19.4 5.9 V11.4 C19.4 15.8 16.4 19.3 12 21 C7.6 19.3 4.6 15.8 4.6 11.4 V5.9 Z")
icon("SHIELD_CHECK", j("M12 3 L19.4 5.9 V11.4 C19.4 15.8 16.4 19.3 12 21 C7.6 19.3 4.6 15.8 4.6 11.4 V5.9 Z",
                       rpoly([(8.6, 11.8), (11, 14.2), (15.6, 9.4)], 0.5)))
icon("PIN", j(rpoly([(9, 4.2), (15, 4.2), (14.2, 9.6), (17.4, 13.2), (6.6, 13.2), (9.8, 9.6)], 0.9, close=True),
              line(12, 13.2, 12, 20)))
icon("STAR", star(12, 12.6, 8.8, 3.8))
icon("FOLDER", rpoly([(3.5, 6.5), (9.4, 6.5), (11.6, 9), (20.5, 9), (20.5, 18.5), (3.5, 18.5)], 1.8, close=True))
icon("CHEVRON_L", rpoly([(14.5, 5), (7.5, 12), (14.5, 19)], 0.6))
icon("CHEVRON_R", rpoly([(9.5, 5), (16.5, 12), (9.5, 19)], 0.6))
icon("CHEVRON_D", rpoly([(5, 9.5), (12, 16.5), (19, 9.5)], 0.6))
icon("CLOSE", j(line(6.2, 6.2, 17.8, 17.8), line(17.8, 6.2, 6.2, 17.8)))
icon("MIN", line(6, 12, 18, 12))
icon("MAX", rrect(5.5, 5.5, 13, 13, 2.4))
icon("RESTORE", j(rrect(4.5, 8.5, 11, 11, 2.2),
                  rpoly([(8.5, 8.5), (8.5, 4.5), (19.5, 4.5), (19.5, 15.5), (15.5, 15.5)], 2.2)))
icon("WINDOW", j(rrect(3.5, 5, 17, 14, 2.8), line(3.5, 9.6, 20.5, 9.6)), j(circle(6.4, 7.3, 0.75), circle(8.9, 7.3, 0.75)))
icon("CHART", j(rrect(4.8, 12, 3.2, 7.5, 1.2), rrect(10.4, 5.5, 3.2, 14, 1.2), rrect(16, 9.5, 3.2, 10, 1.2)))
icon("WIFI", j(arc(12, 18.6, 5, 228, 312), arc(12, 18.6, 9.4, 228, 312), arc(12, 18.6, 13.8, 228, 312)),
     circle(12, 18.6, 1.25))
icon("WIFI_OFF", j(arc(12, 18.6, 5, 228, 312), arc(12, 18.6, 9.4, 228, 312), line(4, 4, 20, 20)), circle(12, 18.6, 1.25))
icon("BOLT", "", rpoly([(13.4, 2.8), (5.8, 13.4), (11, 13.4), (10.2, 21.2), (18.2, 10.2), (12.8, 10.2)], 1.0, close=True))
icon("VOLUME", j(rpoly([(4, 9.6), (8, 9.6), (12.6, 5.6), (12.6, 18.4), (8, 14.4), (4, 14.4)], 1.0, close=True),
                 arc(12.6, 12, 5.2, -48, 48), arc(12.6, 12, 8.6, -48, 48)))
icon("TRASH", j(line(4, 6.5, 20, 6.5), rpoly([(9.5, 6.5), (9.5, 4.2), (14.5, 4.2), (14.5, 6.5)], 0.8),
                rpoly([(6, 6.5), (7, 19.6), (17, 19.6), (18, 6.5)], 1.6),
                line(10.2, 10.4, 10.2, 16), line(13.8, 10.4, 13.8, 16)))
icon("EXTERNAL", j(rpoly([(10.5, 5), (6.5, 5), (4.5, 7), (4.5, 17.5), (6.5, 19.5), (17, 19.5), (19.5, 17), (19.5, 13.5)], 2.0),
                   line(11, 13, 20, 4), rpoly([(14.2, 4), (20, 4), (20, 9.8)], 0.5)))
icon("FULLSCREEN", j(rpoly([(4, 9), (4, 4), (9, 4)], 0.6), rpoly([(15, 4), (20, 4), (20, 9)], 0.6),
                     rpoly([(20, 15), (20, 20), (15, 20)], 0.6), rpoly([(9, 20), (4, 20), (4, 15)], 0.6)))
icon("SNAP_LEFT", rrect(3.5, 5, 17, 14, 2.8), rpoly([(5.9, 7.4), (11.2, 7.4), (11.2, 16.6), (5.9, 16.6)], 0.8, close=True))
icon("LOCK", j(rrect(5, 10.5, 14, 10, 2.8),
               "M8.4 10.5 V8 C8.4 5.8 10 4.2 12 4.2 C14 4.2 15.6 5.8 15.6 8 V10.5"), circle(12, 15.5, 1.25))
icon("SPARK", "M12 3 C12.9 8.2 15.8 11.1 21 12 C15.8 12.9 12.9 15.8 12 21 C11.1 15.8 8.2 12.9 3 12 C8.2 11.1 11.1 8.2 12 3 Z")
icon("MORE", "", j(circle(5.6, 12, 1.5), circle(12, 12, 1.5), circle(18.4, 12, 1.5)))
icon("COPY", j(rrect(8.6, 8.6, 11, 11, 2.4),
               "M6.2 15.4 H5.9 C4.9 15.4 4.2 14.7 4.2 13.7 V5.9 C4.2 4.9 4.9 4.2 5.9 4.2 H13.7 C14.7 4.2 15.4 4.9 15.4 5.9 V6.2"))
icon("PLAY", rpoly([(8, 5.6), (18.6, 12), (8, 18.4)], 1.8, close=True))
icon("BLUETOOTH", rpoly([(7, 7.6), (17, 16.4), (12, 20.8), (12, 3.2), (17, 7.6), (7, 16.4)], 0.5))
icon("DOWNLOAD", j(line(12, 4, 12, 14.6), rpoly([(7.6, 10.4), (12, 14.8), (16.4, 10.4)], 0.5),
                   rpoly([(4.6, 15.4), (4.6, 19.6), (19.4, 19.6), (19.4, 15.4)], 1.4)))
icon("ALERT", j(rpoly([(12, 4), (21, 19.6), (3, 19.6)], 2.0, close=True), line(12, 10, 12, 14.2)), circle(12, 16.9, 0.95))
icon("OK_CIRCLE", j(circle(12, 12, 9), rpoly([(7.6, 12.4), (10.6, 15.4), (16.4, 9.2)], 0.5)))
icon("FAIL_CIRCLE", j(circle(12, 12, 9), line(8.6, 8.6, 15.4, 15.4), line(15.4, 8.6, 8.6, 15.4)))
icon("WAIT_CIRCLE", j(circle(12, 12, 9), rpoly([(12, 6.8), (12, 12), (15.6, 14.2)], 0.6)))
icon("ARROW_R", j(line(5, 12, 19, 12), rpoly([(13.6, 6.6), (19, 12), (13.6, 17.4)], 0.6)))
icon("USER", j(circle(12, 8.4, 3.8), "M4.6 20 C4.8 15.8 8 13.6 12 13.6 C16 13.6 19.2 15.8 19.4 20"))

NAMES = list(ICONS.keys())


def kotlin():
    lines = [
        "package app.sukios",
        "",
        "// DIHASILKAN oleh tools/gen_glyphs.py. Jangan diedit tangan: ubah generatornya, lalu jalankan ulang.",
        "// Kisi 24x24, goresan membulat. `stroke` digambar sebagai garis, `fill` sebagai bidang penuh.",
        "enum class GlyphKind(val stroke: String, val fill: String = \"\") {",
    ]
    for i, name in enumerate(NAMES):
        s, f = ICONS[name]
        end = ";" if i == len(NAMES) - 1 else ","
        lines.append('    %s("%s", "%s")%s' % (name, s, f, end))
    return "\n".join(lines) + "\n"


# ---- pratinjau (hanya untuk mata manusia; tidak dipakai aplikasi) ----
def _flatten(d, seg=14):
    toks = re.sub(r"([MLHVCZ])", r" \1 ", d).replace(",", " ").split()
    i, subs, cur, pos = 0, [], [], (0.0, 0.0)
    while i < len(toks):
        c = toks[i]
        i += 1
        if c in "ML":
            x, y = float(toks[i]), float(toks[i + 1])
            i += 2
            if c == "M":
                if cur:
                    subs.append((cur, False))
                cur = [(x, y)]
            else:
                cur.append((x, y))
            pos = (x, y)
        elif c in "HV":
            v = float(toks[i])
            i += 1
            pos = (v, pos[1]) if c == "H" else (pos[0], v)
            cur.append(pos)
        elif c == "C":
            v = [float(t) for t in toks[i:i + 6]]
            i += 6
            p0 = pos
            for s in range(1, seg + 1):
                t = s / seg
                u = 1 - t
                x = u ** 3 * p0[0] + 3 * u * u * t * v[0] + 3 * u * t * t * v[2] + t ** 3 * v[4]
                y = u ** 3 * p0[1] + 3 * u * u * t * v[1] + 3 * u * t * t * v[3] + t ** 3 * v[5]
                cur.append((x, y))
            pos = (v[4], v[5])
        elif c == "Z":
            subs.append((cur, True))
            cur = []
    if cur:
        subs.append((cur, False))
    return subs


def preview(path):
    from PIL import Image, ImageDraw
    cols, cell, sc = 10, 112, 14
    rows = (len(NAMES) + cols - 1) // cols
    sheet = Image.new("RGB", (cols * cell, rows * (cell + 18)), (18, 21, 29))
    for idx, name in enumerate(NAMES):
        big = Image.new("RGBA", (24 * sc, 24 * sc), (0, 0, 0, 0))
        d = ImageDraw.Draw(big)
        s, f = ICONS[name]
        w = 1.7 * sc
        for sub, closed in _flatten(f):
            if len(sub) > 2:
                d.polygon([(x * sc, y * sc) for x, y in sub], fill=(242, 244, 248, 255))
        for sub, closed in _flatten(s):
            pts = [(x * sc, y * sc) for x, y in sub] + ([(sub[0][0] * sc, sub[0][1] * sc)] if closed else [])
            if len(pts) > 1:
                d.line(pts, fill=(242, 244, 248, 255), width=int(w), joint="curve")
            for p in (pts[0], pts[-1]):
                d.ellipse([p[0] - w / 2, p[1] - w / 2, p[0] + w / 2, p[1] + w / 2], fill=(242, 244, 248, 255))
        small = big.resize((cell - 24, cell - 24), Image.LANCZOS)
        cx, cy = (idx % cols) * cell, (idx // cols) * (cell + 18)
        sheet.paste(small, (cx + 12, cy + 6), small)
        ImageDraw.Draw(sheet).text((cx + 6, cy + cell - 10), name.lower(), fill=(150, 160, 180))
    sheet.save(path)


def main(argv):
    text = kotlin()
    if "--check" in argv:
        have = open(OUT, encoding="utf-8").read() if os.path.exists(OUT) else ""
        if have != text:
            print("SukiGlyphData.kt tidak sama dengan keluaran generator; jalankan tools/gen_glyphs.py")
            return 1
        print("SukiGlyphData.kt sesuai generator (%d ikon)" % len(NAMES))
        return 0
    if "--preview" in argv:
        preview(argv[argv.index("--preview") + 1])
        print("pratinjau ditulis")
        return 0
    with open(OUT, "w", encoding="utf-8") as fh:
        fh.write(text)
    print("%d ikon ditulis ke %s" % (len(NAMES), os.path.relpath(OUT, ROOT)))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
