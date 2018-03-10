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
import java.util.Scanner;

/**
 * Created by hb on 10/03/2018.
 */

public class BusStops {
    private static final String busStopServerURL = "test";
    public BusStops (){

    }

    public ArrayDeque<BusStop> getStops(Float startX, Float startY, Float endX, Float endY){
        try {
            JSONObject request = new JSONObject().put("startX", startX)
                    .put("startY", startY)
                    .put("endX", endX)
                    .put("endY", endY);

            return executeBusStopRequest(request);

        } catch(JSONException e){
            return null;
        }
    }

    public ArrayDeque<BusStop> executeBusStopRequest(JSONObject json){
        try {
            URL busStopServer = new URL(busStopServerURL);
            URLConnection connection = busStopServer.openConnection();
            connection.
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
                        stopAsJSON.getString("description"),
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
