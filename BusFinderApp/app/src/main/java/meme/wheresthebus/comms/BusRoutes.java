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

public class BusRoutes extends AsyncTask<CameraPosition, Void, ArrayDeque<BusRoutes>> {
    private static final String routeinfoURL = "http://10.9.156.46:8080/api/routeinfo";
    private static final double loadFactor = 0.015;
    public BusRoutes (){

    }

    @Override
    protected ArrayDeque<BusRoutes> doInBackground(CameraPosition... cameraPositions) {
        ArrayDeque<BusRoutes> result = new ArrayDeque<>();


        return result;
    }



    private ArrayDeque<BusStop> executeBusStopRequest(HashMap<String, String> params){
        try {
            URL url = new URL(routeinfoURL + ParameterStringBuilder.getParamsString(params));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream()));
            StringBuilder contents = new StringBuilder();

            while(scanner.hasNext()){
                contents.append(scanner.nextLine());
            }

            connection.disconnect();

            JSONObject fullResponse = new JSONObject(contents.toString());
            JSONArray response = fullResponse.getJSONArray("stops");
            System.out.print(response);
            ArrayDeque<BusStop> stops = new ArrayDeque<>();



            System.out.print(stops);
            return stops;
        } catch (JSONException | IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public class BusStop {
        public String name;
        public LatLng position;

        public BusStop(String name, LatLng position){
            this.name = name;
            this.position = position;
        }
    }
}
