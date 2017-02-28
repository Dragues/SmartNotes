package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.chist.testprojectmosru.Application.Utils;
import com.example.chist.testprojectmosru.Dialogs.Dialogs;
import com.example.chist.testprojectmosru.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 1 on 28.02.2017.
 */
public class NoteActivity extends AppCompatActivity {

    public static String HEADERTAG = "RESULT_HEADER";
    public static String BODYTAG = "RESULT_BODY";

    private String noteHeader;
    private String noteBody;
    private int markerLvl;
    private HashMap<Integer,Intent> mapPendings = new HashMap<>(); // pendings for update anything
    private TextView header, body;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.viewer_note));

        Bundle bundle = getIntent().getExtras();
        noteHeader = bundle.getString(FirstLevelActivity.HEADERTAG);
        noteBody = bundle.getString(FirstLevelActivity.BODYTAG);
        markerLvl = bundle.getInt(FirstLevelActivity.MARKERTAG);

        header = (TextView)findViewById(R.id.header);
        body = (TextView)findViewById(R.id.body);
        header.setText(noteHeader);
        body.setText(noteBody);

        updateBackground(this, markerLvl);
    }

    private void updateBackground(Context ctx, int markerLvl) {
        findViewById(R.id.totalnote).setBackgroundColor(Utils.getBackGroundColorFromMarker(ctx,markerLvl));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        getMenuInflater().inflate(R.menu.clear, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.edit:
                Dialog modifyDialog = new Dialogs.ModifyDialog(this, noteHeader, noteBody);
                modifyDialog.setCancelable(true);
                modifyDialog.show();
                break;
            case R.id.deletenotes:
                finish();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mapPendings.put(requestCode, data);
    }

    // We can have several pending intents here after onActivityResult
    // All components in Android communicates via intents. We also will use this principals.
    private void processPendingIntents() {
        for (Map.Entry<Integer, Intent> entry : mapPendings.entrySet()) {
            processPendingIntent(entry);
        }
        mapPendings.clear();
    }

    private void processPendingIntent(Map.Entry<Integer, Intent> entry) {
        switch(entry.getKey()) {
            case 100500: // update data after modify
                noteHeader = entry.getValue().getStringExtra(HEADERTAG);
                noteBody = entry.getValue().getStringExtra(BODYTAG);
                header.setText(noteHeader);
                body.setText(noteBody);
                break;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        processPendingIntents();
    }
}
