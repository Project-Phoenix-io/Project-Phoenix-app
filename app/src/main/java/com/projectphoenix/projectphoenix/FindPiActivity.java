package com.projectphoenix.projectphoenix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

public class FindPiActivity extends AppCompatActivity {

    private TestPreviousIP mTestPreviousIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pi);

        mTestPreviousIP = new TestPreviousIP(this);
        mTestPreviousIP.execute((Void) null);
    }

    public class TestPreviousIP extends AsyncTask<Void, String, Boolean> {

        private final String mIP;
        private final TextView mCurrentIp;
        private final Context mContext;
        private final SharedPreferences mSharedPref;
        private FindPiActivity.FindPi mFindPi;

        TestPreviousIP (Context context) {
            mContext = context;
            mCurrentIp = (TextView) findViewById(R.id.currentIp);
            mSharedPref = context.getSharedPreferences("phoenix", Context.MODE_PRIVATE);
            mIP = mSharedPref.getString("ip", "");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(mIP != "") {
                URL url = null;
                HttpURLConnection conn = null;

                publishProgress("Trying previous successful IP: " + mIP);
                try {
                    url = new URL("http://" + mIP + ":5000/api");

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("GET");
                    conn.connect();

                    int responseCode = conn.getResponseCode();
                    System.out.println(responseCode);

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        conn.disconnect();
                        return true;
                    }
                } catch (IOException e) {
                    Log.e("Error", e.getMessage());
                    conn.disconnect();
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            TextView currentIp = (TextView) findViewById(R.id.currentIp);
            currentIp.setText(progress[0]);
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Intent intent = new Intent(FindPiActivity.this, LoginActivity.class);

            if(success) {
                FindPiActivity.this.startActivity(intent);
            } else {
                WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                int ip = wifiInfo.getIpAddress();
                byte[] ipAddress = BigInteger.valueOf(ip).toByteArray();

                try {
                    InetAddress myaddr = myaddr = InetAddress.getByAddress(ipAddress);
                    String hostaddr = Utils.flipIp(myaddr.getHostAddress());

                    mFindPi = new FindPiActivity.FindPi(hostaddr, mContext);
                    mFindPi.execute((Void) null);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class FindPi extends AsyncTask<Void, String, Boolean> {

        private final String mIP;
        private final TextView mCurrentIp;
        private Context mContext;
        private SharedPreferences mSharedPref;
        private SharedPreferences.Editor mEditor;

        FindPi (String ip, Context context) {
            mIP = ip;
            mCurrentIp = (TextView) findViewById(R.id.currentIp);
            mContext = context;
            mSharedPref = context.getSharedPreferences("phoenix", Context.MODE_PRIVATE);
            mEditor = mSharedPref.edit();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            URL url = null;
            HttpURLConnection conn = null;
            String[] host = mIP.split("\\.");

            for(int i = 2; i <= 254; i++) {
                publishProgress(host[0] + "." + host[1] + "." + host[2] + "." + i);
                if (i != Integer.parseInt(host[3])) {
                    try {
                        url = new URL("http://" + host[0] + "." + host[1] + "." + host[2] + "." + i + ":5000/api");

                        conn = (HttpURLConnection) url.openConnection();
                        conn.setReadTimeout(1000);
                        conn.setConnectTimeout(1000);
                        conn.setRequestMethod("GET");
                        conn.connect();

                        int responseCode = conn.getResponseCode();
                        System.out.println(responseCode);

                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            conn.disconnect();
                            mEditor.putString("ip", host[0] + "." + host[1] + "." + host[2] + "." + i);
                            mEditor.apply();
                            return true;
                        }
                    } catch (IOException e) {
                        Log.e("Error", e.getMessage());
                        conn.disconnect();
                    }
                }
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            TextView currentIp = (TextView) findViewById(R.id.currentIp);
            currentIp.setText(progress[0]);
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Intent intent = new Intent(FindPiActivity.this, LoginActivity.class);

            if(success) {
                FindPiActivity.this.startActivity(intent);
            } else {
                //TODO: Do something when we don't find the Pi
            }
        }
    }
}
