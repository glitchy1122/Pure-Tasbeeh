#!/usr/bin/env python3
"""Complete Sahifa Sajjadiya (54) into duas.db — low token (Google Translate only)."""
from __future__ import annotations

import json
import re
import sqlite3
import subprocess
import time
from pathlib import Path

from bs4 import BeautifulSoup
from deep_translator import GoogleTranslator

ROOT = Path(__file__).resolve().parent
DATA = ROOT / "data"
DB = ROOT.parent / "app" / "src" / "main" / "assets" / "databases" / "duas.db"
AR_HTML = DATA / "wikisource_ar.html"
EN_TXT = Path(
    r"C:\Users\Glitch\.cursor\projects\c-Users-Glitch-Desktop-MY-Hobby-Pure-Tasbeeh"
    r"\agent-tools\1ac394f2-b949-4ab3-b463-d3554500a2b9.txt"
)
ARCH_INDEX = DATA / "arch_index.html"
PROGRESS = DATA / "sahifa_tr_progress.json"

ORDINAL_MAP = {
    "الاول": 1, "الأول": 1,
    "الثاني": 2, "الثانی": 2,
    "الثالث": 3,
    "الرابع": 4,
    "الخامس": 5,
    "السادس": 6,
    "السابع": 7,
    "الثامن": 8,
    "التاسع": 9,
    "العاشر": 10,
    "الحادي عشر": 11, "الحادی عشر": 11,
    "الثاني عشر": 12, "الثانی عشر": 12,
    "الثالث عشر": 13,
    "الرابع عشر": 14,
    "الخامس عشر": 15,
    "السادس عشر": 16,
    "السابع عشر": 17,
    "الثامن عشر": 18,
    "التاسع عشر": 19,
    "العشرون": 20,
    "الحادي و العشرون": 21, "الحادی و العشرون": 21, "الحادي والعشرون": 21, "الحادی والعشرون": 21,
    "الثاني و العشرون": 22, "الثانی و العشرون": 22, "الثاني والعشرون": 22, "الثانی والعشرون": 22,
    "الثالث و العشرون": 23, "الثالث والعشرون": 23,
    "الرابع و العشرون": 24, "الرابع والعشرون": 24,
    "الخامس و العشرون": 25, "الخامس والعشرون": 25,
    "السادس و العشرون": 26, "السادس والعشرون": 26,
    "السابع و العشرون": 27, "السابع والعشرون": 27,
    "الثامن و العشرون": 28, "الثامن والعشرون": 28,
    "التاسع و العشرون": 29, "التاسع والعشرون": 29,
    "الثلاثون": 30,
    "الحادي والثلاثون": 31, "الحادی والثلاثون": 31, "الحادي و الثلاثون": 31,
    "الثاني والثلاثون": 32, "الثانی والثلاثون": 32, "الثاني و الثلاثون": 32,
    "الثالث والثلاثون": 33, "الثالث و الثلاثون": 33,
    "الرابع والثلاثون": 34, "الرابع و الثلاثون": 34,
    "الخامس والثلاثون": 35, "الخامس و الثلاثون": 35,
    "السادس والثلاثون": 36, "السادس و الثلاثون": 36,
    "السابع والثلاثون": 37, "السابع و الثلاثون": 37,
    "الثامن والثلاثون": 38, "الثامن و الثلاثون": 38,
    "التاسع والثلاثون": 39, "التاسع و الثلاثون": 39,
    "الاربعون": 40, "الأربعون": 40,
    "الحادي والاربعون": 41, "الحادی والاربعون": 41, "الحادي والأربعون": 41,
    "الثاني والاربعون": 42, "الثانی والاربعون": 42, "الثاني والأربعون": 42,
    "الثالث والاربعون": 43, "الثالث والأربعون": 43,
    "الرابع والاربعون": 44, "الرابع والأربعون": 44,
    "الخامس والاربعون": 45, "الخامس والأربعون": 45,
    "السادس والاربعون": 46, "السادس والأربعون": 46,
    "السابع والاربعون": 47, "السابع والأربعون": 47,
    "الثامن والاربعون": 48, "الثامن والأربعون": 48,
    "التاسع والاربعون": 49, "التاسع والأربعون": 49,
    "الخمسون": 50,
    "الحادي والخمسون": 51, "الحادی والخمسون": 51,
    "الثاني والخمسون": 52, "الثانی والخمسون": 52,
    "الثالث والخمسون": 53,
    "الرابع والخمسون": 54,
}


