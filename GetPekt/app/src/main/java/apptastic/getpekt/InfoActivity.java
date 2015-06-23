package apptastic.getpekt;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import helper.SQLiteHandler;
import helper.SessionManager;

/**
 * Displays the Information of the Event
 * @author AppTastic
 */
public class InfoActivity extends ActionBarActivity {

    public static String URL = "https://vps1.insertsoft.nl/getpekt/events/";
    public static String DETAILS_URL = "https://vps1.insertsoft.nl/getpekt/details/";
    public static String URL_ICON = "https://vps1.insertsoft.nl/getpekt/icons/";
    private Typeface express;
    private SQLiteHandler user_db;
    private SessionManager session;
    private RelativeLayout layout;
    private RelativeLayout participants_layout;

    private String uid;

    private String eid;
    private String name;
    private String date;
    private int icon;
    private boolean creator;

    private ProgressDialog pDialog;
    private ArrayList<String> participants;

    private boolean changed = false;

    private int height;

    private EditText info_name;
    private EditText info_day;
    private EditText info_month;
    private EditText info_year;
    private EditText info_hours;
    private EditText info_minutes;

    private static final int CROP_PICTURE = 2;
    private ImageButton icon_but;

    private int temp_iconID;
    private Bitmap icon_bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        overridePendingTransition(0,0);

        FacebookSdk.sdkInitialize(getApplicationContext());

        //Get the UID
        user_db = new SQLiteHandler(getApplicationContext());
        uid = user_db.getUserDetails().get("email");

        // session manager
        session = new SessionManager(getApplicationContext());
        express = Typeface.createFromAsset(getAssets(), "expressway.ttf");

        if (AccessToken.getCurrentAccessToken() == null && !session.isLoggedIn()) {
            logoutUser();
        }

        //Set the Progress Dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Getting participants...");

        //Get both of the layouts
        layout = (RelativeLayout) findViewById(R.id.info_layout);
        participants_layout = (RelativeLayout) findViewById(R.id.participants_layout);

        //Get the item height
        height = getResources().getDimensionPixelSize(R.dimen.item_height);

        //Get the ID/Name/Icon

        Bundle b = getIntent().getExtras();
        eid = b.getString("eid");
        date = b.getString("date");
        name = b.getString("name");
        icon = b.getInt("icon");
        temp_iconID = icon;
        creator = b.getBoolean("creator");

