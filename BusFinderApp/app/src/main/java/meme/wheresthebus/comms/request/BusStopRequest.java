package meme.wheresthebus.comms.request;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

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

/**
 * Created by hb on 10/03/2018.
 */

public class BusStopRequest extends AsyncTask<Double, Void, ArrayDeque<BusStop>> {
    private static final String busStopServerURL = "http://10.9.156.46:8080/api/busstops";
    private static final double loadFactor = 0.015;
    public BusStopRequest(){

    }

    @Override
    protected ArrayDeque<BusStop> doInBackground(Double... positionData) {
        LatLng latLng = new LatLng(positionData[0], positionData[1]);


        return getStops(latLng,  (float) positionData[2].doubleValue());
    }

    private ArrayDeque<BusStop> getStops(LatLng position, float zoom){
        double startLat = position.latitude - loadFactor / Math.log(zoom);
        double startLon = position.longitude - loadFactor / Math.log(zoom);
        double endLat = position.latitude + loadFactor / Math.log(zoom);
        double endLon = position.longitude + loadFactor / Math.log(zoom);

        //System.out.println(startLat);
        //System.out.println(startLon);
        //System.out.println(endLat);
        //System.out.println(endLon);


        HashMap<String, String> request = new HashMap<>();
        request.put("startLat", Double.toString(startLat));
        request.put("startLon", Double.toString(startLon));
        request.put("endLat", Double.toString(endLat));
        request.put("endLon", Double.toString(endLon));

        return executeBusStopRequest(request);

    }

    private ArrayDeque<BusStop> executeBusStopRequest(HashMap<String, String> params){
        try {
            URL url = new URL(busStopServerURL + ParameterStringBuilder.getParamsString(params));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream()));
            StringBuilder contents = new StringBuilder();

            while(scanner.hasNext()){
                contents.append(scanner.nextLine());
            }

            //System.out.println(contents.toString());

            connection.disconnect();

            JSONObject fullResponse = new JSONObject(contents.toString());
            JSONArray response = fullResponse.getJSONArray("data");

            ArrayDeque<BusStop> stops = new ArrayDeque<>();

            for(int i=0; i < response.length(); i++){
                JSONObject stopAsJSON = response.getJSONObject(i);
                BusStop stop = new BusStop(stopAsJSON.getString("name"),
                        new LatLng(stopAsJSON.getDouble("lon"),
                            stopAsJSON.getDouble("lat")),
                        stopAsJSON.getString("stop_id"));
                stops.add(stop);
            }

            return stops;
        } catch (JSONException | IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
