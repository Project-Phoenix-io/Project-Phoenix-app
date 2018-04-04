package com.projectphoenix.projectphoenix;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

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
        if(geocoder != null) {
            try {
                List<Address> addresses = geocoder.getFromLocationName(location, 5);
                return addresses;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
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
                URL url = new URL("http://" + ip + ":5000/api/activate");

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
                URL url = new URL("http://" + ip + ":5000/api/shutdown");

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
                conn.setReadTimeout(500 /* milliseconds */);
                conn.setConnectTimeout(500 /* milliseconds */);
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
            }
            return null;
        }

//        @Override
//        protected Boolean onPostExecute(Boolean success) {
//            return true;
//        }
    }

    private static String getPostDataString() throws Exception {
        return "timeOn=" + URLEncoder.encode("1", "UTF-8") + "&timeOff=" + URLEncoder.encode("1", "UTF-8") + "&iterations=" + URLEncoder.encode("2", "UTF-8");
    }

}
