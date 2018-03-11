from flask import request
from flask_restful import Resource
import sqlite3 as sql


class StopLocations(Resource):
    def __init__(self, file):
        self.DBfile = file

    def get(self):
        r = request.args.to_dict(flat=False)
        print(r)

        conn = sql.connect(self.DBfile)
        c = conn.cursor()

        toSend = {}
        keys = ('stop_id', 'lon', 'lat')

        for stop in r['stops']:
            query = c.execute('''
                SELECT stop_id, lon, lat FROM stops 
                WHERE stop_id = ?''',
                (stop,))
            data = dict(zip(keys, query.fetchall()[0]))

            toSend[data['stop_id']] = {
                'lon': data['lon'],
                'lat': data['lat']
            }

        print(toSend)
        return toSend, 200
