#!/usr/bin/env python3
"""
Export tables from one or more SQLite database files into per-table CSV files.
Writes files into an output directory (default: ./csv_exports/).
Produces a master index CSV with metadata about each exported table.

Usage:
  python tools/export_sqlite_to_csv.py --db delivery.db identifier.sqlite --out csv_exports

Behavior:
- Skips sqlite internal tables (names starting with 'sqlite_').
- Writes a header row using column names.
- NULLs are exported as empty fields by default; set --null-repr to use a sentinel (e.g. "\\N").
- BLOBs are base64-encoded by default. Use --blob skip to write a placeholder '<BLOB>' instead.
- If the DB file is locked by another process, the script copies it to a temp file and exports from the copy.
"""

from __future__ import annotations
import argparse
import csv
import os
import re
import shutil
import sqlite3
import sys
import tempfile
import base64
from datetime import datetime


def sanitize_name(s: str) -> str:
    s = s or ""
    s = s.lower()
    s = re.sub(r"[^0-9a-z]+", "_", s)
    s = re.sub(r"_+", "_", s).strip("_")
    return s or "unnamed"


def ensure_dir(path: str) -> None:
    os.makedirs(path, exist_ok=True)


def copy_if_locked(db_path: str) -> str:
    """Try to open DB normally; if fails, copy to temp and return temp path."""
    try:
        # Try a read-only connection
        conn = sqlite3.connect(f"file:{db_path}?mode=ro", uri=True)
        conn.execute("PRAGMA schema_version;")
        conn.close()
        return db_path
    except Exception:
        # Copy to temp
        tmp = tempfile.NamedTemporaryFile(delete=False, suffix=".db")
        tmp.close()
        shutil.copyfile(db_path, tmp.name)
        return tmp.name


def list_tables(conn: sqlite3.Connection) -> list:
    cur = conn.cursor()
    cur.execute("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name;")
    rows = cur.fetchall()
    return [r[0] for r in rows]


def export_table(conn: sqlite3.Connection, table: str, out_csv: str, null_repr: str | None, blob_mode: str) -> int:
    cur = conn.cursor()
    # Get column names
    cur.execute(f"PRAGMA table_info('{table}')")
    cols_info = cur.fetchall()
    col_names = None
    cur2 = conn.cursor()
    cur2.execute(f"SELECT * FROM '{table}'")
    col_names = [d[0] for d in cur2.description]

    # Stream rows to CSV
    rows_exported = 0
    with open(out_csv, "w", newline='', encoding='utf-8') as f:
        writer = csv.writer(f, quoting=csv.QUOTE_MINIMAL)
        writer.writerow(col_names)
        for row in cur2:
            out_row = []
            for v in row:
                if v is None:
                    out_row.append(null_repr if null_repr is not None else "")
                elif isinstance(v, (bytes, bytearray)):
                    if blob_mode == "base64":
                        try:
                            enc = base64.b64encode(bytes(v)).decode('ascii')
                            out_row.append(enc)
                        except Exception:
                            out_row.append('<BLOB>')
                    elif blob_mode == "skip":
                        out_row.append('')
                    else:
                        out_row.append('<BLOB>')
                else:
                    out_row.append(v)
            writer.writerow(out_row)
            rows_exported += 1
    return rows_exported


def export_db(db_path: str, out_dir: str, null_repr: str | None, blob_mode: str, timestamp: str) -> list:
    entries = []
    base = os.path.basename(db_path)
    dbbase = os.path.splitext(base)[0]
    working_db = copy_if_locked(db_path)
    try:
        conn = sqlite3.connect(working_db)
    except Exception as e:
        print(f"Failed to open DB {db_path}: {e}")
        if working_db != db_path:
            try:
                os.remove(working_db)
            except Exception:
                pass
        return entries

    try:
        tables = list_tables(conn)
        if not tables:
            print(f"No user tables found in {db_path}.")
        for table in tables:
            safe_db = sanitize_name(dbbase)
            safe_table = sanitize_name(table)
            csv_name = f"{safe_db}__{safe_table}__{timestamp}.csv"
            csv_path = os.path.join(out_dir, csv_name)
            try:
                rows = export_table(conn, table, csv_path, null_repr, blob_mode)
                entries.append({
                    'source_db': db_path,
                    'table_name': table,
                    'csv_filename': csv_name,
                    'rows_exported': rows,
                    'notes': f'blob={blob_mode}'
                })
                print(f"Exported {table} ({rows} rows) -> {csv_path}")
            except Exception as e:
                print(f"Failed to export table {table} from {db_path}: {e}")
    finally:
        conn.close()
        if working_db != db_path:
            try:
                os.remove(working_db)
            except Exception:
                pass
    return entries


def write_index(entries: list, out_dir: str, timestamp: str) -> str:
    idx_name = f"export_index__{timestamp}.csv"
    idx_path = os.path.join(out_dir, idx_name)
    with open(idx_path, 'w', newline='', encoding='utf-8') as f:
        w = csv.writer(f)
        w.writerow(['source_db', 'table_name', 'csv_filename', 'rows_exported', 'export_time', 'notes'])
        for e in entries:
            w.writerow([e['source_db'], e['table_name'], e['csv_filename'], e['rows_exported'], datetime.utcnow().isoformat() + 'Z', e.get('notes','')])
    return idx_path


def main(argv=None):
    p = argparse.ArgumentParser(description="Export SQLite DB tables to CSV files")
    p.add_argument('--db', nargs='+', required=True, help='One or more sqlite database files to export')
    p.add_argument('--out', default='csv_exports', help='Output directory for CSV files')
    p.add_argument('--null-repr', default=None, help='Representation for NULL values in CSV (default: empty)')
    p.add_argument('--blob', choices=['base64', 'skip', 'placeholder'], default='base64', help='How to handle BLOB columns')
    p.add_argument('--timestamp', default=None, help='Timestamp to add to filenames (default: now YYYYMMDD_HHMMSS)')
    args = p.parse_args(argv)

    timestamp = args.timestamp or datetime.utcnow().strftime('%Y%m%d_%H%M%S')
    out_dir = args.out
    ensure_dir(out_dir)

    all_entries = []
    for db in args.db:
        if not os.path.isfile(db):
            print(f"Database file not found: {db}")
            continue
        entries = export_db(db, out_dir, args.null_repr, args.blob, timestamp)
        all_entries.extend(entries)

    if all_entries:
        idx = write_index(all_entries, out_dir, timestamp)
        print(f"Wrote index: {idx}")
    else:
        print("No tables exported.")


if __name__ == '__main__':
    main()

