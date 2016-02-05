package com.example.akashseth.pedal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EditProfile extends AppCompatActivity {

    EditText editTextFirstName, editTextLastName, editTextAge;
    RadioButton male, female;
    private ProgressDialog progressDialog;
    TextView fullNameText, mobileNoText;
    String fullName, mobileNO;
    View focusView = null;
    Boolean cancel = false;

    String gender, firstName, lastName, age, userId;
    public static final String url_server = "http://188.166.232.9/Cycling%20app/Services/editProfile.php";
    JsonParser jsonParser = new JsonParser();
    SessionManagement sessionManagement;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_tools);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Html.fromHtml(getString(R.string.editProfile)));

        editTextFirstName = (EditText) findViewById(R.id.firstname);
        editTextLastName = (EditText) findViewById(R.id.lastname);
        editTextAge = (EditText) findViewById(R.id.age);
        male = (RadioButton) findViewById(R.id.male);
        female = (RadioButton) findViewById(R.id.female);
        fullNameText = (TextView) findViewById(R.id.profileName);
        mobileNoText = (TextView) findViewById(R.id.profileMobNo);

        sessionManagement = new SessionManagement(getApplicationContext());

        String profileDetail[] = sessionManagement.getProfileDetail();
        fullName = profileDetail[0];
        mobileNO = "+" + profileDetail[1];

        fullNameText.setText(fullName);
        mobileNoText.setText(mobileNO);

    }

    protected Boolean checkIfFirstNameEmpty()
    {
        firstName=editTextFirstName.getText().toString();
        if(firstName.equals(""))
        {
            editTextFirstName.setError("This field is required!");
            focusView = editTextFirstName;
            return true;
        }
        return false;
    }

    protected void saveProfileToServer() {
        userId = Integer.toString(sessionManagement.getUserId());
        firstName = editTextFirstName.getText().toString();
        lastName = editTextLastName.getText().toString();
        age = editTextAge.getText().toString();
        if (male.isChecked()) {
            gender = "M";
        } else if (female.isChecked()) {
            gender = "F";
        }

        new EditDetail().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent startActivityIntent= new Intent(getApplicationContext(),StartActivity.class);
                startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startActivityIntent);
                return true;
            case R.id.save:
                if (checkIfFirstNameEmpty()) {
                    focusView.requestFocus();
                } else {
                    saveProfileToServer();

                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class EditDetail extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog=new ProgressDialog(EditProfile.this);
            progressDialog.setMessage("Updating Profile..");
            progressDialog.show();

        }

        protected String doInBackground(String... args) {


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("firstname", firstName));
            params.add(new BasicNameValuePair("lastname", lastName));
            params.add(new BasicNameValuePair("age", age));
            params.add(new BasicNameValuePair("gender", gender));
            params.add(new BasicNameValuePair("userid", userId));


            // getting JSON Array
            jsonParser.makeHttpRequest(url_server,
                    "POST", params);

            JSONObject jsonObject = jsonParser.jsonForSingleObj();


            // check for success tag
            try {
                String firstName = jsonObject.getString("firstname");
                String lastName = jsonObject.getString("lastname");
                firstName = firstName.equals("null") ? "" : firstName;
                lastName = lastName.equals("null") ? "" : lastName;
                fullName = firstName + " " + lastName;
                sessionManagement.setProfileName(fullName);


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            progressDialog.dismiss();
            Intent startActivityIntent= new Intent(getApplicationContext(),StartActivity.class);
            startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startActivityIntent);

        }


    }
}
