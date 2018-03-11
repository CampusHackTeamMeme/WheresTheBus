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
import java.util.Iterator;
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

            JSONObject fullResponse = new JSONObject(contents.toString()).getJSONObject(busStops[0].id);

            Iterator<String> keys = fullResponse.keys();

            HashMap<String, ArrayList<String>> finalTimes = new HashMap<>();

            System.out.println(fullResponse.toString());
            while(keys.hasNext()) {
                String current = keys.next();
                JSONArray times = fullResponse.getJSONObject(current).getJSONArray("time");
                ArrayList<String> strTimes = new ArrayList<String>();
                for (int i = 0; i < times.length(); i++) {
                    strTimes.add(i,times.getString(i));
                }
                finalTimes.put(current, strTimes);
            }



            //System.out.print(full);




            return finalTimes;
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
