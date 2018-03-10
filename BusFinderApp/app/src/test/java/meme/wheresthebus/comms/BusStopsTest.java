package meme.wheresthebus.comms;

/**
 * Created by hb on 10/03/2018.
 */

import org.junit.Test;

import meme.wheresthebus.comms.BusStops;

import static org.junit.Assert.assertTrue;

public class BusStopsTest {
    @Test
    public void jsonTest(){
        BusStops bs = new BusStops();
        assertTrue(bs.getStops(0.0,0.0,1.0,1.0) != null);
    }
}
