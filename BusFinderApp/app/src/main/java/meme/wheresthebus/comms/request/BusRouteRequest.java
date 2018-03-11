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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import meme.wheresthebus.comms.ParameterStringBuilder;
import meme.wheresthebus.comms.data.BusRoute;
import meme.wheresthebus.comms.data.BusStop;
import meme.wheresthebus.comms.data.BusStopInfo;

/**
 * Created by hb on 11/03/2018.
 */

public class BusRouteRequest extends AsyncTask<String, Void, BusRoute> {
    private static final String routeURL  = "http://10.9.156.46:8080/api/servicestops";

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
            JSONArray stopList = fullResponse.getJSONArray(stopID[0]);

            ArrayDeque<String> stops = new ArrayDeque<>();

            for(int i=0; i<stopList.length(); i++){
                stops.add(stopList.getString(i));
            }

            return new BusRoute(stopID[0], stops);
        } catch (JSONException | IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
