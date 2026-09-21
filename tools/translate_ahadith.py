#!/usr/bin/env python3
"""Auto-translate ahadith.db EN -> UR/FA via Google Translate (no Gemini tokens)."""
import json
import os
import sqlite3
import time

from deep_translator import GoogleTranslator

ROOT = os.path.dirname(os.path.abspath(__file__))
DB = os.path.join(ROOT, "..", "app", "src", "main", "assets", "databases", "ahadith.db")
PROGRESS = os.path.join(ROOT, "data", "hadith_tr_progress.json")
os.makedirs(os.path.dirname(PROGRESS), exist_ok=True)


def chunk(s: str, n: int = 4500):
    s = (s or "").strip()
    if len(s) <= n:
        return [s] if s else []
    parts = []
    while s:
        parts.append(s[:n])
        s = s[n:]
    return parts


def tr(translator, text: str) -> str:
    outs = []
    for p in chunk(text):
        for attempt in range(5):
            try:
                outs.append(translator.translate(p))
                time.sleep(0.12)
                break
            except Exception:
                time.sleep(1.5 * (attempt + 1))
                if attempt == 4:
                    raise
    return " ".join(outs)


def main():
    con = sqlite3.connect(DB)
    con.row_factory = sqlite3.Row
    rows = con.execute(
        "SELECT id, translation, translation_ur, translation_fa FROM ahadith ORDER BY id"
    ).fetchall()
    need = []
    for r in rows:
        en = (r["translation"] or "").strip()
        ur = (r["translation_ur"] or "").strip()
        fa = (r["translation_fa"] or "").strip()
        if en and (ur == en or fa == en or not ur or not fa):
            need.append(int(r["id"]))
    print("rows", len(rows), "need", len(need), flush=True)

    done = {}
    if os.path.exists(PROGRESS):
        with open(PROGRESS, encoding="utf-8") as f:
            done = json.load(f)

    ur_t = GoogleTranslator(source="en", target="ur")
    fa_t = GoogleTranslator(source="en", target="fa")
    updated = 0
    for i, rid in enumerate(need):
        key = str(rid)
        if key in done and done[key].get("ur") and done[key].get("fa"):
            ur, fa = done[key]["ur"], done[key]["fa"]
        else:
            en = con.execute(
                "SELECT translation FROM ahadith WHERE id=?", (rid,)
            ).fetchone()[0]
            try:
                ur = tr(ur_t, en)
                fa = tr(fa_t, en)
            except Exception as e:
                print("fail", rid, e, flush=True)
                continue
            done[key] = {"ur": ur, "fa": fa}
            if (i + 1) % 10 == 0:
                with open(PROGRESS, "w", encoding="utf-8") as f:
                    json.dump(done, f, ensure_ascii=False)
                print(f"progress {i+1}/{len(need)}", flush=True)

        con.execute(
            "UPDATE ahadith SET translation_ur=?, translation_fa=? WHERE id=?",
            (ur, fa, rid),
        )
        updated += 1
        if updated % 25 == 0:
            con.commit()

    con.commit()
    with open(PROGRESS, "w", encoding="utf-8") as f:
        json.dump(done, f, ensure_ascii=False)
    same = con.execute(
        "SELECT COUNT(*) FROM ahadith WHERE translation_ur = translation"
    ).fetchone()[0]
    print("updated", updated, "still_en_clones", same, flush=True)
    con.close()


if __name__ == "__main__":
    main()
