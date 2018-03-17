package com.projectphoenix.projectphoenix;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static android.Manifest.permission.READ_CONTACTS;

public class Login extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final int REQUEST_READ_CONTACTS = 0;
    private TestPreviousIP mTestPreviousIP = null;
    private UserLoginTask mAuthTask = null;
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mTestPreviousIP = new TestPreviousIP(this);
        mTestPreviousIP.execute((Void) null);

        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUsernameSigninButton = (Button) findViewById(R.id.sign_in_button);
        mUsernameSigninButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private String flipIp(String ip) {
        String[] split = ip.split("\\.");
        return split[3] + "." + split[2] + "." + split[1] + "." + split[0];
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mUsernameView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password, this);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUsernameValid(String username) {
        return username.length() >= 4;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 4;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(Login.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUsernameView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public class TestPreviousIP extends AsyncTask<Void, String, Boolean> {

        private final String mIP;
        private final TextView mCurrentIp;
        private final Context mContext;
        private final SharedPreferences mSharedPref;
        //        private final ConstraintLayout mConnectingLayout;
        private FindPi mFindPi;

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
            ConstraintLayout errorLayout = (ConstraintLayout) findViewById(R.id.connectingLayout);
            ConstraintLayout connectingLayout = (ConstraintLayout) findViewById(R.id.connectingLayout);
            ScrollView loginForm = (ScrollView) findViewById(R.id.login_form);
            TextView errorTextView = (TextView) findViewById(R.id.errorTextView);

            if(success) {
                connectingLayout.setVisibility(View.GONE);
                loginForm.setVisibility(View.VISIBLE);
            } else {
                WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                int ip = wifiInfo.getIpAddress();
                byte[] ipAddress = BigInteger.valueOf(ip).toByteArray();
                InetAddress myaddr = null;
                String hostaddr = null;

                try {
                    myaddr = InetAddress.getByAddress(ipAddress);
                    hostaddr  = flipIp(myaddr.getHostAddress());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

                mFindPi = new FindPi(hostaddr, mContext);
                mFindPi.execute((Void) null);
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
            ConstraintLayout errorLayout = (ConstraintLayout) findViewById(R.id.connectingLayout);
            ConstraintLayout connectingLayout = (ConstraintLayout) findViewById(R.id.connectingLayout);
            ScrollView loginForm = (ScrollView) findViewById(R.id.login_form);
            TextView errorTextView = (TextView) findViewById(R.id.errorTextView);

            connectingLayout.setVisibility(View.GONE);

            if(success) {
                connectingLayout.setVisibility(View.GONE);
                loginForm.setVisibility(View.VISIBLE);
            } else {
                errorTextView.setText("Could not connect to local Phoenix System!");
                errorLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        private final Context mContext;

        UserLoginTask(String username, String password, Context context) {
            mUsername = username;
            mPassword = password;
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                SharedPreferences sharedPref = mContext.getSharedPreferences("phoenix", Context.MODE_PRIVATE);
                String ip = sharedPref.getString("ip", "");
                URL url = new URL("http://" + ip + ":5000/api/login");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(mUsername, mPassword));
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("Login", String.valueOf(responseCode));

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return true;

                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Intent intent = new Intent(Login.this, MainActivity.class);
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Login.this.startActivity(intent);
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public String getPostDataString(String username, String password) throws Exception {
        return "username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");
    }
}

