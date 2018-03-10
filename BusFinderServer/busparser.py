import requests

# from xmljson import badgerfish as bf
# from xml.etree.ElementTree import fromstring

url = 'http://data.southampton.ac.uk/dumps/bus-info/2018-03-04/routes.json'

r = requests.get(url)

if r.status_code == 200:
	Jdata = r.json()


# get xml
# temp_json=bf.data(fromstring(temp_xml))