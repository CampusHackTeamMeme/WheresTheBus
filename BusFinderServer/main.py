from flask import Flask
from flask_restful import Api
#from utils import createBase

from busstops import busStops
from routeinfo import routeInfo

app = Flask(__name__)
api = Api(app)

DBfile = 'bus_finder.db'

api.add_resource(busStops, '/api/busstops', resource_class_args=(DBfile,))
api.add_resource(routeInfo, '/api/routeinfo', resource_class_args=(DBfile,))

app.run(host='0.0.0.0', port=8080, debug=True)