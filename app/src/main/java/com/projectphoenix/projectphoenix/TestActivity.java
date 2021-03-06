package com.projectphoenix.projectphoenix;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class TestActivity extends AppCompatActivity {
    AvctivateTask activateTask = null;
    DeactivateTask deactivateTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        final Button activate = (Button) findViewById(R.id.activateButton);
        final Button deactivate = (Button) findViewById(R.id.deactivateButton);

        activate.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                TestActivity.this.activateTask = new AvctivateTask(TestActivity.this);
                TestActivity.this.activateTask.execute((Void) null);
            }
        });

        deactivate.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                TestActivity.this.deactivateTask = new DeactivateTask(TestActivity.this);
                TestActivity.this.deactivateTask.execute((Void) null);
            }
        });
        
    }

    public class AvctivateTask extends AsyncTask<Void, Void, Boolean> {

        private final Context mContext;
        private final Button activateButton = (Button) findViewById(R.id.activateButton);
        private final Button deactivateButton = (Button) findViewById(R.id.deactivateButton);

        AvctivateTask(Context context) {
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            SharedPreferences sharedPref = mContext.getSharedPreferences("phoenix", Context.MODE_PRIVATE);
            String ip = sharedPref.getString("ip", "");
            try {
                URL url = new URL("http://" + ip + ":5000/api/extinguish");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);

                int responseCode = conn.getResponseCode();
                Log.d("Activate", String.valueOf(responseCode));
                activateButton.setVisibility(View.GONE);
                deactivateButton.setVisibility(View.VISIBLE);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class DeactivateTask extends AsyncTask<Void, Void, Boolean> {

        private final Context mContext;
        private final Button activateButton = (Button) findViewById(R.id.activateButton);
        private final Button deactivateButton = (Button) findViewById(R.id.deactivateButton);

        DeactivateTask(Context context) {
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            SharedPreferences sharedPref = mContext.getSharedPreferences("phoenix", Context.MODE_PRIVATE);
            String ip = sharedPref.getString("ip", "");
            try {
                URL url = new URL("http://" + ip + ":5000/api/stop");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);

                int responseCode = conn.getResponseCode();
                Log.d("Deactivate", String.valueOf(responseCode));
                activateButton.setVisibility(View.VISIBLE);
                deactivateButton.setVisibility(View.GONE);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
