package meme.wheresthebus;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import meme.wheresthebus.comms.BusRoutes;
import meme.wheresthebus.comms.BusStop;
import meme.wheresthebus.comms.BusStopInfo;
import meme.wheresthebus.comms.BusStops;
import meme.wheresthebus.comms.ParameterStringBuilder;
import meme.wheresthebus.location.LocationService;

import static android.content.Intent.ACTION_GET_CONTENT;

public class NavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap gmap;
    private LocationService location;
    private Boolean locationBound;
    private ServiceConnection locationConnection;
    private Boolean onLocation;
    private ArrayDeque<Integer> tabHistory;

    private HashMap<Marker, BusStop> markers;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_GET_CONTENT.equals(action) && onLocation) {
                double lat = intent.getDoubleExtra("lat", 999);
                double lng = intent.getDoubleExtra("lng", 999);

                centreMapWithBusStops(new LatLng(lat, lng), 16);
                onLocation = false;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        markers = new HashMap<>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        LocalBroadcastManager lmb = LocalBroadcastManager.getInstance(this);
        lmb.registerReceiver(broadcastReceiver, new IntentFilter(ACTION_GET_CONTENT));

        showMap();

        onLocation = true;

        //start location service
        new Thread(() -> startService(new Intent(this, LocationService.class))).start();

        //init tabs
        TabHost tabHost = (TabHost) findViewById(R.id.tab_host);
        tabHost.setup();

        //add map tab
        TabHost.TabSpec spec = tabHost.newTabSpec("Map");
        spec.setContent(R.id.page);
        spec.setIndicator("Map");
        tabHost.addTab(spec);

        //add routes tab
        spec = tabHost.newTabSpec("Live Times");
        spec.setContent(R.id.routes);
        spec.setIndicator("Live Times");
        tabHost.addTab(spec);

        tabHistory = new ArrayDeque<>();

        //pull tab ids
        LinearLayout map = findViewById(R.id.page);
        LinearLayout liveTimes = findViewById(R.id.routes);

        //set tab animations
        tabAnimations(tabHost, map, liveTimes);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    private void tabAnimations(TabHost tabHost, LinearLayout tab1Layout, LinearLayout tab2Layout){
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            public void onTabChanged(String tabId) {
                tab1Layout.setAnimation(outToLeftAnimation());
                tab2Layout.setAnimation(outToLeftAnimation());
            }
        });
    }

    public Animation inFromRightAnimation() {

        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(500);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    public Animation outToLeftAnimation() {
        Animation outtoLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoLeft.setDuration(500);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
        return outtoLeft;
    }

    private void initLocationConnection() {
        locationConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                location = ((LocationService.LocalBinder) service).getService();

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
    }

    void doUnbindService() {
        if (locationBound) {
            unbindService(locationConnection);
            locationBound = false;
        }
    }

    private void showMap() {
        MapFragment map = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.page, map);
        fragmentTransaction.commit();
        map.getMapAsync(this);
    }

    public void getLimits() {
        CameraPosition cp = gmap.getCameraPosition();
        LatLng position = cp.target;
        System.out.println(position.latitude + " " + position.longitude);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            if(tabHistory.size()==1){
                this.setTitle(getString(R.string.app_name));
            }

            if(tabHistory.isEmpty()) {
                super.onBackPressed();
            } else {


                TabHost tabs = findViewById(R.id.tab_host);
                tabs.setCurrentTab(tabHistory.pop());



            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            TabHost host = findViewById(R.id.tab_host);
            if(host.getCurrentTab() != 0)
            tabHistory.add(host.getCurrentTab());
            host.setCurrentTab(0);
            this.setTitle(getString(R.string.app_name));
        } else if (id == R.id.nav_settings) {


        } else if (id == R.id.nav_share) {
            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Where's the bus?");
                String sAux = "\nLet me recommend you this super cool app\n\n";
                sAux = sAux + "https://github.com/CampusHackTeamMeme/WheresTheBus/ \n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "choose one"));
            } catch (Exception e) {
                //e.toString();
            }

        } else if (id == R.id.nav_send) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/CampusHackTeamMeme/WheresTheBus/issues/new"));
            startActivity(browserIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        BusInfoFormat bif = new BusInfoFormat(markers, this);
        gmap = googleMap;
        gmap.setInfoWindowAdapter(bif);
        gmap.setOnMapClickListener(bif);
        gmap.setOnInfoWindowClickListener(bif);

        //set current location pointer
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        gmap.setMyLocationEnabled(true);

        //get location
        setMapCameraToSoton(gmap);
        gmap.setOnMapLoadedCallback(this);
    }

    private void centreMap(LatLng position){
        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    position, 16));

    }

    private void centreMapWithBusStops(LatLng position, float zoom){
        centreMap(position);
        addBusStops(position, zoom);
    }

    public void setMapCameraToSoton(GoogleMap gmap){
        //set map camera to soton
        LatLng soton = new LatLng(50.928834, -1.400735);
        setMapCamera(gmap,soton,13);
    }

    public void setMapCamera(GoogleMap gmap, LatLng centre, float zoom){
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(centre, zoom));

    }

    public void addBusStops(LatLng position, float zoom){
        this.runOnUiThread(() -> addStopsAsync(gmap, position, zoom));
    }

    private void addStopsAsync(GoogleMap gmap, LatLng position, double zoom){
        try {
            ArrayDeque<BusStop> stops = new BusStops().execute(position.latitude, position.longitude, zoom).get();
            for(BusStop b : stops){
                //System.out.println(b.name + " " + b.position.longitude + " " + b.position.latitude);
                MarkerOptions mo = new MarkerOptions().position(b.position).title(b.name);
                markers.put(gmap.addMarker(mo), b);
            }
        } catch (ExecutionException | InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onMapLoaded() {
        //addBusStops(gmap.getCameraPosition().target,gmap.getCameraPosition().zoom);
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    public void showBusInfo(BusStop bs){
        TabHost tabs = findViewById(R.id.tab_host);
        tabHistory.push(tabs.getCurrentTab());
        tabs.setCurrentTabByTag("Live Times");

        //build current tab
        buildBusStopInfo(bs);
    }

    //@RequiresApi(api = Build.VERSION_CODES.O)
    private void buildBusStopInfo(BusStop bs){
        //get info
        try {
            HashMap<String, BusStopInfo> busStopInfo = new BusRoutes().execute(bs).get();
            bs.addInfo(busStopInfo.get(bs.id));
        } catch(Exception e){
            return;
        }
        //set name
        TextView name = new TextView(this);
        this.setTitle(bs.name);
        //add bus routes
        GridLayout layout = findViewById(R.id.busStopInfo);
        layout.addView(name, new GridLayout.LayoutParams());
        layout.removeAllViews();
        for(String bus : bs.info.services){
            Button stop = new Button(this);

            stop.setText(bus);
            layout.addView(stop);
        }

    }
}
