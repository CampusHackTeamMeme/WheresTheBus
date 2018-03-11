package meme.wheresthebus;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayDeque;
import java.util.HashMap;

import meme.wheresthebus.comms.BusStop;

/**
 * Created by hb on 11/03/2018.
 */

public class BusInfoFormat implements GoogleMap.InfoWindowAdapter, GoogleMap.OnMapClickListener, GoogleMap.OnInfoWindowClickListener {
    private HashMap<Marker, BusStop> markers;
    private NavigationDrawer context;

    public BusInfoFormat(HashMap<Marker, BusStop> markers, NavigationDrawer c){
        this.markers = markers;
        this.context = c;
    }
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View v = context.getLayoutInflater().inflate(R.layout.windowlayout, null);

        // Getting the position from the marker
        LatLng latLng = marker.getPosition();

        // Getting reference to the TextView to set latitude
        TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);

        // Getting reference to the TextView to set longitude
        TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);

        BusStop bs = markers.get(marker);

        // Setting the latitude
        tvLat.setText("Name:" + bs.name);

        // Setting the longitude
        tvLng.setText("Bus Stop ID:"+ bs.id);

        // Returning the view containing InfoWindow contents

        return v;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        for(Marker m : markers.keySet()){
            m.hideInfoWindow();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        context.showBusInfo(markers.get(marker));
    }
}
