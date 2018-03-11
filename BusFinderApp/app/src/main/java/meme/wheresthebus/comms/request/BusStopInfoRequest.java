package meme.wheresthebus.comms.request;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Scanner;

import meme.wheresthebus.comms.data.BusStop;
import meme.wheresthebus.comms.data.BusStopInfo;

/**
 * Created by hb on 10/03/2018.
 */

public class BusStopInfoRequest extends AsyncTask<BusStop, Void, HashMap<String, BusStopInfo>> {
    private static final String routeinfoURL = "http://10.9.156.46:8080/api/routeinfo";
    private static final double loadFactor = 0.015;
    public BusStopInfoRequest(){

    }

    @Override
    protected HashMap<String, BusStopInfo> doInBackground(BusStop... busStops) {
        HashMap<String, BusStop> stops = new HashMap<>();

        for(int i = 0; i < busStops.length; i++){
            stops.put(busStops[i].id, busStops[i]);
        }

        HashMap<String, BusStopInfo> stopInfo = executeBusStopRequest(stops);

        return stopInfo;
    }



    private HashMap<String, BusStopInfo> executeBusStopRequest(HashMap<String, BusStop> stops){
        try {
            URL url = new URL(routeinfoURL + ParameterStringBuilder.getStop(stops));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream()));
            StringBuilder contents = new StringBuilder();

            while(scanner.hasNext()){
                contents.append(scanner.nextLine());
            }

            connection.disconnect();

            JSONObject fullResponse = new JSONObject(contents.toString());

            HashMap<String, BusStopInfo> stopInfo = new HashMap<>();

            ArrayDeque<String> buses = new ArrayDeque<>();
            JSONArray jsonBuses = fullResponse.getJSONArray("stop");
            for(int i = 0; i < jsonBuses.length(); i++){
                buses.add(jsonBuses.getString(i));
            }

            stopInfo.put((String) stops.keySet().toArray()[0], new BusStopInfo(buses));


            return stopInfo;
        } catch (JSONException | IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
