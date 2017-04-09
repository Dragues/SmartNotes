package com.example.chist.testprojectmosru.NotesActivityPackage;

/**
 * Created by 1 on 04.03.2017.
 */

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.chist.testprojectmosru.Application.LocationHolder;
import com.example.chist.testprojectmosru.R;
import com.example.chist.testprojectmosru.data.DatabaseHelper;
import com.example.chist.testprojectmosru.data.NoteDetails;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.SQLException;
import java.util.ArrayList;

public class MapChangerActivity extends BaseNoteActivity {
    public static String SEPARATOR = "&";
    public static String LAUNCHMODETAG = "launch_tag";

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private Marker curMarker;
    private Button saveBut;
    private boolean onSave;
    private ArrayList<NoteDetails> notes = new ArrayList<>();
    private double x = 55.751244; // default Moscow
    private double y = 37.618423;
    private boolean showMode;
    private Button nextFocus;
    private int curIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.map_layout);
        getSupportActionBar().setTitle(getResources().getString(R.string.map));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        showMode = getIntent().getBooleanExtra(LAUNCHMODETAG, false);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();
        if (map == null) {
            finish();
            return;
        }
        saveBut = (Button)findViewById(R.id.savebutton);
        nextFocus = (Button)findViewById(R.id.nextmarker);

        if(showMode)
            doInShowMode();
        else
            doInSetMode();
    }

    private void doInSetMode() {
        nextFocus.setVisibility(View.GONE);
        saveBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onSave)
                    LocationHolder.onGPSUpdate = x + SEPARATOR + y;
                finish();
            }
        });

        x = getIntent().getDoubleExtra(DatabaseHelper.NoteColumns.MAPX, 0);
        y = getIntent().getDoubleExtra(DatabaseHelper.NoteColumns.MAPY, 0);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(x, y))
                .zoom(15)
                .bearing(45)
                .tilt(20)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);
        curMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(x, y))
                .title(getResources().getString(R.string.curplace)));

        map.setOnMapClickListener(createOnMapClickListener());
    }

    private void doInShowMode() {
        saveBut.setVisibility(View.GONE);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(55.751244, 37.618423))
                .zoom(5)
                .bearing(45)
                .tilt(20)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);
        moveCursorToFirstIfNeed();
        if (notes.size() > 0) {
            cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(notes.get(0).x, notes.get(0).y))
                    .zoom(15)
                    .bearing(45)
                    .tilt(20)
                    .build();
            cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            map.animateCamera(cameraUpdate);
        }
        if(notes.size() < 2)
            nextFocus.setVisibility(View.GONE);
        else {
            nextFocus.setOnClickListener(createNextFocusOnClickListener());
        }
    }

    private void moveCursorToFirstIfNeed() {
        try {
            notes.addAll(helper.getNoteDao().queryForAll());
            for( NoteDetails details : notes) {
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(details.x, details.y))
                        .title(details.header));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private GoogleMap.OnMapClickListener createOnMapClickListener() {
        return new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (curMarker == null) {
                    curMarker = map.addMarker(new MarkerOptions().position(latLng));
                } else {
                    curMarker.setPosition(latLng);
                    x = latLng.latitude;
                    y = latLng.longitude;
                    onSave = true;
                }
            }
        };
    }

    @NonNull
    private View.OnClickListener createNextFocusOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng purpose = new LatLng(0,0);
                if (curIndex == notes.size()-1) {
                    purpose =   new LatLng(notes.get(0).x, notes.get(0).y);
                    curIndex = 0;
                }
                else {
                    purpose = new LatLng(notes.get(curIndex + 1).x, notes.get(curIndex + 1).y);
                    curIndex += 1;
                }
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(purpose)
                        .zoom(15)
                        .bearing(45)
                        .tilt(20)
                        .build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                map.animateCamera(cameraUpdate);
            }
        };
    }

    // has some constraints for communicates beetween activities
    private void sendPendingUpdateGPS(double x, double y) {
        Intent intent = new Intent("ANYTHINGTITLE" + "_" + System.currentTimeMillis()); // intents can be cached (it's may occur the errors), name can be anything
        Bundle b = new Bundle();
        b.putDouble(DatabaseHelper.NoteColumns.MAPX, x);
        b.putDouble(DatabaseHelper.NoteColumns.MAPY, y);
        intent.putExtras(b);
        PendingIntent pi = (createPendingResult(NoteActivity.requestCodeUpdateGPS, intent, 0));
        try {
            pi.send(MapChangerActivity.this, NoteActivity.requestCodeUpdateGPS, intent);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
