package meme.wheresthebus.comms;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by hb on 10/03/2018.
 */

public class BusRoutes extends AsyncTask<BusStop, Void, HashMap<String, BusStop>> {
    private static final String routeinfoURL = "http://10.9.156.46:8080/api/routeinfo";
    private static final double loadFactor = 0.015;
    public BusRoutes (){

    }

    @Override
    protected HashMap<String, BusStop> doInBackground(BusStop... busStops) {
        HashMap<String, BusStop> stops = new HashMap<>();

        for(int i = 0; i < busStops.length; i++){
            stops.put(busStops[i].id, busStops[i]);
        }

        HashMap<String, BusStop> stopInfo = executeBusStopRequest(stops);

        return stops;
    }



    private HashMap<String, BusStop> executeBusStopRequest(HashMap<String, BusStop> stops){
        try {
            URL url = new URL(routeinfoURL + ParameterStringBuilder.getStopIDs(stops));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream()));
            StringBuilder contents = new StringBuilder();

            while(scanner.hasNext()){
                contents.append(scanner.nextLine());
            }

            connection.disconnect();

            JSONObject fullResponse = new JSONObject(contents.toString());
            Iterator<String> keyItr = fullResponse.keys();

            while(keyItr.hasNext()){
                String key = keyItr.next();
                ArrayDeque<String> buses = new ArrayDeque<>();
                JSONArray jsonBuses = fullResponse.getJSONArray("key");
                for(int i = 0; i < jsonBuses.length(); i++){
                    buses.add(jsonBuses.getString(i));
                }
                stops.get(key).addInfo(new BusStopInfo(buses));
            }

            return stops;
        } catch (JSONException | IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public class BusRoute{

    }
}
