import requests

r = requests.get('http://127.0.0.1:8080/api/busstops', data={'startLon':0,'endLon':1000000, 'startLat':0, 'endLat':10000000})

r = requests.get('http://127.0.0.1:8080/api/routeinfo', data={'stops': [1,2,3,4,5]})
