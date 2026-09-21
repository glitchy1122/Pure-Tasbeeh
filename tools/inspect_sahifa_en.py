from pathlib import Path
import re
pdf = Path(r"C:\Users\Glitch\.cursor\projects\c-Users-Glitch-Desktop-MY-Hobby-Pure-Tasbeeh\agent-tools\1ac394f2-b949-4ab3-b463-d3554500a2b9.txt").read_text(encoding="utf-8", errors="ignore")
# Find section markers
marks = re.findall(r'^S(\d+)\s*[-–—]\s*(.+)$', pdf, flags=re.M)
print("marks", len(marks))
for n,t in marks[:10]:
    print(n, t[:80])
print("---")
for n,t in marks[-5:]:
    print(n, t[:80])
# also alternate patterns
alt = re.findall(r'^(?:Supplication|Dua|DUA)\s*(\d+)', pdf, flags=re.M|re.I)
print("alt", len(alt), alt[:10])
