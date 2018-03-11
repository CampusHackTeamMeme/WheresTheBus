from flask_restful import Resource
from flask import request
import sqlite3 as sql

class serviceStops(Resource):
    def __init__(self, file):
        self.DBfile = file

    def get(self):
        r = request.args.to_dict()
        print(r)

        conn = sql.connect(self.DBfile)
        c = conn.cursor()

        query = c.execute('''SELECT DISTINCT stops.stop_id FROM stops 
							INNER JOIN routes_stops ON stops.stop_id = routes_stops.stop_id
							INNER JOIN routes ON routes_stops.route_id = routes.route_id
							WHERE service = ?''', (r['service'],))

        data = []
        for i in query.fetchall():
        	data.append(i[0])

       	return {'service': data}, 200