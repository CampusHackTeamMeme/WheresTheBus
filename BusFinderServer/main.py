import sqlite3

from flask import Flask
from flask_restful import Api

from busstops import BusStops
from routeinfo import RouteInfo
from timetable import TimeTable
from flask import g

app = Flask(__name__)
api = Api(app)

DBfile = 'bus_finder.db'

api.add_resource(BusStops, '/api/busstops', resource_class_args=(DBfile,))
api.add_resource(RouteInfo, '/api/routeinfo', resource_class_args=(DBfile,))
api.add_resource(TimeTable, '/api/timetable', resource_class_args=(DBfile,))

app.run(host='0.0.0.0', port=8080, debug=True)


@app.teardown_appcontext
def close_connection(exception):
    db = getattr(g, '_database', None)
    if db is not None:
        db.close()
