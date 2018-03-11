package meme.wheresthebus.comms.data;

import java.util.ArrayDeque;

/**
 * Created by hb on 11/03/2018.
 */

public class BusRoute {
    public String name;
    public ArrayDeque<String> route;

    public BusRoute (String name, ArrayDeque<String> route){
        this.route = route;
    }
}
