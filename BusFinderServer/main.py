from flask import Flask
from flask_restful import Api
from utils import createBase
import sqlite3 as sql

from busstops import busStops

app = Flask(__name__)
api = Api(app)

DBfile = 'database.db'
createBase(DBfile)

api.add_resource(busStops, '/api/busStops/')

app.run(host='0.0.0.0', port=80, debug=True)