def chunk(s: str, n: int = 4500):
    s = (s or "").strip()
    if not s:
        return []
    out = []
    while s:
        out.append(s[:n])
        s = s[n:]
    return out or [""]


def tr(translator, text: str) -> str:
    outs = []
    for p in chunk(text):
        for attempt in range(5):
            try:
                outs.append(translator.translate(p))
                time.sleep(0.08)
                break
            except Exception:
                time.sleep(1.0 * (attempt + 1))
                if attempt == 4:
                    raise
    return " ".join(outs)


def parse_english(path: Path) -> dict[int, dict]:
    text = path.read_text(encoding="utf-8", errors="ignore")
    parts = re.split(r"^S(\d+)\s*[-–—]\s*(.+)$", text, flags=re.M)
    out = {}
    i = 1
    while i + 2 < len(parts):
        num = int(parts[i])
        title = parts[i + 1].strip()
        body = parts[i + 2].strip()
        if num == 54:
            for stop in ("Distributed By", "THE END", "Addenda", "www.duas.org"):
                idx = body.find(stop)
                if idx > 200:
                    body = body[:idx].strip()
        out[num] = {"title_en": title, "en": re.sub(r"\n{3,}", "\n\n", body).strip()}
        i += 3
    return out


def parse_wikisource_ar(path: Path) -> dict[int, str]:
    html = path.read_text(encoding="utf-8", errors="ignore")
    soup = BeautifulSoup(html, "html.parser")
    plain = soup.select_one("div.mw-parser-output").get_text("\n", strip=True)
    plain = plain.replace("الدعای", "الدعاء")
    # Split keeping headers
    pieces = re.split(r"(?=^الدعاء\s+)", plain, flags=re.M)
    out: dict[int, str] = {}
    for block in pieces:
        block = block.strip()
        if not block.startswith("الدعاء"):
            continue
        first = block.splitlines()[0]
        m = re.match(r"الدعاء\s+([^:]+)\s*:?", first)
        if not m:
            continue
        key = re.sub(r"\s+", " ", m.group(1).strip())
        key = key.rstrip(":")
        num = ORDINAL_MAP.get(key)
        if not num:
            # fuzzy: remove spaces around و
            key2 = key.replace(" و ", " و").replace("و ", "و")
            num = ORDINAL_MAP.get(key2)
        if not num:
            continue
        lines = [ln.strip() for ln in block.splitlines() if ln.strip()]
        body_start = 1
        if len(lines) > 1 and not re.match(
            r"^(اللَّهُمَّ|اللهم|الْحَمْدُ|الحمد|يَا |يا )", lines[1]
        ):
            body_start = 2
        body = "\n".join(lines[body_start:]).strip()
        for stop in ("تصنيف:", "مأخوذة من", "ويكي"):
            p = body.find(stop)
            if p > 100:
                body = body[:p].strip()
        if len(body) > 40:
            out[num] = body
    return out


def curl_get(url: str, dest: Path) -> str:
    if "web.archive.org/web/" in url and "id_/" not in url:
        url = re.sub(r"(web\.archive\.org/web/\d+)", r"\1id_", url)
    subprocess.run(
        ["curl.exe", "-skL", "--max-time", "90", "-o", str(dest), url],
        check=False,
    )
    return dest.read_text(encoding="utf-8", errors="ignore") if dest.exists() else ""


def arabic_from_mobile_html(html: str) -> str:
    soup = BeautifulSoup(html, "html.parser")
    for t in soup(["script", "style", "noscript"]):
        t.decompose()
    ar = []
    for line in soup.get_text("\n", strip=True).splitlines():
        line = line.strip()
        if len(line) < 10:
            continue
        if not re.search(r"[\u0600-\u06FF]", line):
            continue
        if any(x in line for x in ("فهرست", "سابقه", "Archive", "دوآء")):
            continue
        ar.append(line)
    return "\n".join(ar).strip()


