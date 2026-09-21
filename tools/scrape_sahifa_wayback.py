#!/usr/bin/env python3
"""Scrape complete Sahifa (54) from Wayback Machine duas.org mobile pages."""
from __future__ import annotations

import json
import re
import time
from pathlib import Path
from urllib.request import Request, urlopen

from bs4 import BeautifulSoup

DATA = Path(__file__).resolve().parent / "data"
INDEX = DATA / "arch_index.html"
OUT = DATA / "sahifa_full.json"
UA = "Mozilla/5.0 (compatible; PureTasbeehBot/1.0)"


def fetch(url: str) -> str:
    # Prefer id_ for original content without archive toolbar
    if "web.archive.org/web/" in url and "id_/" not in url:
        url = re.sub(r"(web\.archive\.org/web/\d+)", r"\1id_", url)
    req = Request(url, headers={"User-Agent": UA})
    with urlopen(req, timeout=60) as r:
        return r.read().decode("utf-8", errors="ignore")


def extract_links(index_html: str) -> list[tuple[int, str, str]]:
    """Return list of (num, title, url)."""
    links = []
    for m in re.finditer(
        r'href="(https?://web\.archive\.org/web/\d+/https?://www\.duas\.org/mobile/sahifasajjadia-dua(\d+)[^"]+\.html)"[^>]*>\s*<h5>\s*(\d+)\.\s*([^<]+)',
        index_html,
        flags=re.I,
    ):
        url, n1, n2, title = m.group(1), int(m.group(2)), int(m.group(3)), m.group(4).strip()
        num = n2 if n2 else n1
        links.append((num, title, url))
    # dedupe by num
    by_num = {}
    for num, title, url in links:
        by_num[num] = (num, title, url)
    return [by_num[k] for k in sorted(by_num)]


def parse_page(html: str) -> tuple[str, str]:
    soup = BeautifulSoup(html, "html.parser")
    # Remove scripts/styles
    for t in soup(["script", "style", "noscript"]):
        t.decompose()
    text = soup.get_text("\n", strip=True)
    # Arabic lines
    ar_lines = []
    en_lines = []
    for line in text.splitlines():
        line = line.strip()
        if not line or len(line) < 8:
            continue
        if re.search(r"[\u0600-\u06FF]", line):
            # skip nav junk
            if any(x in line for x in ("دوآء", "فهرست", "سابقه", "بعدی", "Archive")):
                continue
            ar_lines.append(line)
        else:
            if any(x in line.lower() for x in ("wayback", "archive.org", "http", "menu", "home")):
                continue
            # keep substantive english
            if sum(c.isalpha() for c in line) > 20:
                en_lines.append(line)
    arabic = "\n".join(ar_lines).strip()
    english = "\n\n".join(en_lines).strip()
    return arabic, english


def main():
    if not INDEX.exists():
        raise SystemExit("missing arch_index.html")
    index_html = INDEX.read_text(encoding="utf-8", errors="ignore")
    links = extract_links(index_html)
    print("links", len(links), flush=True)
    if len(links) < 40:
        # fallback: build URLs from known pattern using live mobile + wayback prefix
        print("few links; dumping sample", flush=True)
        print(index_html[index_html.find("sahifasajjadia") : index_html.find("sahifasajjadia") + 200])

    existing = {}
    if OUT.exists():
        existing = {int(x["num"]): x for x in json.loads(OUT.read_text(encoding="utf-8"))}

    items = []
    for num, title, url in links:
        if num in existing and existing[num].get("arabic") and len(existing[num]["arabic"]) > 80:
            items.append(existing[num])
            print("cached", num, flush=True)
            continue
        try:
            html = fetch(url)
            arabic, english = parse_page(html)
            print(num, "ar", len(arabic), "en", len(english), flush=True)
            items.append({
                "num": num,
                "title_en": title,
                "arabic": arabic,
                "en": english,
                "url": url,
            })
            time.sleep(0.4)
        except Exception as e:
            print("fail", num, e, flush=True)
            items.append({
                "num": num,
                "title_en": title,
                "arabic": existing.get(num, {}).get("arabic", ""),
                "en": existing.get(num, {}).get("en", ""),
                "url": url,
                "error": str(e),
            })
        OUT.write_text(json.dumps(items, ensure_ascii=False, indent=1), encoding="utf-8")

    ok = sum(1 for x in items if len(x.get("arabic") or "") > 80)
    print("done", len(items), "with_arabic", ok, flush=True)


if __name__ == "__main__":
    main()
