package apptastic.getpekt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import helper.SQLiteHandler;
import helper.SessionManager;


/**
 * Crops the image selected by the user by using the CropImageView library by edmodo
 * (We chose a fork of the project which had a few bugfixes, since we ran into some
 * bugs while developing, and we kept having to fix bugs in the library instead of
 * actually working on our own application)
 * @author AppTastic
 */
public class CropActivity extends ActionBarActivity {

    private SQLiteHandler user_db;
    private SessionManager session;
    private String uid;
    private Typeface express;
    private CropImageView img;

    private static final int SELECT_PICTURE = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        overridePendingTransition(0, 0);
        user_db = new SQLiteHandler(getApplicationContext());

        //Get the UID
        HashMap<String, String> user = user_db.getUserDetails();
        uid = user.get("email");

        FacebookSdk.sdkInitialize(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());
        express = Typeface.createFromAsset(getAssets(), "expressway.ttf");

        if (AccessToken.getCurrentAccessToken() == null && !session.isLoggedIn()) {
            logoutUser();
        }

        //Choose an Image
        Intent intent = new Intent();
        intent.setType("image/jpeg");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);


        img = (CropImageView) findViewById(R.id.CropImageView);

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
        txt.setText(R.string.title_activity_crop);

        final ImageButton imgButton = (ImageButton) findViewById(R.id.menu);
        imgButton.setImageResource(R.drawable.rotate);

        //Sets the rotate button
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.rotateImage(90);
            }
        });

        //Sets the OK button
        ImageButton select = (ImageButton) findViewById(R.id.add);
        select.setImageResource(R.drawable.success);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Scales the cropped image down to 128x128 and returns it as a result.
                Bitmap cropped = Bitmap.createScaledBitmap(img.getCroppedImage(), 128, 128, false);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", cropped);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    //Logs out user
    private void logoutUser() {

        session.setLogin(false);
        user_db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(CropActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Gets called after the user selected an image: if everything went OK the selected image
     * is set as the image to crop, else returns the user to the previous activity.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                try {
                    Uri selectedImageUri = data.getData();
                    img.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri));
                }
                catch (Exception ex) {

                }
            }
        } else {
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            finish();
            overridePendingTransition(0,0);
        }
    }


    /**
     *  returns user to previous activity when the back button is tapped twice (within 2 seconds).
     */
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    public void onBackPressed(){
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            finish();
            overridePendingTransition(0,0);
        }
        else { Toast.makeText(getBaseContext(), "Tap back button again to quit", Toast.LENGTH_SHORT).show(); }

        mBackPressed = System.currentTimeMillis();
    }
}