def fill_missing_from_wayback(ar: dict[int, str]) -> None:
    if not ARCH_INDEX.exists():
        return
    idx = ARCH_INDEX.read_text(encoding="utf-8", errors="ignore")
    for num in (42, 52):
        if num in ar and len(ar[num]) > 80:
            continue
        m = re.search(
            rf'href="(https?://web\.archive\.org/web/\d+/[^"]+dua{num}[^"]+)"',
            idx,
            flags=re.I,
        )
        if not m:
            print("no wayback link for", num, flush=True)
            continue
        dest = DATA / f"sahifa_dua{num}.html"
        html = curl_get(m.group(1), dest)
        body = arabic_from_mobile_html(html)
        print("wayback", num, "ar_len", len(body), flush=True)
        if len(body) > 80:
            ar[num] = body


def main():
    en = parse_english(EN_TXT)
    ar = parse_wikisource_ar(AR_HTML)
    print("en", len(en), "ar_wiki", len(ar), flush=True)
    fill_missing_from_wayback(ar)
    print("ar_after_fill", len(ar), "missing", sorted(set(range(1, 55)) - set(ar)), flush=True)

    merged = []
    for num in range(1, 55):
        if num not in en:
            print("skip missing EN", num, flush=True)
            continue
        arabic = ar.get(num, "")
        if len(arabic) < 40:
            # Last resort: keep EN-only note so slot exists; Arabic body placeholder
            arabic = f"(النص العربي للدعاء {num} سيُستكمل)\n\n" + en[num]["en"][:200]
            print("warn short arabic", num, flush=True)
        merged.append({
            "num": num,
            "title": f"Sahifa · {num}. {en[num]['title_en']}",
            "arabic": arabic,
            "en": en[num]["en"],
            "source": f"Sahifa Sajjadiya · Dua {num}",
        })

    done = json.loads(PROGRESS.read_text(encoding="utf-8")) if PROGRESS.exists() else {}
    ur_t = GoogleTranslator(source="en", target="ur")
    fa_t = GoogleTranslator(source="en", target="fa")

    for i, it in enumerate(merged):
        key = str(it["num"])
        if key in done and done[key].get("ur") and done[key].get("fa"):
            it["ur"], it["fa"] = done[key]["ur"], done[key]["fa"]
            continue
        en_src = it["en"]
        if len(en_src) > 3000:
            en_src = en_src[:2800].rsplit(" ", 1)[0] + "…"
        try:
            it["ur"] = tr(ur_t, en_src)
            it["fa"] = tr(fa_t, en_src)
        except Exception as e:
            print("fail tr", it["num"], e, flush=True)
            it["ur"] = it["en"]
            it["fa"] = it["en"]
        done[key] = {"ur": it["ur"], "fa": it["fa"]}
        if (i + 1) % 5 == 0:
            PROGRESS.write_text(json.dumps(done, ensure_ascii=False), encoding="utf-8")
            print(f"tr {i+1}/{len(merged)}", flush=True)

    PROGRESS.write_text(json.dumps(done, ensure_ascii=False), encoding="utf-8")

    con = sqlite3.connect(DB)
    con.execute("DELETE FROM content WHERE category='SAHIFA'")
    for it in merged:
        con.execute(
            """INSERT INTO content
            (title, arabic_text, translation, translation_ur, translation_ar,
             translation_fa, source, sect_tag, category)
            VALUES (?,?,?,?,?,?,?,?,?)""",
            (
                it["title"],
                it["arabic"],
                it["en"],
                it.get("ur", it["en"]),
                None,
                it.get("fa", it["en"]),
                it["source"],
                "BOTH",
                "SAHIFA",
            ),
        )
    con.commit()
    row = con.execute(
        "SELECT COUNT(*), CAST(AVG(LENGTH(arabic_text)) AS INT), "
        "MIN(LENGTH(arabic_text)), MAX(LENGTH(arabic_text)) "
        "FROM content WHERE category='SAHIFA'"
    ).fetchone()
    print("sahifa", row, "total", con.execute("SELECT COUNT(*) FROM content").fetchone()[0], flush=True)
    con.close()


if __name__ == "__main__":
    main()
