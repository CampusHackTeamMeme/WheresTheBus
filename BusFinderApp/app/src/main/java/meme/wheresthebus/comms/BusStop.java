package meme.wheresthebus.comms;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by hb on 11/03/2018.
 */

public class BusStop {
    public String id;
    public String name;
    public LatLng position;
    public BusStopInfo info;

    public BusStop(String name, LatLng position, String id){
        this.id = id;
        this.name = name;
        this.position = position;
    }

    public void addInfo(BusStopInfo info){
        this.info = info;
    }
}