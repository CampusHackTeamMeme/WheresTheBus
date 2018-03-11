import re

import requests
from bs4 import BeautifulSoup
from flask import request
from flask_restful import Resource

BUS_STOP_URL = "http://nextbuses.mobi/WebView/BusStopSearch/BusStopSearchResults?id=%s"


class TimeTable(Resource):
    def __init__(self, file):
        self.DBfile = file

    def get(self):
        r = request.args.to_dict()

        html = requests.get(BUS_STOP_URL % r['stop'])
        parsed_html = BeautifulSoup(html.text, "lxml")
        bus_stops_table = parsed_html.body.find('table', attrs={'class': 'BusStops'})

        to_send = {}

        if not bus_stops_table:
            to_send[r['stop']] = [{'error' : "No Bus Times Available"}]
            return to_send, 200

        bus_stops_rows = bus_stops_table.find_all("tr")

        data = []

        for row in bus_stops_rows:
            cells = row.find_all("td")
            bus_regex = "(?P<dest>.*)at\s(?P<time>\d\d:\d\d)"
            bus_service = cells[0].find("a").text
            print(cells[1].text)
            bus_return = re.search(bus_regex, cells[1].text)
            bus_dest = bus_return.group('dest')
            bus_time = bus_return.group('time')
            bus_dict = {
                'service': bus_service,
                'time': bus_time,
                'dest': bus_dest
            }
            data.append(bus_dict)

        to_send[r['stop']] = data

        return to_send, 200
