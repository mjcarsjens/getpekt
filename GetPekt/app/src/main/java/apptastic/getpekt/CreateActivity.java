package apptastic.getpekt;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import helper.SQLiteHandler;
import helper.SessionManager;

/**
 * Creates an Event
 * @author AppTastic
 */
public class CreateActivity extends ActionBarActivity {

    public static String URL = "https://vps1.insertsoft.nl/getpekt/events/";
    private SQLiteHandler user_db;
    private SessionManager session;

    private Typeface express;

    private String eIcon = "0";
    private String dateTime;
    private String eName;
    private String eid;

    private ProgressDialog pDialog;

    private static final int CROP_PICTURE = 2;
    private ImageButton icon;

    private Bitmap icon_bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        overridePendingTransition(0, 0);

        user_db = new SQLiteHandler(getApplicationContext());

        FacebookSdk.sdkInitialize(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());
        express = Typeface.createFromAsset(getAssets(), "expressway.ttf");

        if (AccessToken.getCurrentAccessToken() == null && !session.isLoggedIn()) {
            logoutUser();
        }

        //Set a random value between 1 and the maximum Integer value as the Event-ID (very small change of collisions)
        Random random = new Random();
        eid = Integer.toString(random.nextInt(Integer.MAX_VALUE) + 1);

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
        txt.setText(R.string.title_activity_create);

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

        //Set the other buttons
        buttonEvents();
    }

    /**
     * Sets the button Events
     */
    public void buttonEvents(){

        //Get the Icon, the EditText fields and the Button
        icon = (ImageButton) findViewById(R.id.info_icon);
        final EditText name = (EditText) findViewById(R.id.info_name);
        final EditText year = (EditText) findViewById(R.id.info_year);
        final EditText month = (EditText) findViewById(R.id.info_month);
        final EditText day = (EditText) findViewById(R.id.info_day);
        final EditText hours = (EditText) findViewById(R.id.info_hours);
        final EditText minutes = (EditText) findViewById(R.id.info_minutes);
        Button add = (Button) findViewById(R.id.btnSave);

        //Set the ProgressDialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        /**
         * OnClickListener for the icon: Upon click you get two choices in an AlertDialog,
         * Use the standard Icon or use a custom Icon. When you choose the standard icon this
         * one gets set, else you start the cropactivity for a result.
         */
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] items = {"Custom Icon", "Default Icon"};
                new AlertDialog.Builder(CreateActivity.this)
                        .setTitle("Choose an Icon")
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0:
                                        Intent intent = new Intent(CreateActivity.this, CropActivity.class);
                                        startActivityForResult(intent, CROP_PICTURE);
                                    case 1:
                                        eIcon = "0";
                                        icon.setImageResource(R.drawable.group);
                                        icon.setPadding(12, 12, 12, 12);break;
                                }
                            }
                        }).create().show();
            }
        });

        /**
         * OnClickListener for the createButton: Checks whether the given time and date values are all
         * in the necessary format, and else edits them to be by adding zeroes. If there is no name entered
         * or an invalid date/time, this gets shown in a toast, and else the inputs get given to the AddContactsActivity
         * which gets started.
         */
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eName = name.getText().toString();
                String h = hours.getText().toString();
                String m = minutes.getText().toString();
                String y = year.getText().toString();
                String mo = month.getText().toString();
                String d = day.getText().toString();
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
                String date = y + '-' + mo + '-' + d;
                String time = h + ':' + m + ":00";
                dateTime = date + ' ' + time;

                if (eName.length() == 0){
                    Toast.makeText(getApplicationContext(), "please enter a name", Toast.LENGTH_SHORT).show();
                }
                else if (Integer.parseInt(mo) > 12 || Integer.parseInt(d) > 31 || Integer.parseInt(h) > 24 || Integer.parseInt(m) > 60){
                    Toast.makeText(getApplicationContext(), "please enter a correct date/time", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (Integer.parseInt(eIcon) != 0){
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        icon_bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                        user_db.addBLOB(eid, Integer.parseInt(eIcon), stream.toByteArray());
                    }
                    Intent intent = new Intent(CreateActivity.this, AddContactsActivity.class);
                    Bundle b = new Bundle();
                    b.putString("eName", eName);
                    b.putString("eIcon", eIcon);
                    b.putString("event", dateTime);
                    b.putString("eid", eid);
                    b.putBoolean("first", true);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            }
        });
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }


    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //Logs out the user
    private void logoutUser() {
        session.setLogin(false);

        user_db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(CreateActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * OnActivityResult: This method gets called when an activity started for a result
     * finishes and gives you one. In this class it gets called when CropActivity is finished,
     * and if it gives and RESULT_OK you get the image and set it as the bitmap on the imageIcon.
     * Also a random IconID gets generated (again, small possibility of duplicate values).
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK){
            if (requestCode == CROP_PICTURE){
                icon_bmp = (Bitmap) data.getParcelableExtra("result");
                icon.setImageBitmap(icon_bmp);
                icon.setPadding(5, 5, 5, 5);
                Random random = new Random();
                eIcon = Integer.toString(random.nextInt(Integer.MAX_VALUE) + 1);
            }
        }
    }


    @Override
    public void onBackPressed(){
        finish();
        overridePendingTransition(0,0);
    }
}