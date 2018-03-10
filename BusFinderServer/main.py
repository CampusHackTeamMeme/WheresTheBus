from flask import Flask
from flask_restful import Api

import asyncio
import asyncpg

from busstops import busStops

app = Flask(__name__)
api = Api(app)

api.add_resource(busStops, '/api/busStops/')


app.run(host='0.0.0.0', port=80, debug=True)