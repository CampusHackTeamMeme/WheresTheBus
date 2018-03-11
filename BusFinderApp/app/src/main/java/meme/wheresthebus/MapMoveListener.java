package meme.wheresthebus;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by hb on 11/03/2018.
 */

public class MapMoveListener implements GoogleMap.OnCameraMoveListener {
    private GoogleMap gmap;
    private NavigationDrawer nd;
    private int counter;

    public MapMoveListener(GoogleMap gmap, NavigationDrawer nd){
        this.gmap = gmap;
        this.nd = nd;
        counter = 0;
    }

    @Override
    public void onCameraMove() {
        if(counter++ > 25){
            nd.addStopsAsync(gmap, gmap.getCameraPosition().target, gmap.getCameraPosition().zoom);
            counter = 0;
        }
    }
}
