package com.example.chist.testprojectmosru.Preview;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.example.chist.testprojectmosru.NotesActivityPackage.FirstLevelActivity;
import com.example.chist.testprojectmosru.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myMail = "chist-34dml@mail.ru";
                String customSubject = "Result";
                String body = "Anything result...";
                startActivity(prepareMailIntent(myMail, customSubject, body));
            }
        });

        findViewById(R.id.level1).setOnClickListener(this);
//        findViewById(R.id.level2).setOnClickListener(this);
//        findViewById(R.id.level3).setOnClickListener(this);
//        findViewById(R.id.level4).setOnClickListener(this);
//        findViewById(R.id.level5).setOnClickListener(this);
//        findViewById(R.id.level6).setOnClickListener(this);
//        findViewById(R.id.level7).setOnClickListener(this);
//        findViewById(R.id.level8).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.action_settings:
                // show info
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.level1:
                Intent intent = new Intent(this, FirstLevelActivity.class);
                startActivity(intent);
                break;
        }
    }

    // Send mail to my address
    public Intent prepareMailIntent (String to,String subject, String body){
        return new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + to + "?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(body)));
    }
}
