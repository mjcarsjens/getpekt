package apptastic.getpekt;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
 * Shows the Items of an event.
 * @author AppTastic
 */
public class EventActivity extends ActionBarActivity {

    //Some Shameless self-advertisement.
    public static String URL = "https://vps1.insertsoft.nl/getpekt/items/";
    public static String DETAILS_URL = "https://vps1.insertsoft.nl/getpekt/details/";

    private Typeface express;
    private SQLiteHandler user_db;
    private SessionManager session;
    private List<JSONObject> items;
    private RelativeLayout layout;

    private ProgressDialog pDialog;

    private String eid;
    private String name;
    private int icon;
    private boolean creator;
    private String date;

    private int height;
    private int width;
    private int divHeight;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        overridePendingTransition(0, 0);

        FacebookSdk.sdkInitialize(getApplicationContext());

        // Get the UID
        user_db = new SQLiteHandler(getApplicationContext());
        uid = user_db.getUserDetails().get("email");

        // session manager
        session = new SessionManager(getApplicationContext());
        express = Typeface.createFromAsset(getAssets(), "expressway.ttf");

        if (AccessToken.getCurrentAccessToken() == null && !session.isLoggedIn()) {
            logoutUser();
        }

        //Get the ID/Name/Icon

        Bundle b = getIntent().getExtras();
        eid = b.getString("eid");
        name = b.getString("name");
        icon = b.getInt("icon");
        date = b.getString("date");
        if (b.getInt("creator") == 1){
            creator = true;
        } else {
            creator = false;
        }
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Getting the items...");

