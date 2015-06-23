package apptastic.getpekt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import helper.SQLiteHandler;
import helper.SessionManager;

/**
 * Activity which asks you to add your phone number at your first login (for ease of finding
 * contacts).
 * @author AppTastic
 */
public class PhoneActivity extends ActionBarActivity {

    public static String URL = "https://vps1.insertsoft.nl/getpekt/details/";
    private SQLiteHandler user_db;
    private SessionManager session;

    private Typeface express;

    private String countryCode;
    private String phoneNumber;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        overridePendingTransition(0, 0);

        FacebookSdk.sdkInitialize(getApplicationContext());

        //Check whether the user is logged in
        user_db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        if (AccessToken.getCurrentAccessToken() == null && !session.isLoggedIn()) {
            logoutUser();
        }

        //Set the ProgressDialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        express = Typeface.createFromAsset(getAssets(), "expressway.ttf");

        //Set the ActionBar
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.actionbar_main, null);

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

        TextView txt = (TextView) findViewById(R.id.title_text);
        txt.setTypeface(express);
        txt.setTextSize(getResources().getDimension(R.dimen.button_text));
        txt.setText(R.string.title_activity_phone);


        //Listen to the Submit Button
        Button submit = (Button) findViewById(R.id.btnPhone);
        final EditText country = (EditText) findViewById(R.id.country);
        final EditText phone = (EditText) findViewById(R.id.phone);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countryCode = country.getText().toString();
                phoneNumber = phone.getText().toString();
                //Check whether input is valid (no extensive check, just length, possible to enter fake number)
                if (countryCode.length() > 4 || countryCode.length() == 0 || phoneNumber.length() < 4){
                    Toast.makeText(getApplicationContext(),
                            "please enter a valid phone number", Toast.LENGTH_LONG).show();
                }
                else {
                    countryCode = '+' + countryCode;
                    phoneNumber = countryCode + phoneNumber;

                    String tag_string_req = "req_phone";

                    pDialog.setMessage("Registering number ...");
                    showDialog();

                    StringRequest strReq = new StringRequest(Request.Method.POST,
                            URL, new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {

                            try {
                                hideDialog();
                                JSONObject jObj = new JSONObject(response);
                                boolean error = jObj.getBoolean("error");
                                if (!error) {
                                    Intent intent = new Intent(PhoneActivity.this, MainActivity.class);
                                    PhoneActivity.this.startActivity(intent);
                                    finish();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "number already in use", Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "number already in use", Toast.LENGTH_LONG).show();
                            }

                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            hideDialog();
                            Toast.makeText(getApplicationContext(),
                                    "something went wrong", Toast.LENGTH_LONG).show();
                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() {
                            // Posting params to register url
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("tag", "create");
                            params.put("uid", user_db.getUserDetails().get("email"));
                            params.put("country", countryCode);
                            params.put("phone", phoneNumber);

                            return params;
                        }

                    };

                    // Adding request to request queue
                    AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
                }
            }
        });

    }

    //Logout the user
    private void logoutUser() {
        session.setLogin(false);

        user_db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(PhoneActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    //Logout the user on double back tap (within 2 seconds)
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    public void onBackPressed()
    {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            logoutUser();
        }
        else { Toast.makeText(getBaseContext(), "Tap back button again to logout", Toast.LENGTH_SHORT).show(); }

        mBackPressed = System.currentTimeMillis();
    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }


    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}