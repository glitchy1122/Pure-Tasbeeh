import sqlite3
p = r"C:\Users\Glitch\Desktop\MY Hobby\Pure Tasbeeh\app\src\main\assets\databases\duas.db"
c = sqlite3.connect(p)
c.execute("UPDATE content SET sect_tag='BOTH' WHERE category IN ('DAILY','NAMAZ')")
c.commit()
print(c.execute("select category, sect_tag, count(*) from content group by 1,2").fetchall())
c.close()
print("ok")
