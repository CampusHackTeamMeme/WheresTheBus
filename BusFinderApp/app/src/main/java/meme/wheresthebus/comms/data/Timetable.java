package meme.wheresthebus.comms.data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by costin on 11/03/2018.
 */

public class Timetable {
    public HashMap<String, ArrayList<String>> servicesAndTimes= new HashMap<String, ArrayList<String>>();

    public ArrayList<String> getTimesForService(String service){
        return servicesAndTimes.get(service);
    }


}
