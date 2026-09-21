import re, subprocess

def sh(*a):
    return subprocess.run(a, capture_output=True, text=True, encoding="utf-8", errors="ignore")

sh("adb", "shell", "input", "tap", "540", "1200")
sh("adb", "shell", "uiautomator", "dump", "/sdcard/ui.xml")
raw = sh("adb", "shell", "cat", "/sdcard/ui.xml").stdout
print("ui_len", len(raw or ""))
for t in re.findall(r'text="([^"]{2,80})"', raw or "")[:40]:
    print("T:", t)

print("--- databases ---")
print(sh("adb", "shell", "run-as", "com.adreesulhassan.puretasbeeh", "ls", "-la", "databases").stdout)
print(sh("adb", "shell", "run-as", "com.adreesulhassan.puretasbeeh", "ls", "-la", "databases").stderr)
