package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chist.testprojectmosru.Application.LaunchApplication;
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
    public static String MARKERTAG = "RESULT_MARKER";

    // need optimize logic
    private String headerOld;
    private String bodyOld;
    private String headerNew;
    private String bodyNew;
    private int markerLvlOld;
    private int markerLvlNew;
    private HashMap<Integer,Intent> mapPendings = new HashMap<>(); // pendings for update anything
    private TextView header, body;
    private ImageView photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.viewer_note));

        Bundle bundle = getIntent().getExtras();
        headerOld = headerNew = bundle.getString(FirstLevelActivity.HEADERTAG);
        bodyOld = bodyNew = bundle.getString(FirstLevelActivity.BODYTAG);
        markerLvlOld = markerLvlNew =  bundle.getInt(FirstLevelActivity.MARKERTAG);

        header = (TextView)findViewById(R.id.header);
        body = (TextView)findViewById(R.id.body);
        photo = (ImageView)findViewById(R.id.notephoto);
        header.setText(headerNew);
        body.setText(bodyNew);

        updateBackground(this, markerLvlNew);
        Bitmap bitmap = Utils.getSavedBitmap(headerOld, true);
        if(bitmap != null)
            photo.setImageBitmap(bitmap);
        else
            photo.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.no_data));
    }

    private void updateBackground(Context ctx, int markerLvl) {
        findViewById(R.id.totalnote).setBackgroundColor(Utils.getBackGroundColorFromMarker(ctx,markerLvl));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        getMenuInflater().inflate(R.menu.revert, menu);
        getMenuInflater().inflate(R.menu.clear, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.edit:
                Dialog modifyDialog = new Dialogs.ModifyDialog(this, headerNew, bodyNew, markerLvlNew);
                modifyDialog.setCancelable(true);
                modifyDialog.show();
                break;
            case R.id.revert:
                header.setText(headerOld);
                body.setText(bodyOld);
                headerNew = headerOld;
                bodyNew = bodyOld;
                break;
            case R.id.deletenotes:
                    DBHelper helper = new DBHelper(this);
                    helper.deleteNote(headerNew, bodyNew);
                    helper.close();
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
                headerNew = entry.getValue().getStringExtra(HEADERTAG);
                bodyNew = entry.getValue().getStringExtra(BODYTAG);
                markerLvlNew = entry.getValue().getIntExtra(MARKERTAG, 0);
                header.setText(headerNew);
                body.setText(bodyNew);
                updateBackground(this,markerLvlNew);
                break;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        processPendingIntents();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!bodyOld.equals(bodyNew) || !headerOld.equals(headerNew) || markerLvlNew != markerLvlOld){
            DBHelper helper = new DBHelper(this);
            helper.deleteNote(headerOld, bodyOld);
            if (header != null)
                Utils.renameFiles(headerOld, headerNew);
            helper.insertNote(headerNew, bodyNew, markerLvlNew);
            helper.close();
        }
    }
}
