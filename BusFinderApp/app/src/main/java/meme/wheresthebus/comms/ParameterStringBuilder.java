package meme.wheresthebus.comms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import meme.wheresthebus.comms.data.BusStop;

/**
 * Created by hb on 10/03/2018.
 */

public class ParameterStringBuilder {
    public static String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? "?" + resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

    public static String getStopIDs (HashMap<String, BusStop> stops) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        result.append("stop=");

        for (String stopID : stops.keySet()) {
            result.append(URLEncoder.encode(stopID, "UTF-8"));
            result.append(",");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? "?" + resultString.substring(0, resultString.length() - 1)
                : resultString;
    }
}