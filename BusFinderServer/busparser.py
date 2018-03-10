import argparse
import sqlite3
import os

import requests

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
    c.execute('''DROP TABLE stops''')
    c.execute('''DROP TABLE routes''')
    c.execute('''DROP TABLE routes_stops''')
    conn.commit()


def insert_stops(conn, stops_data):
    c = conn.cursor()
    stops_length = len(stops_data)
    print("INSERTING STOPS 0/%d" % stops_length, end="\r")
    count = 0
    for stop in stops_data:
        count += 1
        if count % 10 == 0:
            print("INSERTING STOPS %d/%d" % (count, stops_length), end="\r")
        c.execute(
            'INSERT OR REPLACE INTO stops (stop_id, desc, lat, lon) VALUES (?,?,?,?)',
            (stop['id'], stop['label'], float(stop['lat']), float(stop['lon']))
        )
    print("%d STOPS INSERTED" % stops_length)


def insert_routes(conn, routes_data):
    c = conn.cursor()
    routes_length = len(routes_data)
    print("INSERTING Routes 0/%d" % routes_length, end="\r")
    count = 0
    for route in routes_data:
        count += 1
        if count % 10 == 0:
            print("INSERTING ROUTES %d/%d" % (count, routes_length), end="\r")
        c.execute(
            'INSERT OR REPLACE INTO routes (route_id, desc, operator, direction, service) VALUES (?,?,?,?,?)',
            (route['id'], route['label'], route['operator'], route['direction'], route['service'])
        )
        c.executemany(
            'INSERT OR REPLACE INTO routes_stops (route_id, stop_id) VALUES (?,?)',
            [(route['id'], stop) for stop in route['stops']]
        )
    print("%d ROUTES INSERTED" % routes_length)


def import_data(conn):
    print("Getting stops JSON")
    stops_resp = requests.get(STOPS_URL, timeout=30)
    stops_data = stops_resp.json()

    insert_stops(conn, stops_data)

    print("Getting routes JSON")
    routes_resp = requests.get(ROUTES_URL, timeout=30)
    routes_data = routes_resp.json()

    insert_routes(conn, routes_data)


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

        import_data(conn)
        conn.commit()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--create_db", action='store_true')
    parser.add_argument("--database", "-d", default="bus_finder.db")
    args = parser.parse_args()
    main(args.database, args.create_db)
