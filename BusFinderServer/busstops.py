from flask_restful import Resource
from flask import request
import sqlite3 as sql


class BusStops(Resource):
    def __init__(self, file):
        self.DBfile = file

    def get(self):
        r = request.args.to_dict()
        print(r)

        conn = sql.connect(self.DBfile)
        c = conn.cursor()
        
        sLon, eLon = float(r['startLon']), float(r['endLon'])
        sLat, eLat = float(r['startLat']), float(r['endLat'])

        query = c.execute(
            '''SELECT * FROM stops 
                INNER JOIN routes_stops ON stops.stop_id = routes_stops.stop_id
                INNER JOIN routes ON routes_stops.route_id = routes.route_id
                WHERE lon > ?
                AND lon < ?
                AND lat > ?
                AND lat < ?''',
                (sLon, eLon, sLat, eLat))

        keys = ('stop_id', 'name', 'lon', 'lat')

        return {'data': [dict(zip(keys, i)) for i in query.fetchall()]}, 200
