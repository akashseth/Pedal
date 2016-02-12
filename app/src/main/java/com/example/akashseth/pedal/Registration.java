package com.example.akashseth.pedal;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class Registration extends BaseActivity {

    EditText countryCodeField, mobileNoField, passwordField;
    Button registerButton;
    private ProgressDialog pDialog;
    String countryCode, mobileNo, mobileNoWithCountryCode, password;
    public static final String url_server = "http://188.166.232.9/Cycling%20app/Services/signup.php";

    int success;

    JsonParser jsonParser = new JsonParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        commonDrawerConfigCall();
        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setTitle(Html.fromHtml(getString(R.string.registerActivity)));

        countryCodeField = (EditText) findViewById(R.id.countryCode);
        countryCodeField.setText("+" + GetCountryZipCode());
        mobileNoField = (EditText) findViewById(R.id.mobileNo);
        mobileNoField.setRawInputType(Configuration.KEYBOARD_12KEY);
        passwordField = (EditText) findViewById(R.id.password);
        registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                actionRegister();
            }
        });

    }

    public void skipSignUp(View view) {
        Intent signUpLater = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(signUpLater);
        finish();
    }

    public void goToLogin(View view) {
        Intent loginActivityIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginActivityIntent);
        finish();
    }

    public void alertIfAlreadyRegistered() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(Registration.this);
        dialog.setTitle(R.string.numberAlreadyRegistered);
        dialog.setMessage(R.string.numberAlreadyRegisteredMessage);
        dialog.setPositiveButton(getString(R.string.Login), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent intentForLogin = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intentForLogin);
                finish();
            }
        });
        dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

            }
        });
        dialog.show();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 3;
    }

    private boolean isMobileNoValid(String mobileNo) {
        return mobileNo.contains("[0-9]+");
    }

    protected void actionRegister() {
        countryCode = countryCodeField.getText().toString();
        //countryCode=countryCode.startsWith("+")?countryCode.substring(1):countryCode;
        mobileNo = mobileNoField.getText().toString();
        mobileNoWithCountryCode = countryCode + mobileNo;
        password = passwordField.getText().toString();

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
                    new NewAccount().execute();
        }
    }


    class NewAccount extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Registration.this);
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
            JSONObject obj = jsonParser.jsonForSingleObj();

            // check for success tag
            try {
                success = obj.getInt("result");

                if (success == 2) {

                    Intent intentForOtp = new Intent(getApplicationContext(), OtpVerification.class);
                    intentForOtp.putExtra("mobileNo", mobileNo);
                    startActivity(intentForOtp);
                    finish();

                } else if (success == 4) {
                    //alertIfAlreadyRegistered
                } else {
                    if (success == 1) {
                        // Transfer to otp verification
                        Intent intent = new Intent(getApplicationContext(), OtpVerification.class);
                        intent.putExtra("mobileNo", mobileNo);
                        startActivity(intent);
                        finish();

                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
            if (success == 4) {
                alertIfAlreadyRegistered();
            }
        }
    }
}
