package com.pedal.app.pedal;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class Help extends BaseActivity {

    TextView titelHelpText1, titelHelpText2, titelHelpText3, titelHelpText4, titelHelpText5, titelHelpText6, helpContentText1, helpContentText2, helpContentText3, helpContentText4, helpContentText5, helpContentText6;
    TextView profileName, profileMobNO;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        commonDrawerConfigCall();
        addDrawerItems();
        setupDrawer();

        profileName = (TextView) findViewById(R.id.profileName);
        profileMobNO = (TextView) findViewById(R.id.mobileNo);

        if(sessionManagement.isLoggedIn())
        {

            String[] profileDetails = sessionManagement.getProfileDetail();
            profileName.setText(profileDetails[0]);
            profileMobNO.setText("+" + profileDetails[1]);
        }

        getSupportActionBar().setTitle(Html.fromHtml(getString(R.string.helpActivity)));

        helpContentText1 = (TextView) findViewById(R.id.helpContent1);
        titelHelpText1 = (TextView) findViewById(R.id.titleHelp1);

        titelHelpText2 = (TextView) findViewById(R.id.titleHelp2);
        helpContentText2 = (TextView) findViewById(R.id.helpContent2);

        helpContentText3 = (TextView) findViewById(R.id.helpContent3);
        titelHelpText3 = (TextView) findViewById(R.id.titleHelp3);

        helpContentText4 = (TextView) findViewById(R.id.helpContent4);
        titelHelpText4 = (TextView) findViewById(R.id.titleHelp4);

        helpContentText5 = (TextView) findViewById(R.id.helpContent5);
        titelHelpText5 = (TextView) findViewById(R.id.titleHelp5);

        helpContentText6 = (TextView) findViewById(R.id.helpContent6);
        titelHelpText6 = (TextView) findViewById(R.id.titleHelp6);

        helpContentText2.setVisibility(View.GONE);
        helpContentText3.setVisibility(View.GONE);
        helpContentText4.setVisibility(View.GONE);
        helpContentText5.setVisibility(View.GONE);
        helpContentText6.setVisibility(View.GONE);
    }

    public void toggleContents1(View v) {
        helpContentText1.setVisibility(helpContentText1.isShown() ? View.GONE : View.VISIBLE);
    }

    public void toggleContents2(View v) {
        helpContentText2.setVisibility(helpContentText2.isShown() ? View.GONE : View.VISIBLE);
    }

    public void toggleContents3(View v) {
        helpContentText3.setVisibility(helpContentText3.isShown() ? View.GONE : View.VISIBLE);
    }

    public void toggleContents4(View v) {
        helpContentText4.setVisibility(helpContentText4.isShown() ? View.GONE : View.VISIBLE);
    }

    public void toggleContents5(View v) {
        helpContentText5.setVisibility(helpContentText5.isShown() ? View.GONE : View.VISIBLE);
    }

    public void toggleContents6(View v) {
        helpContentText6.setVisibility(helpContentText6.isShown() ? View.GONE : View.VISIBLE);
    }


}
