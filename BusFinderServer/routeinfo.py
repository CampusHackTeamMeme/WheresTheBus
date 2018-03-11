from flask_restful import Resource
from flask import request
import sqlite3 as sql


class RouteInfo(Resource):
    def __init__(self, file):
        self.DBfile = file

    def get(self):
        r = request.args.to_dict()
        print(r)

        conn = sql.connect(self.DBfile)
        c = conn.cursor()

        toSend = {}


        query = c.execute(
            '''SELECT routes.string_bus 
            FROM routes_stops
            INNER JOIN routes ON routes_stops.route_id = routes.route_id
            WHERE routes_stops.stop_id = ?
            ''', (r['stop'],))

        data = []
        for i in query.fetchall():
            data.append(i[0])
        data = list(set(data))

        toSend['stop'] = data

        return toSend, 200
