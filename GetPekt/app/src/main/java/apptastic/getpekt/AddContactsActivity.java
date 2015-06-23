package apptastic.getpekt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import helper.SQLiteHandler;
import helper.SessionManager;


/**
 * This class lets you add contacts who also have the app to your events.
 * @author AppTastic
 */
public class AddContactsActivity extends ActionBarActivity {

    public static String URL_CREATE = "https://vps1.insertsoft.nl/getpekt/events/";
    public static String URL_ICON = "https://vps1.insertsoft.nl/getpekt/icons/";
    private SQLiteHandler user_db;
    private SessionManager session;

    private Typeface express;

    private RelativeLayout layout;
    private int height;

    private List<String> checked;
    private ProgressDialog pDialog;

    private String eName;
    private String eIcon;
    private String event;
    private String eid;
    private boolean first;
    private List<String> participants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);
        overridePendingTransition(0,0);


        //Login Check
        FacebookSdk.sdkInitialize(getApplicationContext());

        user_db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        if (AccessToken.getCurrentAccessToken() == null && !session.isLoggedIn()) {
            logoutUser();
        }

        //Get the Extras given by the previous activity
        Bundle b = getIntent().getExtras();
        eName = b.getString("eName");
        eIcon = b.getString("eIcon");
        event = b.getString("event");
        eid = b.getString("eid");
        first = b.getBoolean("first");
        if (!first){
            participants = b.getStringArrayList("participants");
        }


        //Set the layout, pDialog, font and item height
        layout = (RelativeLayout) findViewById(R.id.layout);
        height = getResources().getDimensionPixelSize(R.dimen.item_height);
        express = Typeface.createFromAsset(getAssets(), "expressway.ttf");

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

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
        txt.setText(R.string.title_activity_add_contacts);

        //Set the ActionBar back button
        final ImageButton imgButton = (ImageButton) findViewById(R.id.menu);
        imgButton.setImageResource(R.drawable.previous);

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0,0);
            }
        });

        //Listen to the Save Button
        Button create = (Button) findViewById(R.id.btnSave);
        if (!first)
            create.setText("Add Participants");

        //OnClickListener: Adds the participants to the online database using the volley library
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (first)
                    pDialog.setMessage("Creating Event...");
                else
                    pDialog.setMessage("Adding Participants...");
                showDialog();

                for (final String uid : checked) {
                    String tag_string_req = "req_create";

                    StringRequest strReq = new StringRequest(Request.Method.POST,
                            URL_CREATE, new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            if(!first){
                                hideDialog();
                            }
                            try {
                                JSONObject jObj = new JSONObject(response);
                                boolean error = jObj.getBoolean("error");

                                if (!error) {
                                    if (!first){
                                        overridePendingTransition(0, 0);
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(getBaseContext(),
                                            "Something went wrong", Toast.LENGTH_LONG).show();

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(),
                                    "Something went wrong", Toast.LENGTH_LONG).show();
                            if (!first){
                                hideDialog();
                            }
                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("tag", "create");
                            params.put("uid", uid);
                            params.put("name", eName);
                            params.put("icon", eIcon);
                            params.put("event", event);
                            params.put("creator", "0");
                            params.put("eid", eid);
                            return params;
                        }

                    };
                    AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
                }

                //If you got here through the createactivity it is possible you have set an icon,
                //which of course has to be uploaded to the database (again, using volley).
                if (first) {
                    if (Integer.parseInt(eIcon) != 0) {
                        String tag_icon = "req_icon";
                        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                                URL_ICON, new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jObj = new JSONObject(response);
                                    boolean error = jObj.getBoolean("error");

                                    if (!error) {
                                        create_owner();
                                    } else {
                                        Toast.makeText(getBaseContext(),
                                                "Something went wrong", Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(),
                                        "Something went wrong", Toast.LENGTH_LONG).show();
                            }
                        }) {

                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("tag", "create");
                                params.put("eid", eid);
                                params.put("blob",Base64.encodeToString(user_db.getBlob(eid), Base64.DEFAULT));
                                return params;
                            }

                        };
                        AppController.getInstance().addToRequestQueue(stringRequest, tag_icon);
                    } else {
                        create_owner();
                    }
                }
            }
        });
        drawContacts();
    }


    /**
     * Draws all the contacts that are not already in the event.
     */
    public void drawContacts(){

        checked = new ArrayList<String>();
        List<String> uids = user_db.getUIDs();
        int counter = 0;

        //Filter out the contacts already in the event
        if (!first) {
            for (String participant : participants){
                if (uids.contains(participant)){
                    uids.remove(participant);
                }
            }
        }

        //Draws every single contact
        for (final String uid : uids) {
            RelativeLayout.LayoutParams lp_checkbox = new RelativeLayout.LayoutParams(height, height);
            RelativeLayout.LayoutParams lp_contact = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
            //add the checkbox
            lp_checkbox.setMargins(10, height * counter, 0, 0);
            final ImageButton checkBox = new ImageButton(this);
            checkBox.setImageResource(R.drawable.unchecked);
            checkBox.setLayoutParams(lp_checkbox);
            checkBox.setScaleType(ImageView.ScaleType.FIT_CENTER);
            checkBox.setPadding(5, 5, 5, 5);
            checkBox.setMaxWidth(height);
            checkBox.setMaxHeight(height);
            checkBox.setBackground(null);
            layout.addView(checkBox);

            //Checkbox which adds/removes contacts to the checked-List.
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checked.contains(uid)) {
                        checkBox.setImageResource(R.drawable.unchecked);
                        checked.remove(uid);
                    } else {
                        checkBox.setImageResource(R.drawable.checked);
                        checked.add(uid);
                    }
                }
            });

            //Add the contact
            String name = user_db.getName(uid);
            lp_contact.setMargins(height + 25, height * counter, 0, 0);
            TextView contact = new TextView(this);
            contact.setText(name);
            contact.setTypeface(express);
            contact.setTextSize(getResources().getDimension(R.dimen.button_text));
            contact.setSingleLine(true);
            contact.setTextColor(getResources().getColor(R.color.black));
            contact.setGravity(Gravity.CENTER_VERTICAL);
            contact.setLayoutParams(lp_contact);
            layout.addView(contact);

            counter++;
        }
    }

    /**
     * Creates You as the owner of the event in the database.
     */
    private void create_owner(){
        String tag_string_req = "req_create_owner";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_CREATE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        Intent intent = new Intent(AddContactsActivity.this, MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    } else {
                        Toast.makeText(getBaseContext(),
                                "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                Toast.makeText(getApplicationContext(),
                        "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "create");
                params.put("uid", user_db.getUserDetails().get("email"));
                params.put("name", eName);
                params.put("icon", eIcon);
                params.put("event", event);
                params.put("creator", "1");
                params.put("eid", eid);
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    /**
     * Logout the user
     */
    private void logoutUser() {
        session.setLogin(false);
        user_db.deleteUsers();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }


    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    /**
     * Finish activity on back press
     */
    @Override
    public void onBackPressed(){
        finish();
        overridePendingTransition(0,0);
    }
}