        //Set the ActionBar
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.actionbar_main, null);

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

        /**
         * Set the previous Button; if there are any changes made an AlertDialog will show
         * asking the user whether he wants to save the changes or not.
         */
        final ImageButton imgButton = (ImageButton) findViewById(R.id.menu);
        imgButton.setImageResource(R.drawable.previous);

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (changed){
                    AlertDialog.Builder saveDialog = new AlertDialog.Builder(InfoActivity.this);
                    saveDialog
                            .setMessage("Do you want to apply the made changes?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    saveIcon();
                                }
                            })
                            .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(InfoActivity.this, EventActivity.class);
                                    Bundle b = new Bundle();
                                    b.putString("eid", eid);
                                    b.putString("name", name);
                                    b.putInt("icon", icon);
                                    b.putString("date", date);
                                    if (creator)
                                        b.putInt("creator", 1);
                                    else
                                        b.putInt("creator", 0);
                                    intent.putExtras(b);
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(0,0);
                                }
                            }).show();

                } else {
                    Intent intent = new Intent(InfoActivity.this, EventActivity.class);
                    Bundle b = new Bundle();
                    b.putString("eid", eid);
                    b.putString("name", name);
                    b.putInt("icon", icon);
                    b.putString("date", date);
                    if (creator)
                        b.putInt("creator", 1);
                    else
                        b.putInt("creator", 0);
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(0,0);
                }
            }
        });

        /**
         * If the user is the creator of the event, this will show a button to remove the entire event.
         * If not so, this will be a button to leave the event.
         */
        final ImageButton remove = (ImageButton) findViewById(R.id.add);

        if (creator) {
            remove.setImageResource(R.drawable.remove);

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder addBuilder = new AlertDialog.Builder(InfoActivity.this);
                    addBuilder.setMessage("Are you sure you want to remove the event?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    removeEvent("remove");
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });
        } else {
            remove.setImageResource(R.drawable.leave);
            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder addBuilder = new AlertDialog.Builder(InfoActivity.this);
                    addBuilder.setMessage("Are you sure you want to leave the event?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    removeEvent("leave");
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });
        }

        TextView txt = (TextView) findViewById(R.id.title_text);
        txt.setTypeface(express);
        txt.setTextSize(getResources().getDimension(R.dimen.button_text));
        txt.setText(R.string.title_activity_info);

        //Set the Icon
        icon_but = (ImageButton) findViewById(R.id.info_icon);

        if (icon != 0){
            byte[] image = user_db.getBlob(eid);
            icon_but.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
            icon_but.setPadding(5, 5, 5, 5);
        }
        //OnClickListener for the icon, same as in the CreateActivity
        icon_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changed = true;
                CharSequence[] items = {"Custom Icon", "Default Icon"};
                new AlertDialog.Builder(InfoActivity.this)
                        .setTitle("Choose an Icon")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0:
                                        Intent intent = new Intent(InfoActivity.this, CropActivity.class);
                                        startActivityForResult(intent, CROP_PICTURE);
                                    case 1:
                                        temp_iconID = 0;
                                        icon_but.setImageResource(R.drawable.group);
                                        icon_but.setPadding(12, 12, 12, 12);break;
                                }
                            }
                        }).create().show();
            }
        });



        //Set the layout

        info_name = (EditText) findViewById(R.id.info_name);
        info_name.setText(name);
        info_name.setFocusable(false);

        // When clicked makes the Name editable
        ImageButton edit_name = (ImageButton) findViewById(R.id.edit_name);
        edit_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changed = true;
                info_name.setFocusableInTouchMode(true);
                info_name.setFocusable(true);
                info_name.setClickable(true);

                info_name.requestFocus();
                info_name.setSelection(info_name.getText().length());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(info_name, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        info_day = (EditText) findViewById(R.id.info_day);
        info_day.setText(date.substring(8, 10));
        info_day.setFocusable(false);

        info_month = (EditText) findViewById(R.id.info_month);
        info_month.setText(date.substring(5, 7));
        info_month.setFocusable(false);

        info_year = (EditText) findViewById(R.id.info_year);
        info_year.setText(date.substring(0, 4));
        info_year.setFocusable(false);

         // When clicked makes the Date editable
        ImageButton edit_date = (ImageButton) findViewById(R.id.edit_date);
        edit_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changed = true;
                info_day.setFocusableInTouchMode(true);
                info_day.setFocusable(true);
                info_day.setClickable(true);

                info_month.setFocusableInTouchMode(true);
                info_month.setFocusable(true);
                info_month.setClickable(true);

                info_year.setFocusableInTouchMode(true);
                info_year.setFocusable(true);
                info_year.setClickable(true);

                info_day.requestFocus();
                info_day.setSelection(info_day.getText().length());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(info_day, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        info_hours = (EditText) findViewById(R.id.info_hours);
        info_hours.setText(date.substring(11, 13));
        info_hours.setFocusable(false);

        info_minutes = (EditText) findViewById(R.id.info_minutes);
        info_minutes.setText(date.substring(14, 16));
        info_minutes.setFocusable(false);

        //When clicked makes the Time editable
        ImageButton edit_time = (ImageButton) findViewById(R.id.edit_time);
        edit_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changed = true;

                info_hours.setFocusableInTouchMode(true);
                info_hours.setFocusable(true);
                info_hours.setClickable(true);

                info_minutes.setFocusableInTouchMode(true);
                info_minutes.setFocusable(true);
                info_minutes.setClickable(true);

                info_hours.requestFocus();
                info_hours.setSelection(info_hours.getText().length());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(info_hours, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        /**
         * If the user is the creator, shows a button which lets the user
         * add other contacts to the event. If not so, removes the button
         * completely from the view.
         */
        Button addParticipants = (Button) findViewById(R.id.btnParticipants);

        if (creator) {
            addParticipants.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(InfoActivity.this, AddContactsActivity.class);
                    Bundle b = new Bundle();
                    b.putString("eName", name);
                    b.putString("eIcon", Integer.toString(icon));
                    b.putString("event", date);
                    b.putString("eid", eid);
                    b.putBoolean("first", false);
                    b.putStringArrayList("participants", participants);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            });
        } else {
            addParticipants.setVisibility(View.GONE);
        }

        //Saves the changes made on press
        Button save = (Button) findViewById(R.id.btnSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIcon();
            }
        });
        showDialog();
        getParticipants();
    }

    //Gets the participants of the event and puts them in an arraylist
    public void getParticipants(){
        String tag_string_req = "req_participants";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        participants = new ArrayList<String>();
                        participants_layout.removeAllViews();
                        int counter = 0;
                        while (jObj.has(Integer.toString(counter))){
                            participants.add(jObj.getJSONObject(Integer.toString(counter)).getString("uid"));
                            counter++;
                        }
                        drawParticipants();
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
                        "ERROR", Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "participants");
                params.put("eid", eid);
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //Draws the participants of the event
    public void drawParticipants(){
        //Draw Yourself
        RelativeLayout.LayoutParams lp_you = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
        lp_you.setMargins(25, 0, 0, 0);
        TextView you = new TextView(this);
        you.setText("You");
        you.setTextSize(getResources().getDimension(R.dimen.button_text));
        you.setSingleLine(true);
        you.setTextColor(getResources().getColor(R.color.black));
        you.setGravity(Gravity.CENTER_VERTICAL);
        you.setLayoutParams(lp_you);
        participants_layout.addView(you);

        /**
         * Draws all the other contacts; if they are in your contact list this
         * will draw the name you have for them in there, else it will draw their
         * phone-number.
         */
        int counter = 1;
        for (final String participant: participants) {
            if (!participant.equals(uid)) {
                final StringBuilder temp_part = new StringBuilder();
                if (user_db.getUIDs().contains(participant)) {
                    temp_part.append(user_db.getName(participant));
                } else {
                    String tag_string_req = "req_phone";

                    StringRequest strReq = new StringRequest(Request.Method.POST,
                            DETAILS_URL, new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jObj = new JSONObject(response);
                                boolean error = jObj.getBoolean("error");
                                if (!error) {
                                    temp_part.append(jObj.getJSONObject("phone").getString("phone"));
                                } else {
                                    Toast.makeText(getBaseContext(),
                                            "Something went wrong here", Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(),
                                    "ERROR", Toast.LENGTH_LONG).show();
                        }
                    }) {

                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("tag", "get");
                            params.put("uid", participant);
                            return params;
                        }

                    };
                    AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
                }
                RelativeLayout.LayoutParams lp_part = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, height);
                lp_part.setMargins(25, height * counter, 0, 0);
                TextView contact = new TextView(this);
                contact.setText(temp_part.toString());
                contact.setTextSize(getResources().getDimension(R.dimen.button_text));
                contact.setSingleLine(true);
                contact.setTextColor(getResources().getColor(R.color.black));
                contact.setGravity(Gravity.CENTER_VERTICAL);
                contact.setLayoutParams(lp_part);
                participants_layout.addView(contact);

                counter++;
            }
        }
        hideDialog();
    }

    //Removes the event
    public void removeEvent(final String remove) {
        String tag_string_req = "req_remove";

        StringRequest strReq = new StringRequest(Request.Method.POST,URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        Intent intent = new Intent(InfoActivity.this, MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        finish();
                    } else {
                        Toast.makeText(getBaseContext(),
                                "Something went wrong here", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "ERROR", Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", remove);
                params.put("eid", eid);
                if (remove.equals("leave"))
                    params.put("uid", uid);
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //Saves the icon if changed
    public void saveIcon(){
        pDialog.setMessage("Saving changes...");
        showDialog();

        if (temp_iconID != icon && temp_iconID != 0){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            icon_bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            user_db.addBLOB(eid, temp_iconID, stream.toByteArray());

            String tag_icon = "req_icon";
            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    URL_ICON, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");

                        if (!error) {
                            saveChanges();
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
                            "ERROR", Toast.LENGTH_LONG).show();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("tag", "create");
                    params.put("eid", eid);
                    params.put("blob", Base64.encodeToString(user_db.getBlob(eid), Base64.DEFAULT));
                    return params;
                }

            };
            AppController.getInstance().addToRequestQueue(stringRequest, tag_icon);
        } else {
            saveChanges();
        }

    }

    //Saves the changes made
    public void saveChanges(){
        final String updated_name = info_name.getText().toString();

        String h = info_hours.getText().toString();
        String m = info_minutes.getText().toString();
        String y = info_year.getText().toString();
        String mo = info_month.getText().toString();
        String d = info_day.getText().toString();
        if (h.length() == 0){
            h = "00";
        }
        else if (h.length() == 1){
            h = "0" + h;
        }
        if (m.length() == 0){
            m = "00";
        }
        else if (m.length() == 1){
            m = "0" + m;
        }
        if (d.length() == 0){
            d = "00";
        }
        else if (d.length() == 1){
            d = "0" + d;
        }
        if (mo.length() == 0){
            mo = "00";
        }
        else if (mo.length() == 1){
            mo = "0" + mo;
        }
        if (y.length() == 0){
            y = "0000";
        }
        else if (mo.length() == 1){
            y = "000" + y;
        }
        else if (y.length() == 2){
            y = "00" + y;
        }
        else if (y.length() == 3){
            y = "0" + y;
        }
        String date_temp = y + '-' + mo + '-' + d;
        String time_temp = h + ':' + m + ":00";
        final String updated_date = date_temp + ' ' + time_temp;


        String tag_string_req = "req_update";

        StringRequest strReq = new StringRequest(Request.Method.POST,URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        Intent intent = new Intent(InfoActivity.this, EventActivity.class);
                        Bundle b = new Bundle();
                        b.putString("eid", eid);
                        b.putString("name", updated_name);
                        b.putInt("icon", temp_iconID);
                        b.putString("date", updated_date);
                        if (creator)
                            b.putInt("creator", 1);
                        else
                            b.putInt("creator", 0);
                        intent.putExtras(b);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getBaseContext(),
                                "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                hideDialog();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "Something went wrong", Toast.LENGTH_LONG).show();
                hideDialog();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "update");
                params.put("eid", eid);
                params.put("icon", Integer.toString(temp_iconID));
                params.put("name", updated_name);
                params.put("event", updated_date);
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //Logs out the user
    private void logoutUser() {
        session.setLogin(false);

        user_db.deleteUsers();

        // Launching the login activity
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
     * If there are changes made, shows an alertdialog asking the user whether he wants
     * to save these changes or discard them.
     */
    @Override
    public void onBackPressed(){
        if (changed){
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(InfoActivity.this);
            saveDialog
                    .setMessage("Do you want to apply the made changes?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveIcon();
                        }
                    })
                    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(InfoActivity.this, EventActivity.class);
                            Bundle b = new Bundle();
                            b.putString("eid", eid);
                            b.putString("name", name);
                            b.putInt("icon", icon);
                            b.putString("date", date);
                            if (creator)
                                b.putInt("creator", 1);
                            else
                                b.putInt("creator", 0);
                            intent.putExtras(b);
                            startActivity(intent);
                            finish();
                            overridePendingTransition(0,0);
                        }
                    }).show();

        } else {
            Intent intent = new Intent(InfoActivity.this, EventActivity.class);
            Bundle b = new Bundle();
            b.putString("eid", eid);
            b.putString("name", name);
            b.putInt("icon", icon);
            b.putString("date", date);
            if (creator)
                b.putInt("creator", 1);
            else
                b.putInt("creator", 0);
            intent.putExtras(b);
            startActivity(intent);
            finish();
            overridePendingTransition(0,0);
        }
    }

    //OnActivityResult for the CropActivity
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK){
            if (requestCode == CROP_PICTURE){
                icon_bmp = (Bitmap) data.getParcelableExtra("result");
                icon_but.setImageBitmap(icon_bmp);
                icon_but.setPadding(5, 5, 5, 5);
                Random random = new Random();
                temp_iconID = random.nextInt(Integer.MAX_VALUE) + 1;
            }
        }
    }

    //Draws the participants onResume (necessary for when you have just added new participants)
    @Override
    public void onResume(){
        super.onResume();
        getParticipants();
    }
}