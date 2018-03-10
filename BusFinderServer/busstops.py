from flask_restful import Resource
from flask import request
import sqlite3 as sql
from werkzeug.datastructures import ImmutableMultiDict

class busStops(Resource):
    def __init__(self, file):
        self.DBfile = file

    def get(self):
        r = request.form.to_dict(flat=False)
        print(r)
        
        conn = sql.connect(self.DBfile)
        c = conn.cursor()

        stopsquery = c.execute(
            '''SELECT * FROM stops WHERE 
            lon > ? AND 
            lon < ? AND 
            lat > ? AND 
            lat < ? ''',
            (r['startLon'], r['endLon'], r['startLat'], r['endLat']))

        keys = ('stop_id', 'name', 'lat', 'lon')
        toSend = { 'data': [dict( zip(keys, i) ) for i in stopsquery.fetchall()]}

        for stop in toSend['data']:
            busRoutes = []

            Routesquery = c.execute(
                '''SELECT route_id FROM routes_stops WHERE
                stop_id = ?''',
                (stop['stop_id'],))

            for route_id in Routesquery.fetchall():
                query = c.execute(
                    '''SELECT service FROM routes WHERE
                    route_id = ?
                    ''', (route_id,))

                busRoutes.append(query.fetchall()[0])

            stop['busRoutes'] = busRoutes

        return toSend, 200
