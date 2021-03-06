package com.projectphoenix.projectphoenix;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap gmap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private GetFireActivity getFireActivity;
    private List<String[]> fireList;
    private static ToggleButton pulseToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        ArcGISMap map = new ArcGISMap(Basemap.createImagery());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        ToggleButton activateDeactivate = (ToggleButton) findViewById(R.id.systemActivate);
        activateDeactivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    Utils.ActivateTask taskActivate = new Utils.ActivateTask(MainActivity.this);
                    taskActivate.execute();
                } else {
                    Utils.DeactivateTask taskDeactivate = new Utils.DeactivateTask(MainActivity.this);
                    taskDeactivate.execute();
                }
            }
        });

        pulseToggle = (ToggleButton) findViewById(R.id.pulseToggle);
        pulseToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    PulseTask taskPulse = new PulseTask(MainActivity.this);
                    taskPulse.execute();
                }
            }
        });

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        TextView mapErrorView = (TextView) findViewById(R.id.mapError);
        MapView mapView = (MapView) findViewById(R.id.mapView);
        gmap = googleMap;
        gmap.setMinZoomPreference(10);
        List<Address> aList = Utils.getCoords(this, "11317 Mercury Ct, Mira Loma, CA 91752");
        if(aList != null) {
            Address address = aList.get(0);
            LatLng home = new LatLng(address.getLatitude(), address.getLongitude());
            gmap.moveCamera(CameraUpdateFactory.newLatLng(home));
//            gmap.addMarker(new MarkerOptions().position(home).title("Home"));
            ArcGISMap arcMap = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, home.latitude, home.longitude, 16);

//            getFireActivity = new GetFireActivity("https://firms.modaps.eosdis.nasa.gov/active_fire/viirs/text/VNP14IMGTDL_NRT_USA_contiguous_and_Hawaii_24h.csv", gmap);
//            getFireActivity.execute();
//            try {
//                fireList = getFireActivity.get();
//                String[] row;
//                for (int i = 1; i < fireList.size(); i++) {
//                    row = (String[]) fireList.get(i);
//                    gmap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(row[0]), Double.valueOf(row[1]))).title(row[5] + " at " + row[6]));
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
        } else {
            mapErrorView.setVisibility(View.VISIBLE);
            mapView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public class GetFireActivity extends AsyncTask<Void, Void, List<String[]>> {
        private String mURL;
        private InputStream iStream;
        private GoogleMap mGmap;

        public GetFireActivity(String url, GoogleMap gmap) {
            mURL = url;
            mGmap = gmap;
        }

        @Override
        protected List<String[]> doInBackground(Void... voids) {
            List<String[]> fileList;
            try {
                URL url = new URL(mURL);
                URLConnection connection = url.openConnection();
                connection.connect();

                iStream = new BufferedInputStream(url.openStream());
                fileList = Utils.CVStoList(iStream);

            } catch (IOException e) {
                Log.e("GetFireActivity", e.getMessage());
                return null;
            }
            return fileList;
        }

        @Override
        protected void onPostExecute(final List<String[]> list) {
            try {
                iStream.close();
            } catch (IOException e) {
                Log.e("GetFireActivity", e.getMessage());
            }
        }


    }


    public static class PulseTask extends AsyncTask<Void, Void, Void> {

        private final Context mContext;

        public PulseTask(Context context) { mContext = context;}

        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences sharedPref = mContext.getSharedPreferences("phoenix", Context.MODE_PRIVATE);
            String ip = sharedPref.getString("ip", "");
            try {
                URL url = new URL("http://" + ip + ":5000/api/pulse");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(50000 /* milliseconds */);
                conn.setConnectTimeout(50000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString());
                writer.flush();
                writer.close();
                os.close();

                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    conn.disconnect();
                }

            } catch (Exception e) {
                e.printStackTrace();
                pulseToggle.setChecked(false);
            }
            return null;
        }

    }

    private static String getPostDataString() throws Exception {
        return "timeOn=" + URLEncoder.encode("1", "UTF-8") + "&timeOff=" + URLEncoder.encode("1", "UTF-8") + "&iterations=" + URLEncoder.encode("2", "UTF-8");
    }
}
