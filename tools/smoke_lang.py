import re, subprocess, time, pathlib

PROJ = pathlib.Path(r"C:\Users\Glitch\Desktop\MY Hobby\Pure Tasbeeh")

def sh(*args):
    return subprocess.run(args, capture_output=True, text=True, encoding="utf-8", errors="ignore")

def dump():
    sh("adb", "shell", "uiautomator", "dump", "/sdcard/ui.xml")
    p = sh("adb", "shell", "cat", "/sdcard/ui.xml")
    return p.stdout or ""

def tap_text(pattern):
    raw = dump()
    m = re.search(rf'text="([^"]*{pattern}[^"]*)"[^>]*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', raw)
    if not m:
        return None
    x = (int(m.group(2)) + int(m.group(4))) // 2
    y = (int(m.group(3)) + int(m.group(5))) // 2
    sh("adb", "shell", "input", "tap", str(x), str(y))
    print("tapped", m.group(1), x, y)
    time.sleep(1.5)
    return m.group(1)

def snap(name):
    sh("adb", "shell", "screencap", "-p", f"/sdcard/{name}.png")
    sh("adb", "pull", f"/sdcard/{name}.png", str(PROJ / f"{name}.png"))

time.sleep(2)
for p in [r"اردو", "Urdu", r"جاری", "Continue", "Both", r"دونوں", r"كلاهما", r"شیعہ", "Shia"]:
    tap_text(p)

time.sleep(5)
snap("lang_home")
raw = dump()
texts = re.findall(r'text="([^"]{15,100})"', raw)
for t in texts[:20]:
    print("TXT:", t)
