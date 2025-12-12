import sqlite3
import sys
from pathlib import Path

db_path = Path(__file__).resolve().parents[1] / 'delivery.db'
print('Using DB:', db_path)
conn = sqlite3.connect(str(db_path))
cur = conn.cursor()

print('\nTables:')
for row in cur.execute("SELECT name, type FROM sqlite_master WHERE type IN ('table','view')"):
    print(row)


def show_columns(table):
    print(f"\nColumns for {table}:")
    for col in cur.execute(f"PRAGMA table_info('{table}')"):
        print(col)

show_columns('deliveryData')
show_columns('JobsTable')
show_columns('userAccount')

# show first few rows
print('\nSample rows from deliveryData:')
for row in cur.execute('SELECT * FROM deliveryData LIMIT 5'):
    print(row)

conn.close()

