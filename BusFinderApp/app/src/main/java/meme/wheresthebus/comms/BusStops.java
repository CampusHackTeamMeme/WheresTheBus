package meme.wheresthebus.comms;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Created by hb on 10/03/2018.
 */

public class BusStops {
    private static final String busStopServerURL = "10.9.156.46:8080/api/busstops";
    public BusStops (){

    }

    public ArrayDeque<BusStop> getStops(double startLon, double startLat, double endLon, double endLat){
        try {
            JSONObject request = new JSONObject().put("startLat", startLat)
                    .put("startLon", startLon)
                    .put("endLat", endLat)
                    .put("endLon", endLon);

            return executeBusStopRequest(request);

        } catch(JSONException e){
            return null;
        }
    }

    private ArrayDeque<BusStop> executeBusStopRequest(JSONObject json){
        try {
            URL busStopServer = new URL(busStopServerURL);
            URLConnection connection = busStopServer.openConnection();
            Iterator<String> itr = json.keys();

            while(itr.hasNext()){
                String currentKey = itr.next();
                connection.addRequestProperty(currentKey, json.get(currentKey).toString());
            }

            Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream()));
            StringBuilder contents = new StringBuilder();

            while(scanner.hasNext()){
                contents.append(scanner.nextLine());
            }

            JSONArray response = new JSONArray(contents.toString());

            ArrayDeque<BusStop> stops = new ArrayDeque<>();

            for(int i=0; i < response.length(); i++){
                JSONObject stopAsJSON = response.getJSONObject(i);
                BusStop stop = new BusStop(stopAsJSON.getString("name"),
                        stopAsJSON.getString("desc"),
                        stopAsJSON.getDouble("x"),
                        stopAsJSON.getDouble("y"));
                stops.add(stop);
            }

            return stops;
        } catch (JSONException | IOException e){
            return null;
        }
    }

    public class BusStop {
        public String name;
        public String desc;
        public Double x;
        public Double y;

        public BusStop(String name, String desc, Double x, Double y){
            this.name = name;
            this.desc = desc;
            this.x = x;
            this.y = y;
        }
    }
}
