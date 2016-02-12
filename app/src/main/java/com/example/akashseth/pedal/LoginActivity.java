package com.example.akashseth.pedal;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends BaseActivity {

    String mobileNo, mobileNoWithCountryCode, password, countryCode;
    EditText mobileNoField, passwordField, countryCodeText;
    Button loginButton;
    private ProgressDialog pDialog;
    public static final String url_server = "http://188.166.232.9/Cycling%20app/Services/login.php";
    SessionManagement sessionManagement;
    int success;

    JsonParser jsonParser = new JsonParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        commonDrawerConfigCall();
        addDrawerItems();
        setupDrawer();
        getSupportActionBar().setTitle(Html.fromHtml(getString(R.string.loginActivity)));


        countryCodeText = (EditText) findViewById(R.id.countryCode);
        countryCodeText.setText("+" + GetCountryZipCode());
        mobileNoField = (EditText) findViewById(R.id.mobileNo);
        mobileNoField.setRawInputType(Configuration.KEYBOARD_12KEY);
        passwordField = (EditText) findViewById(R.id.password);
        loginButton = (Button) findViewById(R.id.login_Button);

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                actionLogin();
            }
        });

    }

    public void skipLogin(View view) {
        Intent skipLogin = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(skipLogin);
        finish();
    }

    public void goToRegister(View view) {
        Intent register = new Intent(getApplicationContext(), Registration.class);
        startActivity(register);
        finish();
        ;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 3;
    }

    private boolean isMobileNoValid(String mobileNo) {
        return mobileNo.contains("[0-9]+");
    }

    protected void actionLogin() {
        password = passwordField.getText().toString();
        countryCode = countryCodeText.getText().toString();
        // countryCode=countryCode.startsWith("+")?countryCode.substring(1):countryCode;
        mobileNo = mobileNoField.getText().toString();
        mobileNoWithCountryCode = countryCode + mobileNo;

        View focusView = null;
        Boolean cancel = false;

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password)) {
            passwordField.setError(getString(R.string.error_invalid_password));
            focusView = passwordField;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mobileNo) || mobileNo.length() != 10 || isMobileNoValid(mobileNo)) {
            mobileNoField.setError(getString(R.string.invalidMobileNo));
            focusView = mobileNoField;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            if(!isNetworkAvailable(getApplicationContext()))
                alertNetworkNotAvailable();
            else
            new Login().execute();
        }
    }

    public void alertIfWrongCredential() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
        dialog.setTitle(getString(R.string.wrongCredentials));
        dialog.setMessage(getString(R.string.wrongCredentialMessage));

        dialog.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

            }
        });
        dialog.show();
    }

    class Login extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage(getString(R.string.loginWaitMessage));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(String... args) {


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("contact", mobileNoWithCountryCode));


            // getting JSON Array
            jsonParser.makeHttpRequest(url_server,
                    "POST", params);

            JSONObject jsonObject = jsonParser.jsonForSingleObj();


            // check for success tag
            try {
                success = jsonObject.getInt("id");

                if (success != 0) {
                    sessionManagement = new SessionManagement(getApplicationContext());
                    sessionManagement.createLoginSession(success);

                    String getFullName = jsonObject.getString("nam");
                    String getMobileNO = jsonObject.getString("mob");
                    getFullName = getFullName.equals("null") ? "" : getFullName;
                    sessionManagement.setProfile(getFullName, getMobileNO);
                    // Transfer to otp verification
                    Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    ;
                    startActivity(intent);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
            if (success == 0) {
                alertIfWrongCredential();
            }
        }

    }

}

