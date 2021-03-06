package meme.wheresthebus;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
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
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import meme.wheresthebus.comms.data.BusRoute;
import meme.wheresthebus.comms.request.BusRouteRequest;
import meme.wheresthebus.comms.request.BusStopInfoRequest;
import meme.wheresthebus.comms.data.BusStop;
import meme.wheresthebus.comms.data.BusStopInfo;
import meme.wheresthebus.comms.request.BusStopRequest;
import meme.wheresthebus.comms.request.ParameterStringBuilder;
import meme.wheresthebus.comms.request.TimetableRequest;
import meme.wheresthebus.location.LocationService;
import meme.wheresthebus.notify.Reminder;

import static android.app.Notification.EXTRA_CHANNEL_ID;
import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static android.content.Intent.ACTION_GET_CONTENT;

public class NavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap gmap;
    private LocationService location;
    private Boolean locationBound;
    private ServiceConnection locationConnection;
    private Boolean onLocation;
    private ArrayDeque<Integer> tabHistory;

    private HashSet<BusStop> loaded;
    private HashMap<Marker, BusStop> markers;

    private Intent locationService;
    private Intent reminder;


    private void scheduleNotification(Notification notification, int delay) {

        Intent notificationIntent = new Intent(this, Reminder.class);
        notificationIntent.putExtra(Reminder.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(Reminder.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

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
        loaded = new HashSet<>();

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

        locationService = new Intent(this, LocationService.class);
        if(!isMyServiceRunning(Reminder.class)){
            reminder = new Intent(this, Reminder.class);
            new Thread(() -> startService(reminder)).start();
        }


        //start location service
        new Thread(() -> startService(locationService)).start();

        //init tabs
        TabHost tabHost = (TabHost) findViewById(R.id.tab_host);
        tabHost.setup();

        //add map tab
        TabHost.TabSpec spec = tabHost.newTabSpec("Map");
        spec.setContent(R.id.page);
        spec.setIndicator("Map");
        tabHost.addTab(spec);

        //add live times tab
        spec = tabHost.newTabSpec("Live Times");
        spec.setContent(R.id.live_times);
        spec.setIndicator("Live Times");
        tabHost.addTab(spec);

        //add route tab
        spec = tabHost.newTabSpec("Route");
        spec.setContent(R.id.bus_route);
        spec.setIndicator("Route");
        tabHost.addTab(spec);

        tabHistory = new ArrayDeque<>();

        //pull tab ids
        LinearLayout map = findViewById(R.id.page);
        LinearLayout liveTimes = findViewById(R.id.live_times);

        //set tab animations
        tabAnimations(tabHost, map, liveTimes);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        stopService(locationService);
        super.onDestroy();
    }

    private void tabAnimations(TabHost tabHost, LinearLayout tab1Layout, LinearLayout tab2Layout){
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

            public void onTabChanged(String tabId) {
                tab1Layout.setAnimation(inFromRightAnimation());
                //tab2Layout.setAnimation(inFromRightAnimation());
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

    public void showMap() {
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
        gmap.setMaxZoomPreference(16);
        gmap.setMinZoomPreference(12);
        gmap.setOnCameraMoveListener(new MapMoveListener(gmap,this));

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

    public void addStopsAsync(GoogleMap gmap, LatLng position, double zoom){
        try {
            ArrayDeque<BusStop> stops = new BusStopRequest().execute(position.latitude, position.longitude, zoom).get();
            for(BusStop b : stops){
                if(loaded.add(b)) {
                    MarkerOptions mo = new MarkerOptions().position(b.position).title(b.name);
                    markers.put(gmap.addMarker(mo), b);
                }
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
            HashMap<String, ArrayList<String>> timetable = new TimetableRequest().execute(bs).get();
            System.out.print(timetable.keySet());

            HashMap<String, BusStopInfo> busStopInfo = new BusStopInfoRequest().execute(bs).get();
            bs.addInfo(busStopInfo.get(bs.id));
            times = new TimetableRequest().execute(bs).get();
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
            stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadRoute((String) ((Button) v).getText());
                }
            });
            stop.setText(ParameterStringBuilder.formatOperator(bus));
            layout.addView(stop);
            String textTimes = "";
            if(times.get(bus) != null){
                for(String s : times.get(bs.id)){
                    textTimes += s + " ";
                }
            } else {
                textTimes = "";
            }
            TextView tv = new TextView(this);
            tv.setText(textTimes);
            //layout.addView(tv);
        }

    }

    private void loadRoute(String routeName){
        try {
            AsyncTask<String, Void, BusRoute> route = new BusRouteRequest().execute(routeName);
            viewRouteTab(route);
        } catch(Exception e){
            return;
        }
    }

    private void viewRouteTab(AsyncTask<String, Void, BusRoute> route){
        TabHost tabs = findViewById(R.id.tab_host);
        tabHistory.push(tabs.getCurrentTab());
        tabs.setCurrentTabByTag("Route");

        MapFragment map = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.bus_route, map);
        fragmentTransaction.commit();
        map.getMapAsync(new RouteBuilder(route));
    }


    private void setReminder(String title){
        Intent snoozeIntent = new Intent(this, BroadcastReceiver.class);
        //snoozeIntent.setAction(ACTION_SNOOZE);
        snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(this, 0, snoozeIntent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, EXTRA_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_menu_gallery)
                .setContentTitle("Survey")
                .setContentText(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_menu_gallery, "Yes",
                        (snoozePendingIntent));

        scheduleNotification(mBuilder.getNotification(), 60     000);
    }

    public class RouteBuilder implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {
        GoogleMap gmap;
        AsyncTask<String, Void, BusRoute> route;


        public RouteBuilder(AsyncTask<String, Void, BusRoute> route){
            this.route = route;
        }

        @Override
        public void onMapLoaded() {
            try {
                BusRoute br = route.get();
                NavigationDrawer.this.setTitle(br.name);
                PolylineOptions options = new PolylineOptions().addAll(br.route);

                gmap.addPolyline(options);

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMapReady(GoogleMap gmap) {
            this.gmap = gmap;
            setMapCameraToSoton(gmap);
            gmap.clear();

            gmap.setOnMapLoadedCallback(this);
        }



    }
}
