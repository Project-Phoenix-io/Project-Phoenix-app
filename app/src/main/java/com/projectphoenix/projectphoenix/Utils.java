package com.projectphoenix.projectphoenix;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by KooTG on 3/20/2018.
 */

public class Utils {

    public static String flipIp(String ip) {
        String[] split = ip.split("\\.");
        return split[3] + "." + split[2] + "." + split[1] + "." + split[0];
    }

    public static List<Address> getCoords(Context context, String location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 5);
            return addresses;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List CVStoList (InputStream iStream) {
        List<String[]> resultList = new ArrayList();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
            String cvsLine;
            while((cvsLine = reader.readLine()) != null) {
                String[] row = cvsLine.split(",");
                resultList.add(row);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("Error reading CVS file: " + e);
        }
        return resultList;
    }

    public static class ActivateTask extends AsyncTask<Void, Void, Void> {

        private final Context mContext;

        public ActivateTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences sharedPref = mContext.getSharedPreferences("phoenix", Context.MODE_PRIVATE);
            String ip = sharedPref.getString("ip", "");
            try {
                URL url = new URL("http://" + ip + ":5000/api/extinguish");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);

                int responseCode = conn.getResponseCode();
                Log.d("Activate", String.valueOf(responseCode));

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class DeactivateTask extends AsyncTask<Void, Void, Void> {

        private final Context mContext;

        public DeactivateTask(Context context) { mContext = context; }

        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences sharedPref = mContext.getSharedPreferences("phoenix", Context.MODE_PRIVATE);
            String ip = sharedPref.getString("ip", "");
            try {
                URL url = new URL("http://" + ip + ":5000/api/stop");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);

                int responseCode = conn.getResponseCode();
                Log.d("Deactivate", String.valueOf(responseCode));

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
