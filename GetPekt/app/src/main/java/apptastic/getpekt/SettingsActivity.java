package apptastic.getpekt;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.widget.LoginButton;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.util.HashMap;

import helper.SQLiteHandler;
import helper.SessionManager;

/**
 * Settings Activity which at the moment only shows your name, uid, and the logout button since we
 * didn't have time to actually implement any other settings. We still kept the screen though since
 * otherwise the slidingmenu would be a bit of a waste, especially since the planned contacts button
 * didn't get through either.
 * @author AppTastic
 */
public class SettingsActivity extends ActionBarActivity {

    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;

    private ImageButton imgButton;
    private Animation rotForward;
    private Animation rotBack;

    private SQLiteHandler db;
    private SessionManager session;
    private CallbackManager callbackManager;

    private SlidingMenu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        overridePendingTransition(0, 0);

        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        btnLogout = (Button) findViewById(R.id.btnLogout);


        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        //Shows the appropriate logout-button.
        if (AccessToken.getCurrentAccessToken() != null) {
            btnLogout.setVisibility(View.GONE);

            FacebookSdk.sdkInitialize(getApplicationContext());
            callbackManager = CallbackManager.Factory.create();
            LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
            AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                    if (currentAccessToken == null) {
                        db.deleteUsers();
                        Intent logout = new Intent(SettingsActivity.this, LoginActivity.class);
                        overridePendingTransition(0, 0);
                        SettingsActivity.this.startActivity(logout);
                        SettingsActivity.this.finish();
                    }
                }
            };
        }
        else if (session.isLoggedIn()) {
            LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
            loginButton.setVisibility(View.GONE);

            btnLogout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    logoutUser();
                }
            });
        }
        else{
            logoutUser();
        }

        //Gets the user-details (name and UID)
        HashMap<String, String> user = db.getUserDetails();
        String name = user.get("name");
        String email = user.get("email");

        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);

        //Set the ActionBar
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.actionbar_main, null);

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

        TextView txt = (TextView) findViewById(R.id.title_text);
        Typeface font = Typeface.createFromAsset(getAssets(), "expressway.ttf");
        txt.setTypeface(font);
        txt.setText(R.string.settings);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);

        imgButton = (ImageButton) findViewById(R.id.menu);
        imgButton.setImageResource(R.drawable.menu);
        rotForward = AnimationUtils.loadAnimation(this, R.anim.button_rotate);
        rotBack = AnimationUtils.loadAnimation(this, R.anim.button_rotate_back);

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

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!menu.isMenuShowing()) {
                    imgButton.startAnimation(rotForward);
                    imgButton.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imgButton.setImageResource(R.drawable.previous);
                        }
                    }, 100);
                }
                else {
                    imgButton.setImageResource(R.drawable.menu);
                    imgButton.startAnimation(rotBack);
                }
                menu.toggle();
            }
        });

        Button events = (Button) findViewById(R.id.btnEvents);
        events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0,0);
            }
        });

        Button settings = (Button) findViewById(R.id.btnSettings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgButton.setImageResource(R.drawable.menu);
                imgButton.startAnimation(rotBack);
                menu.toggle();
            }
        });
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    public void onBackPressed(){
        if (menu.isMenuShowing()){
            imgButton.setImageResource(R.drawable.menu);
            imgButton.startAnimation(rotBack);
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
}