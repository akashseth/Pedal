package com.pedal.app.pedal;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Akash seth on 1/3/2016.
 */
public class SessionManagement {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Context context;

    int PRIVATE_MODE = 0;

    // SharedPreference file name
    private static final String PREF_NAME = "Spoof";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_USERID = "keyUserId";
    public static final String KEY_FULLNAME = "keyFullName";
    public static final String KEY_MobileNO = "keyMobileNO";


    public SessionManagement(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = preferences.edit();
    }

    public void createLoginSession(int userId) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putInt(KEY_USERID, userId);

        // commit changes
        editor.commit();
    }

    public void setProfileMobile(String mobileNo) {

        // Storing name in pref
        editor.putString(KEY_MobileNO, mobileNo);

        // commit changes
        editor.commit();
    }

    public void setProfileName(String fullName) {
        // Storing login value as TRUE
        editor.putString(KEY_FULLNAME, fullName);

        // commit changes
        editor.commit();
    }

    public void setProfile(String fullName, String mobileNo) {
        // Storing login value as TRUE
        editor.putString(KEY_FULLNAME, fullName);
        // Storing name in pref
        editor.putString(KEY_MobileNO, mobileNo);

        // commit changes
        editor.commit();
    }


    public boolean isLoggedIn() {
        return preferences.getBoolean(IS_LOGIN, false);
    }

    public int getUserId() {
        return preferences.getInt(KEY_USERID, 0);
    }

    public String[] getProfileDetail() {
        String[] profileDetail = new String[2];

        profileDetail[0] = preferences.getString(KEY_FULLNAME, "");
        profileDetail[1] = preferences.getString(KEY_MobileNO, "");

        return profileDetail;
    }


    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }


}
