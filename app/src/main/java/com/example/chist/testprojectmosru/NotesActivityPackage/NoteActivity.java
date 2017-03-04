package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chist.testprojectmosru.Application.LaunchApplication;
import com.example.chist.testprojectmosru.Application.Utils;
import com.example.chist.testprojectmosru.Dialogs.Dialogs;
import com.example.chist.testprojectmosru.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 1 on 28.02.2017.
 */
public class NoteActivity extends BaseNoteActivity {

    public static String HEADERTAG = "RESULT_HEADER";
    public static String BODYTAG = "RESULT_BODY";
    public static String MARKERTAG = "RESULT_MARKER";
    public static final int requestCodeUpdateData = 100500;
    public static final int requestCodeUpdateGPS = 100501;

    // need optimize logic
    private String headerOld;
    private String bodyOld;
    private String headerNew;
    private String bodyNew;
    private int markerLvlOld;
    private int markerLvlNew;
    private HashMap<Integer, Intent> mapPendings = new HashMap<>(); // pendings for update anything
    private TextView header, body, gps;
    private ImageView photo;
    private int idNote;
    private Point pointOld = new Point(0,0);
    private Point pointNew = new Point(0,0);
    private boolean needUpdateGps = false;
    private long timeAdded;
    private Button changeLocation;

    class Point {
        public double x;
        public double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.viewer_note));

        Bundle bundle = getIntent().getExtras();
        idNote = bundle.getInt(DBHelper.NoteColumns.ID);
        Cursor c = helper.getNote(idNote);
        headerOld = headerNew = c.getString(c.getColumnIndex(DBHelper.NoteColumns.HEADER));
        bodyOld = bodyNew = c.getString(c.getColumnIndex(DBHelper.NoteColumns.BODY));
        markerLvlOld = markerLvlNew = c.getInt(c.getColumnIndex(DBHelper.NoteColumns.MARKER));
        pointOld.x = pointNew.x =  c.getDouble(c.getColumnIndex(DBHelper.NoteColumns.MAPX));
        pointOld.y = pointNew.y =  c.getDouble(c.getColumnIndex(DBHelper.NoteColumns.MAPY));
        timeAdded = c.getInt(c.getColumnIndex(DBHelper.NoteColumns.TIME));

        header = (TextView) findViewById(R.id.header);
        body = (TextView) findViewById(R.id.body);
        photo = (ImageView) findViewById(R.id.notephoto);
        gps = (TextView) findViewById(R.id.gps);
        changeLocation = (Button)findViewById(R.id.changelocation);
        changeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NoteActivity.this, MapChangerActivity.class);
                i.putExtra(DBHelper.NoteColumns.ID, idNote);
                i.putExtra(DBHelper.NoteColumns.MAPX, pointNew.x);
                i.putExtra(DBHelper.NoteColumns.MAPY, pointNew.y);
                startActivity(i);
            }
        });

        if(pointNew.x != 0 || pointNew.y != 0)
            gps.setText("X: " + pointNew.x + "\n" + "Y: "  + pointNew.y);
        else
           gps.setText("GPS NO DATA");

        header.setText(headerNew);
        body.setText(bodyNew);

        updateBackground(this, markerLvlNew);
        Bitmap bitmap = Utils.getSavedBitmap(idNote, true);
        if (bitmap != null)
            photo.setImageBitmap(bitmap);
        else
            photo.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.no_data));
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // creating popup in fullscreen
            }
        });


        if(pointNew.x == 0 && pointNew.y == 0) {
            final ContentObserver observer = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    // Если данные свежие то добавляю последние с GPS
                    if (Math.abs(timeAdded - LaunchApplication.getInstance().getLasttimeUpdate()) < LaunchApplication.getInstance().validDeltaTime) {
                        pointNew = pointOld = new Point(LaunchApplication.getInstance().getLastX(), LaunchApplication.getInstance().getLastY());
                        gps.setText("X: " + pointNew.x + "\n" + "Y: "  + pointNew.y);
                        needUpdateGps = true;
                        getContentResolver().unregisterContentObserver(this);
                    }

                }
            };
            observers.add(observer);
        }
    }

    private void updateBackground(Context ctx, int markerLvl) {
        findViewById(R.id.totalnote).setBackgroundColor(Utils.getBackGroundColorFromMarker(ctx, markerLvl));
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
        switch (item.getItemId()) {
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
                pointNew.x = pointOld.x;
                pointNew.y = pointOld.y;
                gps.setText("X: " + pointNew.x + "\n" + "Y: " + pointNew.y);
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
        switch (entry.getKey()) {
            case requestCodeUpdateData: // update data after modify
                headerNew = entry.getValue().getStringExtra(HEADERTAG);
                bodyNew = entry.getValue().getStringExtra(BODYTAG);
                markerLvlNew = entry.getValue().getIntExtra(MARKERTAG, 0);
                header.setText(headerNew);
                body.setText(bodyNew);
                updateBackground(this, markerLvlNew);
                break;
            case requestCodeUpdateGPS: // update data after modify
                double x = entry.getValue().getDoubleExtra(DBHelper.NoteColumns.MAPX, pointNew.x);
                double y = entry.getValue().getDoubleExtra(DBHelper.NoteColumns.MAPY, pointNew.y);
                if (x != pointNew.x || y != pointNew.y) {
                    pointNew.x = x;
                    pointNew.y = y;
                    needUpdateGps = true;
                    gps.setText("X: " + pointNew.x + "\n" + "Y: " + pointNew.y);
                    break;
                }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        processPendingIntents();
    }

    @Override
    protected void onStop() {
        if (!bodyOld.equals(bodyNew) || !headerOld.equals(headerNew) || markerLvlNew != markerLvlOld || needUpdateGps) {
            ContentValues values = Utils.prepareContentValues(idNote, headerNew, bodyNew, markerLvlNew);
            if(pointNew.x != 0)
                values.put(DBHelper.NoteColumns.MAPX, pointNew.x);
            if(pointNew.y != 0)
                values.put(DBHelper.NoteColumns.MAPY, pointNew.y);
            helper.insertNote(values);
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (LaunchApplication.getInstance().onGPSUpdate != null) {
            pointNew.x = Double.parseDouble(LaunchApplication.getInstance().onGPSUpdate.split(MapChangerActivity.SEPARATOR)[0]);
            pointNew.y = Double.parseDouble(LaunchApplication.getInstance().onGPSUpdate.split(MapChangerActivity.SEPARATOR)[1]);
            gps.setText("X: " + pointNew.x + "\n" + "Y: " + pointNew.y);
            needUpdateGps = true;
            LaunchApplication.getInstance().onGPSUpdate = null;
        }
    }
}
