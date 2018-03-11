package meme.wheresthebus.comms.data;

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

    @Override
    public int hashCode(){
        return id.hashCode();
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof BusStop){
            return this.hashCode() == other.hashCode();
        } else {
            return false;
        }
    }
}