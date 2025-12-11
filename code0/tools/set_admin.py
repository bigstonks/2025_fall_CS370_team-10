import sqlite3

conn = sqlite3.connect('delivery.db')
cursor = conn.cursor()

# Check current users
print("Current users and authorization levels:")
cursor.execute('SELECT userName, authorization FROM userAccount')
for row in cursor.fetchall():
    print(f"  {row[0]}: {row[1]}")

# Set testUser as admin
print("\nUpdating 'testUser' to admin...")
cursor.execute("UPDATE userAccount SET authorization = 'admin' WHERE userName = 'testUser'")
conn.commit()
print(f"Rows affected: {cursor.rowcount}")

# Verify
print("\nVerifying update:")
cursor.execute("SELECT userName, authorization FROM userAccount WHERE authorization = 'admin'")
admins = cursor.fetchall()
print(f"Admin users: {admins}")

conn.close()

