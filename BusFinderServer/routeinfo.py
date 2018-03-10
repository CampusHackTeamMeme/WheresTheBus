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
            busRoutes = []
        
            Routesquery = c.execute(
                '''SELECT route_id FROM routes_stops WHERE
                stop_id = ?''',
                (stops,))

            for route_id in Routesquery.fetchall():
                query = c.execute(
                    '''SELECT service FROM routes WHERE
                    route_id = ?
                    ''', (route_id,))

                busRoutes.append(query.fetchall()[0])

            toSend[stops] = busRoutes

        return toSend, 200
