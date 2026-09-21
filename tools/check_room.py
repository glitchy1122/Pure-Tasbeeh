import subprocess, tempfile, os, sqlite3, shutil

def sh(*a):
    return subprocess.run(a, capture_output=True)

# Pull room db
local = r"C:\Users\Glitch\Desktop\MY Hobby\Pure Tasbeeh\tools\data\room_check.db"
# checkpoint not available; pull main db
sh("adb", "shell", "run-as", "com.adreesulhassan.puretasbeeh", "cp", "databases/pure_tasbeeh.db", "/data/local/tmp/pt.db")
# run-as can't always cp to /data/local/tmp; try cat
p = subprocess.run(
    ["adb", "exec-out", "run-as", "com.adreesulhassan.puretasbeeh", "cat", "databases/pure_tasbeeh.db"],
    capture_output=True,
)
open(local, "wb").write(p.stdout)
print("pulled", len(p.stdout))
# also wal
for extra in ["pure_tasbeeh.db-wal", "pure_tasbeeh.db-shm"]:
    p2 = subprocess.run(
        ["adb", "exec-out", "run-as", "com.adreesulhassan.puretasbeeh", "cat", f"databases/{extra}"],
        capture_output=True,
    )
    open(local + extra.replace("pure_tasbeeh.db", ""), "wb").write(p2.stdout)
    print(extra, len(p2.stdout))

con = sqlite3.connect(local)
print("tables", con.execute("select name from sqlite_master where type='table'").fetchall())
try:
    print("content", con.execute("select count(*) from content").fetchone())
    print("by_cat", con.execute("select category, sect_tag, count(*) from content group by 1,2").fetchall())
    print("hadith", con.execute("select count(*) from ahadith").fetchone())
    print("sample_ur", con.execute("select substr(translation_ur,1,80) from ahadith limit 1").fetchone())
    print("sample_dua", con.execute("select title, category, sect_tag, length(arabic_text) from content order by length(arabic_text) desc limit 5").fetchall())
except Exception as e:
    print("err", e)
