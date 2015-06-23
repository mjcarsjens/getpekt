package apptastic.getpekt;

import helper.SQLiteHandler;
import helper.SessionManager;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Main Activity: Shows your events
 * @author AppTastic
 */
public class MainActivity extends ActionBarActivity {

    //SELF-ADVERTISEMENT
    public static String URL = "https://vps1.insertsoft.nl/getpekt/events/";
    public static String CONTACTS_URL = "https://vps1.insertsoft.nl/getpekt/details/";
    public static String URL_ICON = "https://vps1.insertsoft.nl/getpekt/icons/";
    private SQLiteHandler user_db;
    private SessionManager session;

    private SlidingMenu menu;
    private List<JSONObject> events;
    private RelativeLayout layout;

    private int height;
    private int width;
    private int divHeight;
    private int dateWidth;

    private ImageButton imgButton;
    private Animation rotForward;
    private Animation rotBack;
    private ImageButton add;

    private Typeface lobster;
    private Typeface express;

    private ProgressDialog pDialog;

    private String country;
    private String ownphone;

    private boolean first;

    private String uid;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(0, 0);

        FacebookSdk.sdkInitialize(getApplicationContext());

        firstLogin();

        user_db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());
        layout = (RelativeLayout) findViewById(R.id.layout);

        // Set the size-parameters for the different elements of the layout
        height = getResources().getDimensionPixelSize(R.dimen.section_height);
        width = RelativeLayout.LayoutParams.MATCH_PARENT;
        dateWidth = getResources().getDimensionPixelSize(R.dimen.date_width);
        divHeight = getResources().getDimensionPixelSize(R.dimen.div_height);

        //Load the custom fonts
        lobster = Typeface.createFromAsset(getAssets(), "lobster.otf");
        express = Typeface.createFromAsset(getAssets(), "expressway.ttf");

        //Set the ProgressDialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        first = true;

        //Logout users that aren't supposed to be here
        if (AccessToken.getCurrentAccessToken() == null && !session.isLoggedIn() && !user_db.getUserDetails().isEmpty()) {
            logoutUser();
        }

        //Get UID
        uid = user_db.getUserDetails().get("email");

        //Set the ActionBar

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.actionbar_main, null);

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

        TextView txt = (TextView) findViewById(R.id.title_text);
        txt.setTypeface(lobster);
        txt.setText(R.string.app_name);

        //Set the menu-button animations
        imgButton = (ImageButton) findViewById(R.id.menu);
        imgButton.setImageResource(R.drawable.menu);
        rotForward = AnimationUtils.loadAnimation(this, R.anim.button_rotate);
        rotBack = AnimationUtils.loadAnimation(this, R.anim.button_rotate_back);

        //Set the Add Button
        add = (ImageButton) findViewById(R.id.add);
        add.setImageResource(R.drawable.add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateActivity.class);
                startActivity(intent);
            }
        });

        //Make the SlidingMenu
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setShadowDrawable(R.drawable.menu_shadow);
        menu.setShadowWidth(100);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.slidingmenu);
        menu.setSlidingEnabled(false);

        //Make the SlidingMenu show on click
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!menu.isMenuShowing()) {
                    imgButton.startAnimation(rotForward);
                    add.setVisibility(View.GONE);
                    imgButton.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imgButton.setImageResource(R.drawable.previous);
                        }
                    }, 100);
                } else {
                    imgButton.setImageResource(R.drawable.menu);
                    imgButton.startAnimation(rotBack);
                    add.setVisibility(View.VISIBLE);
                }
                menu.toggle();
            }
        });

        //Hide SlidingMenu
        Button events = (Button) findViewById(R.id.btnEvents);
        events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgButton.setImageResource(R.drawable.menu);
                imgButton.startAnimation(rotBack);
                add.setVisibility(View.VISIBLE);
                menu.toggle();
            }
        });

        //Go to Settings
        Button settings = (Button) findViewById(R.id.btnSettings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        getCountry();
    }


    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        user_db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    /**
     * Checks whether the users UID is in the details DB.
     * If not, starts the PhoneActivity.
     */
    public void firstLogin(){
        String tag_string_req = "req_phone";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                CONTACTS_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    Log.d("test", Boolean.toString(error));
                    if (error) {
                        Intent intent = new Intent(MainActivity.this, PhoneActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(),error.toString(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "check");
                params.put("uid", uid);

                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //Gets the Events
    public void getEvents(){
        String tag_string_req = "req_events";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        layout.removeAllViews();
                        events = new ArrayList<JSONObject>();

                        int counter = 0;
                        while(jObj.has(Integer.toString(counter))){
                            events.add(jObj.getJSONObject(Integer.toString(counter)));
                            counter++;
                        }
                        drawEvents();
                    } else {
                        String error_msg = jObj.getString("error_msg");
                        //Toast.makeText(getBaseContext(),error_msg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                //Toast.makeText(getApplicationContext(),"Something went wrong", Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "get");
                params.put("uid", uid);

                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }


    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //Draws the Events
    private void drawEvents(){

        int counter = 0;
        layout.removeAllViews();
        for (JSONObject j: events){
            try {
                final String eid = j.getString("eid");
                final String name = j.getString("name");
                final int icon = j.getInt("icon");
                final int creator = j.getInt("creator");
                String update = j.getString("updated");
                final String temp = j.getString("datetime");
                String month = temp.substring(5, 7);
                String day = temp.substring(8, 10);
                String date = day + '-' + month;

                RelativeLayout.LayoutParams lp_icon = new RelativeLayout.LayoutParams(height, height);
                RelativeLayout.LayoutParams lp_button = new RelativeLayout.LayoutParams(width, height);
                RelativeLayout.LayoutParams lp_date = new RelativeLayout.LayoutParams(dateWidth, height);
                RelativeLayout.LayoutParams lp_div = new RelativeLayout.LayoutParams(width, divHeight);

                /**
                 * Adds the icon: to limit the use of data we store the images in a local database, and
                 * in an online database. Whenever someone uploads a new icon a new random icon_ID gets generated
                 * when the icon_ID in your local DB doesn't equal the one in the online database, the one in the
                 * online DB gets downloaded and the offline DB gets updated with this EID and image.
                 */
                lp_icon.setMargins(0, (height + divHeight) * counter, 0, 0);
                final ImageButton img = new ImageButton(this);
                if (icon == 0) {
                    img.setImageResource(R.drawable.group);
                    img.setPadding(12,12,12,12);
                    user_db.deleteBlob(eid);
                }
                else {
                    if (icon == user_db.getVersion(eid)){
                        byte[] image = user_db.getBlob(eid);
                        img.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
                        img.setPadding(7,7,7,7);
                    } else {
                        String tag_icon = "req_icon";
                        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                                URL_ICON, new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jObj = new JSONObject(response);
                                    boolean error = jObj.getBoolean("error");

                                    if (!error) {
                                        byte[] blob = Base64.decode(jObj.getString("blob"), Base64.DEFAULT);
                                        if (user_db.getVersion(eid) == 0){
                                            user_db.addBLOB(eid, icon, blob);
                                        } else {
                                            user_db.updateBlob(eid, icon, blob);
                                        }
                                        img.setImageBitmap(BitmapFactory.decodeByteArray(blob, 0, blob.length));
                                        img.setPadding(7,7,7,7);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }) {

                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("tag", "get");
                                params.put("eid", eid);
                                return params;
                            }

                        };
                        AppController.getInstance().addToRequestQueue(stringRequest, tag_icon);
                    }
                }
                img.setLayoutParams(lp_icon);
                img.setBackground(null);
                img.setScaleType(ImageView.ScaleType.FIT_CENTER);
                layout.addView(img);

                //Add the button
                lp_button.setMargins(height, (height + divHeight) * counter, height, 0);
                TextView button = new TextView(this);
                button.setText(name);
                button.setTypeface(express);
                button.setTextSize(getResources().getDimension(R.dimen.button_text));
                button.setTextColor(getResources().getColor(R.color.black));
                button.setGravity(Gravity.CENTER_VERTICAL);
                button.setLayoutParams(lp_button);
                layout.addView(button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, EventActivity.class);
                        Bundle b = new Bundle();
                        b.putString("eid", eid);
                        b.putString("name", name);
                        b.putInt("icon", icon);
                        b.putInt("creator", creator);
                        b.putString("date", temp);
                        intent.putExtras(b);
                        startActivity(intent);
                    }
                });


                //Add the Date
                if (!month.equals("00") && !day.equals("00")) {
                    lp_date.setMargins(0, (height + divHeight) * counter, 0, 0);
                    lp_date.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    TextView datetime = new TextView(this);
                    datetime.setText(date);
                    datetime.setGravity(Gravity.CENTER);
                    datetime.setLayoutParams(lp_date);
                    layout.addView(datetime);
                }


                //Add the Divider
                lp_div.setMargins(height, (height + divHeight) * counter + height, 20, 0);
                ImageView div = new ImageView(this);
                div.setImageResource(R.drawable.divider);
                div.setLayoutParams(lp_div);
                layout.addView(div);

                counter++;
            }
            catch(JSONException ex){

            }
        }
    }

    //Gets the country code of the user so the app can add Country Codes to the Phone numbers in the users contacts.
    public void getCountry(){
        String tag_string_req = "req_country";
        if (first) {
            pDialog.setMessage("Getting your contacts...");
            showDialog();
        }

        StringRequest strReq = new StringRequest(Request.Method.POST,
                CONTACTS_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        JSONObject details = jObj.getJSONObject("result");
                        country = details.getString("country");
                        ownphone = details.getString("phone");
                        getContacts();
                    }
                    else {
                        country = null;
                        ownphone = null;
                    }
                } catch (JSONException e) {

                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(),"something went wrong", Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "check");
                params.put("uid", uid);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //Reads your contact List
    public void getContacts(){
        Log.d("country:", country);
        Log.d("phone:", ownphone);
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace("-", "");
                        if (phoneNo.charAt(0) != '+'){
                            while(phoneNo.charAt(0) == '0'){
                                phoneNo = phoneNo.substring(1);
                            }
                            phoneNo = country + phoneNo;
                        }
                        if (!phoneNo.equals(ownphone) && !user_db.userExists(phoneNo)) {
                            Log.d("checking", phoneNo + " : " + name);
                            checkContact(phoneNo, name);
                        }
                    }
                    pCur.close();
                }
            }
        }
        cur.close();
        first = false;
        getEvents();
    }

    // Checks whether a contact is in the details database or not.
    public void checkContact(final String phone, final String name){
        String tag_string_req = "req_phoneno";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                CONTACTS_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        String uid = jObj.getJSONObject("uid").getString("UID");
                        Log.d("added:", name);
                        user_db.addContact(uid, name, phone);
                    }
                } catch (JSONException e) {

                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(getApplicationContext(),"something went wrong", Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "get");
                params.put("phone", phone);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    //Closes the app on double back press within 2 seconds.
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    public void onBackPressed()
    {
        if (menu.isMenuShowing()){
            imgButton.setImageResource(R.drawable.menu);
            imgButton.startAnimation(rotBack);
            add.setVisibility(View.VISIBLE);
            menu.toggle();
        }
        else if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            moveTaskToBack(true);
            return;
        }
        else { Toast.makeText(getBaseContext(), "Tap back button again to exit", Toast.LENGTH_SHORT).show(); }

        mBackPressed = System.currentTimeMillis();
    }

    //Gets the events everytime you resume.
    @Override
    public void onResume(){
        super.onResume();
        getEvents();
        if (menu.isMenuShowing()){
            imgButton.setImageResource(R.drawable.menu);
            imgButton.startAnimation(rotBack);
            add.setVisibility(View.VISIBLE);
            menu.toggle();
        }
    }
}