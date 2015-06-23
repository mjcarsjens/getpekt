package helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Handles all the SQLite tables in the DataBase (login, contacts and icons).
 * @author AppTastic
 */
public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "AppTastic";

    // Login table name
    private static final String TABLE_LOGIN = "login";
    private static final String TABLE_CONTACTS = "contacts";
    private static final String TABLE_ICONS = "icons";


    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_EID = "eid";
    private static final String KEY_VERSION = "version";
    private static final String KEY_BLOB = "blob";


    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_LOGIN + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        String CREATE_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_UID + " TEXT PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_PHONE + " TEXT UNIQUE" + ")";
        db.execSQL(CREATE_TABLE);

        String CREATE_ICONS_TABLE = "CREATE TABLE " + TABLE_ICONS + "("
                + KEY_EID + " TEXT PRIMARY KEY," + KEY_VERSION + " INTEGER,"
                + KEY_BLOB + " BLOB" + ")";
        db.execSQL(CREATE_ICONS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }

    //Storing User Data
    public void addUser(String name, String email) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, email); // Email


        // Inserting Row
        long id = db.insert(TABLE_LOGIN, null, values);
        db.close(); // Closing database connection
    }



    //Get User
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_LOGIN;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
        }
        cursor.close();
        db.close();
        // return user
        return user;
    }

    //Delete all Users
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LOGIN, null, null);
        db.close();
    }

    //Add a Contact
    public void addContact(String uid, String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UID, uid);
        values.put(KEY_NAME, name); // Name
        values.put(KEY_PHONE, phone); // Email


        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }

    //Get all UIDs of your contacts
    public List<String> getUIDs() {
        List<String> names = new ArrayList<String>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0){
            do {
                names.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return user

        return names;
    }

    //Get the Name of the contact with UID uid
    public String getName(String uid) {
        String name = "";
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS + " WHERE uid = '" + uid + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            name = cursor.getString(1);
        }
        cursor.close();
        db.close();
        // return user

        return name;
    }

    //Check whether the user with phonenumber phone is in your contacts
    public Boolean userExists(String phone){
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS + " WHERE phone = '" + phone +"'";
        boolean exists = false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            exists = true;
        }
        cursor.close();
        db.close();
        return exists;
    }

    //Add an image (blob) to the database
    public void addBLOB(String eid, int version, byte[] blob) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EID, eid);
        values.put(KEY_VERSION, version); // Name
        values.put(KEY_BLOB, blob); // Email

        // Inserting Row
        db.insert(TABLE_ICONS, null, values);
        db.close(); // Closing database connection
    }

    //Get the version of the image
    public int getVersion(String eid){
        int version = 0;
        String selectQuery = "SELECT * FROM " + TABLE_ICONS + " WHERE " + KEY_EID + " = '" + eid + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            version = Integer.parseInt(cursor.getString(1));
        }
        cursor.close();
        db.close();
        return version;
    }

    //Get the Image (blob)
    public byte[] getBlob(String eid){
        byte[] blob = null;
        String selectQuery = "SELECT * FROM " + TABLE_ICONS + " WHERE " + KEY_EID + " = '" + eid + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            blob = cursor.getBlob(2);
        }
        cursor.close();
        db.close();
        return blob;
    }

    //Update the image
    public void updateBlob(String eid, int version, byte[] blob){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(KEY_VERSION, version);
        cv.put(KEY_BLOB, blob);

        db.update(TABLE_ICONS, cv, KEY_VERSION + "=" + Integer.toString(version), null);
        db.close();
    }

    //Delete the image
    public void deleteBlob(String eid){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ICONS, KEY_EID + "=" + eid, null);
        db.close();
    }
}