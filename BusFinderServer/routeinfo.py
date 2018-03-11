from flask_restful import Resource
from flask import request
import sqlite3 as sql
from werkzeug.datastructures import ImmutableMultiDict

class routeInfo(Resource):
    def __init__(self, file):
        self.DBfile = file

    def get(self):
        r = request.form.to_dict(flat=False)
        print(r)

        conn = sql.connect(self.DBfile)
        c = conn.cursor()

        toSend = {}

        for stops in r['stops']:
            query = c.execute(
                '''SELECT routes.service 
                FROM routes_stops
                INNER JOIN routes ON routes_stops.route_id = routes.route_id
                where routes_stops.stop_id = ?
                ''', (stops,))
            
            data = []
            for i in query.fetchall():
                data.append(i[0])
            data = list(set(data))

            toSend[stops] = data

        return toSend, 200
