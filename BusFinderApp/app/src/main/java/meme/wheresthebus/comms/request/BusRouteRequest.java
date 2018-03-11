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
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import meme.wheresthebus.comms.data.BusRoute;

/**
 * Created by hb on 11/03/2018.
 */

public class BusRouteRequest extends AsyncTask<String, Void, BusRoute> {
    private static final String routeURL  = "http://10.9.156.46:8080/api/servicestops";
    private static final String latLonURL = "http://10.9.156.46:8080/api/stoplocations";

    @Override
    protected BusRoute doInBackground(String... stopID) {
        try {
            URL url = new URL(routeURL + "?service=" + stopID[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream()));
            StringBuilder contents = new StringBuilder();

            while (scanner.hasNext()) {
                contents.append(scanner.nextLine());
            }

            connection.disconnect();

            JSONObject fullResponse = new JSONObject(contents.toString());
            JSONArray stopList = fullResponse.getJSONArray("service");

            ArrayDeque<String> stops = new ArrayDeque<>();

            for(int i=0; i < stopList.length(); i++){
                stops.add(stopList.getString(i));
            }

            URL url2 = new URL( latLonURL + "?stops=" + ParameterStringBuilder.makeArray(stops));
            HttpURLConnection connection2 = (HttpURLConnection) url2.openConnection();

            Scanner scanner2 = new Scanner(new InputStreamReader(connection2.getInputStream()));
            StringBuilder contents2 = new StringBuilder();

            while (scanner2.hasNext()) {
                contents2.append(scanner2.nextLine());
            }


            connection2.disconnect();

            JSONObject fullResponse2 = new JSONObject(contents2.toString());
            ArrayDeque<LatLng> busRouteData = new ArrayDeque<>();

            while(!stops.isEmpty()){
                JSONObject locData = fullResponse2.getJSONObject(stops.poll());
                busRouteData.add(
                        new LatLng(locData.getDouble("lat"),
                                locData.getDouble("lon")));
            }

            return new BusRoute(stopID[0], busRouteData);
        } catch (JSONException | IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
