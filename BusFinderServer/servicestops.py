import sqlite3 as sql

from flask import request
from flask_restful import Resource


class ServiceStops(Resource):
    def __init__(self, file):
        self.DBfile = file

    def get(self):
        r = request.args.to_dict()
        print(r)

        conn = sql.connect(self.DBfile)
        c = conn.cursor()

        query = c.execute('''SELECT DISTINCT stops.stop_id, routes.operator FROM stops 
INNER JOIN routes_stops ON stops.stop_id = routes_stops.stop_id
INNER JOIN routes ON routes_stops.route_id = routes.route_id
WHERE service = ?''', (r['service'],))

        data = {}
        for i in query.fetchall():
            data.setdefault(i[1], []).append(i[0])

        return data, 200
