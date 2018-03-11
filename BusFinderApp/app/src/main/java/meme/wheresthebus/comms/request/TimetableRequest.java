package meme.wheresthebus.comms.request;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import meme.wheresthebus.comms.data.BusStop;
import meme.wheresthebus.comms.data.BusStopInfo;
import meme.wheresthebus.comms.data.Timetable;

/**
 * Created by costin on 11/03/2018.
 */

public class TimetableRequest extends AsyncTask<BusStop, Void, HashMap<String, ArrayList<String>>> {




    private static final String routeURL = "http://10.9.156.46:8080/api/timetable";

    @Override
    protected HashMap<String, ArrayList<String>> doInBackground(BusStop... busStops) {

        HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
        try {


            URL url = new URL(routeURL + "?stop=" + busStops[0].id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream()));
            StringBuilder contents = new StringBuilder();

            while (scanner.hasNext()) {
                contents.append(scanner.nextLine());

            }

            Timetable key = new Timetable();

            JSONObject fullResponse = new JSONObject(contents.toString());

            JSONArray servicesAndTimes = fullResponse.getJSONArray(busStops[0].id);

            for(int i=0;i<servicesAndTimes.length();i++){
                ArrayList<String> times = new ArrayList<String>();
                JSONArray timesJSON = (
                        (JSONObject)servicesAndTimes.get(0)).
                        getJSONArray(((JSONObject) servicesAndTimes.
                                get(0)).getString("time")
                        );
                for (int j=0;j<timesJSON.length();j++){
                    times.add(timesJSON.get(j).toString());
                }

                result.put(servicesAndTimes.get(i).toString(), times);


            }


            HashMap<String, BusStopInfo> stopInfo = new HashMap<>();

            //System.out.print(full);




            return result;
            //System.err.print(stops);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;


    }



}
