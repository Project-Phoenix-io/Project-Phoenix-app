package com.projectphoenix.projectphoenix;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        WebView aboutParagraphs = (WebView) findViewById(R.id.aboutParagraphs);
        String text;
        text = "<html><body style=\"background-color:#FAFAFA\"><p align=\"justify\" style=\"text-indent: 2rem\">  ";

        text+= "Project Phoenix is an app-based wildfire prevention system, designed for the average homeowner. The idea for the system originated in the wake of the 2017 California wildfire season, which is the most destructive wildfire season on record, with nearly 10,000 structures completely destroyed. We wanted to create a system that would allow families to protect their homes immediately, no matter where they are.";
        text+= "</p> <p align=\"justify\" style=\"text-indent: 2rem\">  ";
        text+= "Here at Santiago High School, we have worked together for months in order to make this dream a reality. We also reached out to the City of Corona Fire Department, as well as to local hardware stores for data and measurements in regard to fires and how to prevent and suppress them. From these sources we gathered vital information allowing us to create an optimal sprinkler system. The aid we’ve received from our community is immeasurable.";
        text+= "</p> <p align=\"justify\" style=\"text-indent: 2rem\">  ";
        text+= "We named the system Project Phoenix to represent the persistence and determination of the Californian people as we persevere through destruction caused by this disastrous event. It knocked us down, but we will always get back up and rise from the ashes, better and stronger than before.";
        text+= "</p></body></html>";
        aboutParagraphs.loadData(text, "text/html", "utf-8");
    }
}
