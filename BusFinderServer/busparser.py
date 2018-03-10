import argparse
import sqlite3

import os

ROUTES_URL = 'http://data.southampton.ac.uk/dumps/bus-info/2018-03-04/routes.json'
STOPS_URL = 'http://data.southampton.ac.uk/dumps/bus-info/2018-03-04/stops.json'


def create_db(conn):
    c = conn.cursor()

    # Create history table
    c.execute('''
    CREATE TABLE stops(
        stop_id TEXT PRIMARY KEY NOT NULL,
        desc TEXT,
        lat REAL,
        lon REAL
    )''')
    c.execute('''
    CREATE TABLE routes(
        route_id TEXT PRIMARY KEY NOT NULL,
        desc TEXT,
        operator TEXT,
        direction TEXT,
        service TEXT
    )''')
    c.execute('''
    CREATE TABLE routes_stops(
        route_id TEXT NOT NULL,
        stop_id TEXT NOT NULL,
        PRIMARY KEY (route_id, stop_id),
        FOREIGN KEY (route_id) REFERENCES routes (route_id)
          ON DELETE CASCADE ON UPDATE NO ACTION,
        FOREIGN KEY (stop_id) REFERENCES stops (stop_id)
          ON DELETE CASCADE ON UPDATE NO ACTION
    )''')

    # Save (commit) the changes
    conn.commit()


def delete_db(conn):
    c = conn.cursor()
    c.execute('''DROP TABLE history''')
    c.execute('''DROP TABLE names''')
    c.execute('''DROP TABLE notes''')
    conn.commit()


def main(db_name, should_create_db):
    exists = os.path.isfile(db_name)
    with sqlite3.connect(db_name) as conn:
        # Init the DB
        if not exists:
            create_db(conn)
            print("Database file '{}' didn't exist, created.".format(db_name))
        elif should_create_db:
            delete_db(conn)
            create_db(conn)
            print("Deleted database and recreated.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--create_db", action='store_true')
    parser.add_argument("--database", "-d", default="bus_finder.db")
    args = parser.parse_args()
    main(args.database, args.create_db)
