package meme.wheresthebus.comms.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayDeque;

/**
 * Created by hb on 11/03/2018.
 */

public class BusRoute {
    public String name;
    public ArrayDeque<LatLng> route;

    public BusRoute (String name, ArrayDeque<LatLng> route){
        this.route = route;
    }
}