        //Set the ActionBar
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.actionbar_main, null);

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

        //Set the previous button
        final ImageButton imgButton = (ImageButton) findViewById(R.id.menu);
        imgButton.setImageResource(R.drawable.previous);

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0,0);
            }
        });

        /**
         * Sets the add button: upon being clicked it shows an AlertDialog where you can enter
         * an item and an amount, with 3 options: cancel, add the item and close the dialog,
         * or add the item and add another item afterwards.
         */
        final ImageButton add = (ImageButton) findViewById(R.id.add);
        add.setImageResource(R.drawable.add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final LinearLayout setFields = new LinearLayout(EventActivity.this);
                setFields.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams textViews = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                textViews.setMargins(0, 15, 0, 15);
                final TextView textName = new TextView(EventActivity.this);
                textName.setText("Name");
                textName.setTextSize(getResources().getDimension(R.dimen.button_text));
                textName.setLayoutParams(textViews);
                textName.setGravity(Gravity.CENTER);
                setFields.addView(textName);
                final EditText setName = new EditText(EventActivity.this);
                setName.setFilters(new InputFilter[] {new InputFilter.LengthFilter(25)});
                setName.setGravity(Gravity.CENTER);
                setFields.addView(setName);
                final TextView textAmount = new TextView(EventActivity.this);
                textAmount.setLayoutParams(textViews);
                textAmount.setText("Amount");
                textAmount.setTextSize(getResources().getDimension(R.dimen.button_text));
                textAmount.setGravity(Gravity.CENTER);
                setFields.addView(textAmount);
                LinearLayout.LayoutParams editText = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                editText.setMargins(0, 0, 0, 10);
                final EditText setAmount = new EditText(EventActivity.this);
                setAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
                setAmount.setGravity(Gravity.CENTER);
                setAmount.setLayoutParams(editText);
                setFields.addView(setAmount);

                AlertDialog.Builder addBuilder = new AlertDialog.Builder(EventActivity.this);
                addBuilder.setView(setFields)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Checks if the amount is a normal value, and if so, adds the item and
                                //refreshes the item list
                                if (setName.getText().length() == 0){
                                    Toast.makeText(EventActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                                    add.callOnClick();
                                }
                                    else {
                                    if (setAmount.getText().length() == 0) {
                                        setAmount.setText("1");
                                    }
                                    if (Integer.parseInt(setAmount.getText().toString()) > 9999) {
                                        Toast.makeText(EventActivity.this, "Don't you think that's a bit of an overkill?", Toast.LENGTH_SHORT).show();
                                        add.callOnClick();
                                    } else if (Integer.parseInt(setAmount.getText().toString()) > 0) {
                                        addItem(setName.getText().toString(), setAmount.getText().toString());
                                        getItems();
                                    } else {
                                        Toast.makeText(EventActivity.this, "Please enter an amount of at least 1", Toast.LENGTH_SHORT).show();
                                        add.callOnClick();
                                    }
                                }
                            }
                        })
                        .setNeutralButton("Next", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (setName.getText().length() == 0){
                                    Toast.makeText(EventActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    if (setAmount.getText().length() == 0) {
                                        setAmount.setText("1");
                                    }
                                    if (Integer.parseInt(setAmount.getText().toString()) > 9999) {
                                        Toast.makeText(EventActivity.this, "Don't you think that's a bit of an overkill?", Toast.LENGTH_SHORT).show();
                                    } else if (Integer.parseInt(setAmount.getText().toString()) > 0) {
                                        addItem(setName.getText().toString(), setAmount.getText().toString());
                                        getItems();
                                    } else
                                        Toast.makeText(EventActivity.this, "Please enter an amount of at least 1", Toast.LENGTH_SHORT).show();
                                }
                                add.callOnClick();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                getItems();
                                dialog.cancel();
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        //Sets the ActionBar TextView and empty space to open the InfoActivity when clicked.
        TextView txt = (TextView) findViewById(R.id.title_text);
        txt.setTypeface(express);
        txt.setTextSize(getResources().getDimension(R.dimen.button_text));
        String temp = stringEditor(name, txt);
        txt.setText(temp);
        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventActivity.this, InfoActivity.class);
                Bundle b = new Bundle();
                b.putString("eid", eid);
                b.putString("name", name);
                b.putInt("icon", icon);
                b.putString("date", date);
                b.putBoolean("creator", creator);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });
        View view = (View) findViewById(R.id.fillView);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventActivity.this, InfoActivity.class);
                Bundle b = new Bundle();
                b.putString("eid", eid);
                b.putString("name", name);
                b.putInt("icon", icon);
                b.putString("date", date);
                b.putBoolean("creator", creator);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });

        //Set the drawing dimensions

        layout = (RelativeLayout) findViewById(R.id.event_layout);

        height = getResources().getDimensionPixelSize(R.dimen.item_height);
        width = RelativeLayout.LayoutParams.MATCH_PARENT;
        divHeight = getResources().getDimensionPixelSize(R.dimen.div_height);
        showDialog();
        getItems();
    }


    /**
     *  Checks whether the name of the event actually fits within the actionbar, and if not,
     *  removes the last character until it does fit (and then removes 3 more characters and adds 3 dots
     *  instead). This because the standard SingleLine Function would still make the name overlap with
     *  the add-button.
     */
    public String stringEditor(String name, TextView text){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        while (text.getPaint().measureText(name) > width - 2*height){
            name = name.substring(0, name.length()-1);
            if (text.getPaint().measureText(name) > width - 2*height)
                name = name.substring(0, name.length()-3) + "...";
        }
        return name;
    }

    /**
     * Adds the item to the online database (using Volley).
     * @param name Name of the item
     * @param amount Amount of the item
     */
    public void addItem(final String name, final String amount){
        String tag_string_req = "req_add";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {}
                    else {
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
                params.put("name", name);
                params.put("amount", amount);
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    /**
     * Gets the items from the database (using Volley).
     */
    public void getItems(){

        String tag_string_req = "req_items";
        items = new ArrayList<JSONObject>();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        int counter = 0;
                        while(jObj.has(Integer.toString(counter))){
                            items.add(jObj.getJSONObject(Integer.toString(counter)));
                            counter++;
                        }
                        drawItems(counter);
                    } else {
                        String error_msg = jObj.getString("error_msg");
                        Toast.makeText(getBaseContext(),
                                error_msg, Toast.LENGTH_LONG).show();
                    }

                    hideDialog();
                } catch (JSONException e) {
                    hideDialog();
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
                params.put("tag", "get");
                params.put("eid", eid);
                params.put("uid", uid);

                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //Draws the items after they are retrieved from the database.
    private void drawItems(int debug){

        layout.removeAllViews();
        int counter = 0;

        for (final JSONObject j : items) {
            try {
                if (counter < debug) {
                    final String item = j.getString("item");
                    final String name = j.getString("name");
                    final int amount = j.getInt("amount");
                    final int selected = j.getInt("selected");
                    final String selected_by = j.getString("selected_by");
                    final String number = j.getString("number");

                    RelativeLayout.LayoutParams lp_checkbox = new RelativeLayout.LayoutParams(height, height);
                    RelativeLayout.LayoutParams lp_amount = new RelativeLayout.LayoutParams(height, height);
                    RelativeLayout.LayoutParams lp_button = new RelativeLayout.LayoutParams(width, height);
                    RelativeLayout.LayoutParams lp_div = new RelativeLayout.LayoutParams(width, divHeight);

                    //Add the button
                    lp_button.setMargins(height + 25, (height + divHeight) * counter, height + 10, 0);
                    TextView button = new TextView(this);
                    button.setText(name);
                    button.setTypeface(express);
                    button.setTextSize(getResources().getDimension(R.dimen.button_text));
                    button.setTextColor(getResources().getColor(R.color.black));
                    button.setGravity(Gravity.CENTER_VERTICAL);
                    button.setSingleLine(true);
                    button.setLayoutParams(lp_button);
                    layout.addView(button);
                    /**
                     *  When the name of the item gets clicked, it opens an AlertDialog which
                     *  shows who has selected this particular item and how many of the item
                     *  each person has selected. Also has a remove-button which removes the item
                     *  from the list completely.
                     */
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final LinearLayout view = new LinearLayout(EventActivity.this);
                            view.setOrientation(LinearLayout.VERTICAL);

                            LinearLayout.LayoutParams lp_title = new LinearLayout.LayoutParams(width, height);
                            final LinearLayout.LayoutParams lp_selected = new LinearLayout.LayoutParams(width, height/2);


                            TextView title = new TextView(EventActivity.this);
                            title.setText(name + " (" + amount + "x)");
                            title.setTextSize(getResources().getDimension(R.dimen.button_text));
                            title.setTextColor(getResources().getColor(R.color.black));
                            title.setGravity(Gravity.CENTER);
                            title.setLayoutParams(lp_title);
                            view.addView(title);

                            final TextView selected = new TextView(EventActivity.this);
                            selected.setTextSize(getResources().getDimension(R.dimen.other_text));
                            selected.setText("Selected by:");
                            selected.setGravity(Gravity.CENTER);
                            selected.setLayoutParams(lp_title);
                            view.addView(selected);

                            String tag_string_req = "selected_by";

                            StringRequest strReq = new StringRequest(Request.Method.POST,
                                    URL, new Response.Listener<String>() {

                                @Override
                                public void onResponse(String response) {
                                    try {
                                        int i = 0;
                                        JSONObject jObj = new JSONObject(response);
                                        boolean error = jObj.getBoolean("error");

                                        if (!error) {
                                            while (jObj.has(Integer.toString(i))){
                                                JSONObject temp = jObj.getJSONObject(Integer.toString(i));
                                                TextView item = new TextView(EventActivity.this);
                                                item.setGravity(Gravity.CENTER);
                                                item.setLayoutParams(lp_selected);
                                                final StringBuilder input = new StringBuilder();

                                                if (!temp.getString("selected_by").equals(uid)) {
                                                    if (user_db.getUIDs().contains(temp.getString("selected_by"))) {
                                                        input.append(user_db.getName(temp.getString("selected_by")));
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
                                                                        input.append(jObj.getJSONObject("phone").getString("phone"));
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
                                                                params.put("uid", uid);
                                                                return params;
                                                            }

                                                        };
                                                        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
                                                    }
                                                } else {
                                                    input.append("You");
                                                }
                                                input.append(" (" + temp.getString("number") + "x)");
                                                item.setText(input);
                                                i++;
                                                if (!jObj.has(Integer.toString(i))){
                                                    lp_selected.setMargins(0,0,0,10);
                                                }
                                                view.addView(item);
                                            }
                                        } else {
                                            String error_msg = jObj.getString("error_msg");
                                            Toast.makeText(getBaseContext(),
                                                    error_msg, Toast.LENGTH_LONG).show();
                                        }

                                        hideDialog();
                                    } catch (JSONException e) {
                                        hideDialog();
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
                                    params.put("tag", "selected");
                                    params.put("eid", eid);
                                    params.put("item", item);

                                    return params;
                                }

                            };
                            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);


                            AlertDialog.Builder infoBuilder = new AlertDialog.Builder(EventActivity.this);
                            infoBuilder.setCancelable(true);
                            infoBuilder.setView(view)
                                    .setNeutralButton("Remove Item", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            AlertDialog.Builder removeBuilder = new AlertDialog.Builder(EventActivity.this);
                                            removeBuilder
                                                    .setCancelable(false)
                                                    .setMessage("Are you sure you want to remove '" + name + "'?")
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            pDialog.setMessage("Removing item...");
                                                            showDialog();

                                                            String tag_string_req = "selected_by";

                                                            StringRequest strReq = new StringRequest(Request.Method.POST,
                                                                    URL, new Response.Listener<String>() {

                                                                @Override
                                                                public void onResponse(String response) {
                                                                    try {
                                                                        int i = 0;
                                                                        JSONObject jObj = new JSONObject(response);
                                                                        boolean error = jObj.getBoolean("error");
                                                                        if (!error) {
                                                                            getItems();
                                                                        } else {
                                                                            String error_msg = jObj.getString("error_msg");
                                                                            Toast.makeText(getBaseContext(),
                                                                                    error_msg, Toast.LENGTH_LONG).show();
                                                                        }

                                                                        hideDialog();
                                                                    } catch (JSONException e) {
                                                                        hideDialog();
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
                                                                    params.put("tag", "remove");
                                                                    params.put("eid", eid);
                                                                    params.put("item", item);

                                                                    return params;
                                                                }

                                                            };
                                                            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
                                                        }
                                                    })
                                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.cancel();
                                                            dialog.dismiss();
                                                        }
                                                    }).show();

                                        }
                                    })
                                    .show();
                        }
                    });

                    //Add the Amount
                    lp_amount.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    lp_amount.setMargins(0, (height + divHeight) * counter, 0, 0);
                    TextView txtAmount = new TextView(this);
                    if (Integer.parseInt(number) > 9999)
                        txtAmount.setText("A LOT");
                    else
                        txtAmount.setText(number + "/" + amount);
                    txtAmount.setGravity(Gravity.CENTER_VERTICAL);
                    txtAmount.setLayoutParams(lp_amount);
                    layout.addView(txtAmount);

                    //Add the Divider
                    lp_div.setMargins(height + 20, (height + divHeight) * counter + height, 10, 0);
                    ImageView div = new ImageView(this);
                    div.setImageResource(R.drawable.divider);
                    div.setLayoutParams(lp_div);
                    layout.addView(div);

                    /**
                     * Adds the checkbox: the checkbox when touched triggers an connection to be made
                     * with the online database; when the response is OK the icon is changed, this
                     * does however mean that you can only check items when you actually have an internet
                     * connection. If the amount of the item is > 1, the checkbox click (when not selected
                     * by the user) opens an alertdialog where you can specify how many of the item you want
                     * to select.
                     */
                    lp_checkbox.setMargins(10, (height + divHeight) * counter, 0, 0);
                    final ImageButton checkBox = new ImageButton(this);
                    if (selected == 1) {
                        if (selected_by.equals(uid)) {
                            checkBox.setImageResource(R.drawable.checked);
                        } else {
                            checkBox.setImageResource(R.drawable.checked_other);
                        }
                    } else {
                        checkBox.setImageResource(R.drawable.unchecked);
                    }
                    checkBox.setLayoutParams(lp_checkbox);
                    checkBox.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    checkBox.setPadding(5, 5, 5, 5);
                    checkBox.setMaxWidth(height);
                    checkBox.setMaxHeight(height);
                    checkBox.setBackground(null);
                    layout.addView(checkBox);
                    checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (selected_by.equals(uid)) {
                                deselect_entry(item, uid);
                            } else {
                                if (amount == 1)
                                    select_entry(item, uid, "1");
                                else {
                                    AlertDialog.Builder selectBuilder = new AlertDialog.Builder(EventActivity.this);
                                    LinearLayout selectView = new LinearLayout(EventActivity.this);
                                    selectView.setOrientation(LinearLayout.VERTICAL);
                                    LinearLayout.LayoutParams lp_text = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    lp_text.setMargins(0, 5, 0, 10);
                                    LinearLayout.LayoutParams lp_edit = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    lp_edit.setMargins(0, 0, 0, 20);

                                    TextView context = new TextView(EventActivity.this);
                                    context.setText("Please enter the amount you want to select");
                                    context.setTextColor(getResources().getColor(R.color.black));
                                    context.setGravity(Gravity.CENTER);
                                    context.setLayoutParams(lp_text);
                                    selectView.addView(context);

                                    final EditText selectAmount = new EditText(EventActivity.this);
                                    selectAmount.setInputType(InputType.TYPE_CLASS_NUMBER);
                                    selectAmount.setText("1");
                                    selectAmount.setGravity(Gravity.CENTER);
                                    selectAmount.setWidth(100);
                                    selectAmount.setSelection(selectAmount.getText().length());
                                    selectAmount.setLayoutParams(lp_edit);
                                    selectView.addView(selectAmount);

                                    selectBuilder
                                            .setView(selectView)
                                            .setCancelable(true)
                                            .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (selectAmount.getText().length() == 0){
                                                        Toast.makeText(EventActivity.this, "Please enter an amount of at least 1", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else if (Integer.parseInt(selectAmount.getText().toString()) > 9999){
                                                        Toast.makeText(EventActivity.this, "Don't you think that's a bit of an overkill?", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else if (Integer.parseInt(selectAmount.getText().toString()) > 0){
                                                        select_entry(item, uid, selectAmount.getText().toString());
                                                    } else {
                                                        Toast.makeText(EventActivity.this, "Please enter an amount of at least 1", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }).show();
                                }
                            }
                        }
                    });
                    counter++;
                }
            } catch (JSONException ex) {
            }
        }
    }

    //Logs out the user
    private void logoutUser() {
        session.setLogin(false);

        user_db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(EventActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Selects the item in the database.
     * @param item the item being seleted
     * @param uid user (the person selecting the item)
     * @param number the amount being selected
     */
    public void select_entry(final String item, final String uid, final String number){
        String tag_string_req = "req_items";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        getItems();
                    } else {
                        String error_msg = jObj.getString("error_msg");
                        Toast.makeText(getBaseContext(),
                                "Something went wrong", Toast.LENGTH_LONG).show();
                        getItems();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    getItems();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "Something went wrong", Toast.LENGTH_LONG).show();
                getItems();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "select");
                params.put("eid", eid);
                params.put("item", item);
                params.put("number", number);
                params.put("uid", uid);

                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Deselects the item in the database
     * @param item the item to deselect
     * @param uid user (the person deselecting the item)
     */
    public void deselect_entry(final String item, final String uid){
        String tag_string_req = "req_items";
        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        getItems();
                    } else {
                        String error_msg = jObj.getString("error_msg");
                        Toast.makeText(getBaseContext(),
                                "Something went wrong", Toast.LENGTH_LONG).show();
                        getItems();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    getItems();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "Something went wrong", Toast.LENGTH_LONG).show();
                getItems();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "deselect");
                params.put("eid", eid);
                params.put("item", item);
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

    @Override
    public void onBackPressed(){
        finish();
        overridePendingTransition(0,0);
    }

    /**
     * Makes sure the items are synced everytime you resume this activity.
     */
    @Override
    public void onResume(){
        super.onResume();
        getItems();
    }

}