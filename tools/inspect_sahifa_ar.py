import re
from pathlib import Path
from bs4 import BeautifulSoup

html = Path(r"C:\Users\Glitch\Desktop\MY Hobby\Pure Tasbeeh\tools\data\wikisource_ar.html").read_text(encoding="utf-8", errors="ignore")
# Prefer bs4 if available else regex
try:
    soup = BeautifulSoup(html, "html.parser")
except Exception:
    import subprocess, sys
    subprocess.check_call([sys.executable, "-m", "pip", "install", "beautifulsoup4", "-q"])
    from bs4 import BeautifulSoup
    soup = BeautifulSoup(html, "html.parser")

content = soup.select_one("div.mw-parser-output")
text = content.get_text("\n", strip=True) if content else ""
Path(r"C:\Users\Glitch\Desktop\MY Hobby\Pure Tasbeeh\tools\data\sahifa_ar_plain.txt").write_text(text, encoding="utf-8")
print("plain_len", len(text))
# show headings-like lines
for i, line in enumerate(text.splitlines()[:80]):
    if line.strip():
        print(f"{i:03d}|{line[:120]}")
