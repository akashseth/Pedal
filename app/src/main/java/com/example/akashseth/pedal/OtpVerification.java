package com.example.akashseth.pedal;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

public class OtpVerification extends BaseActivity {

    EditText mobileNoField, otpField, countryCodeField;
    Button button;
    String mobileNo, mobileNoWithCountryCode, otp, countryCode;
    private ProgressDialog pDialog;
    int success;


    public static final String url_server = "http://188.166.232.9/Cycling%20app/Services/authenticateUser.php";

    JsonParser jsonParser = new JsonParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        commonDrawerConfigCall();
        addDrawerItems();
        setupDrawer();
        getSupportActionBar().setTitle(Html.fromHtml(getString(R.string.optVerification)));

        mobileNoField = (EditText) findViewById(R.id.mobileNo);

        mobileNoField.setText(getIntent().getStringExtra("mobileNo"));

        otpField = (EditText) findViewById(R.id.otp);
        countryCodeField = (EditText) findViewById(R.id.countryCode);
        countryCodeField.setText("+" + GetCountryZipCode());

        button = (Button) findViewById(R.id.otp_button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                actionOtpVerification();
            }
        });
    }

    private boolean isOtpValid(String otp) {
        return otp.length() > 3;
    }

    private boolean isMobileNoIsValid(String mobileNo) {
        return mobileNo.contains("[0-9]+");
    }

    protected void actionOtpVerification() {
        countryCode = countryCodeField.getText().toString();
        //countryCode=countryCode.startsWith("+")?countryCode.substring(1):countryCode;
        mobileNo = mobileNoField.getText().toString();
        mobileNoWithCountryCode = countryCode + mobileNo;
        otp = otpField.getText().toString();

        View focusView = null;
        Boolean cancel = false;

        // Check for a valid password, if the user entered one.
        if (!isOtpValid(otp)) {
            otpField.setError(getString(R.string.error_invalid_password));
            focusView = otpField;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mobileNo) || mobileNo.length() != 10 || isMobileNoIsValid(mobileNo)) {
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
                new OtpVerify().execute();
        }
    }

    public void alertIfWrongCredential() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(OtpVerification.this);
        dialog.setTitle(getString(R.string.wrongCredentials));
        dialog.setMessage(getString(R.string.wrongCredentialMessage));

        dialog.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

            }
        });
        dialog.show();
    }

    class OtpVerify extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(OtpVerification.this);
            pDialog.setMessage(getString(R.string.loginWaitMessage));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(String... args) {


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("otp", otp));
            params.add(new BasicNameValuePair("mobile", mobileNoWithCountryCode));


            // getting JSON Array
            jsonParser.makeHttpRequest(url_server,
                    "POST", params);
            JSONObject obj = jsonParser.jsonForSingleObj();


            // check for success tag
            try {

                success = obj.getInt("result");
                if (success != 0) {
                    SessionManagement sessionManagement = new SessionManagement(getApplicationContext());
                    sessionManagement.createLoginSession(success);

                    String getMobileNO = obj.getString("mob");
                    sessionManagement.setProfileMobile(getMobileNO);

                    // Transfer to startPage
                    Intent i = new Intent(getApplicationContext(), EditProfile.class).putExtra("newUser",true);
                    startActivity(i);

                    finish();

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

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.alertRegistrationNotComplete));
        builder.setMessage(getString(R.string.alertRegistrationNotCompleteMessage));
        builder.setPositiveButton(getString(R.string.enterOtp), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }


}
