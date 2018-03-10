import sqlite3 as sql

# create required sql tables
def createBase(DBfile):
    conn = sql.connect(DBfile)
    c = conn.cursor()

    #c.execute()

    conn.commit()
    c.close()
    conn.close()
