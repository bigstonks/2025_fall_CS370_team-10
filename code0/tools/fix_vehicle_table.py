import sqlite3

conn = sqlite3.connect('delivery.db')
cursor = conn.cursor()

# Check current structure
print("Current table structure:")
cursor.execute('PRAGMA table_info(vehicle)')
for col in cursor.fetchall():
    print(col)

# Add missing columns
columns_to_add = [
    ('vehicleModel', 'TEXT'),
    ('currentVehicleDriven', 'TEXT DEFAULT "false"'),
    ('currentVehicleMiles', 'INTEGER DEFAULT 0')
]

for col_name, col_type in columns_to_add:
    try:
        cursor.execute(f'ALTER TABLE vehicle ADD COLUMN {col_name} {col_type}')
        print(f'Added {col_name} column')
    except Exception as e:
        print(f'{col_name}: {e}')

conn.commit()

# Show updated table structure
print("\nUpdated table structure:")
cursor.execute('PRAGMA table_info(vehicle)')
for col in cursor.fetchall():
    print(col)

conn.close()
print("\nDone!")

