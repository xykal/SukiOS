#!/usr/bin/env python3
"""Membuat font bawaan SukiOS (res/font) dari font variabel resmi.

Sumber (dipin ke satu commit repo google/fonts, lisensi SIL OFL 1.1; teks lisensi ada di assets/licenses):
  Inter            ofl/inter/Inter[opsz,wght].ttf
  Plus Jakarta Sans ofl/plusjakartasans/PlusJakartaSans[wght].ttf
Langkah: unduh -> verifikasi SHA-256 -> ambil instans statis per bobot (fontTools.varLib.instancer) ->
pangkas ke aksara Latin dan fitur yang dipakai (fontTools.subset). Kedua font tidak mendeklarasikan Reserved Font
Name, jadi hasil pangkasan boleh memakai nama aslinya.

Pakai (butuh `pip install fonttools`, hanya untuk membuat ulang; build aplikasi tidak memerlukannya):
  python3 tools/mkfonts.py
"""
import hashlib
import os
import sys
import urllib.request

from fontTools import subset
from fontTools.ttLib import TTFont
from fontTools.varLib import instancer

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "sukios/app/src/main/res/font")
PIN = "9710da1eacb3be272583c3224dcb70f9da6eadbb"
BASE = "https://raw.githubusercontent.com/google/fonts/%s/ofl/" % PIN
SOURCES = {
    "inter": (BASE + "inter/Inter%5Bopsz%2Cwght%5D.ttf", "29160a80ff49ddcab2c97711247e08b1fab27a484a329ce8b813d820dc559031"),
    "jakarta": (BASE + "plusjakartasans/PlusJakartaSans%5Bwght%5D.ttf", "89b3fb38aa0d275d7a731d0d817a4f1622b316b4d7fbdedcf02ee9099ff68bc8"),
}
UNI = ("U+0020-007E,U+00A0-00FF,U+0131,U+0152-0153,U+02C6,U+02DA,U+02DC,U+2013-2014,"
       "U+2018-201A,U+201C-201E,U+2022,U+2026,U+2039-203A,U+2190-2193,U+2212,U+00B7")
FEATURES = ["kern", "liga", "calt", "ccmp", "locl", "mark", "mkmk", "tnum", "case", "zero"]
# (berkas, sumber, sumbu)
CUTS = [
    ("inter_regular", "inter", {"wght": 400, "opsz": 14}),
    ("inter_medium", "inter", {"wght": 500, "opsz": 14}),
    ("inter_semibold", "inter", {"wght": 600, "opsz": 14}),
    ("inter_bold", "inter", {"wght": 700, "opsz": 14}),
    ("inter_display_light", "inter", {"wght": 300, "opsz": 32}),
    ("jakarta_bold", "jakarta", {"wght": 700}),
    ("jakarta_extrabold", "jakarta", {"wght": 800}),
]


def fetch(key):
    url, want = SOURCES[key]
    data = urllib.request.urlopen(url, timeout=120).read()
    got = hashlib.sha256(data).hexdigest()
    if got != want:
        sys.exit("SHA-256 %s tidak cocok: %s" % (key, got))
    path = "/tmp/suki-%s.ttf" % key
    with open(path, "wb") as fh:
        fh.write(data)
    return path


def make(src, axes, out):
    inst = instancer.instantiateVariableFont(TTFont(src), axes, inplace=False)
    opt = subset.Options()
    opt.layout_features = FEATURES
    opt.name_IDs = ["*"]
    opt.name_languages = ["*"]
    opt.name_legacy = True
    opt.notdef_outline = True
    opt.glyph_names = False
    opt.hinting = False
    opt.drop_tables += ["STAT", "DSIG"]
    sub = subset.Subsetter(opt)
    sub.populate(unicodes=subset.parse_unicodes(UNI))
    sub.subset(inst)
    inst.save(out)
    print(os.path.relpath(out, ROOT), os.path.getsize(out))


def main():
    os.makedirs(OUT, exist_ok=True)
    paths = {k: fetch(k) for k in SOURCES}
    for name, key, axes in CUTS:
        make(paths[key], axes, os.path.join(OUT, name + ".ttf"))


if __name__ == "__main__":
    main()
