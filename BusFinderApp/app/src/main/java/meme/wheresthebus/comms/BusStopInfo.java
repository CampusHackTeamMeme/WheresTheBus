package meme.wheresthebus.comms;

import java.util.ArrayDeque;

/**
 * Created by hb on 11/03/2018.
 */

public class BusStopInfo {
    public ArrayDeque<String> services;

    public BusStopInfo(ArrayDeque<String> services){
        this.services = services;
    }
}
