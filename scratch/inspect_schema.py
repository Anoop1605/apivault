import psycopg2
try:
    conn = psycopg2.connect(dbname='apivault', user='postgres', password='postgres', host='localhost')
    cur = conn.cursor()
    cur.execute("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'events'")
    for row in cur.fetchall():
        print(f"Column: {row[0]}, Type: {row[1]}")
    conn.close()
except Exception as e:
    print(e)
