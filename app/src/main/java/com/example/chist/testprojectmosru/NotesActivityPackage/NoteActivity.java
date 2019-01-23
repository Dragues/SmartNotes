package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chist.testprojectmosru.application.LocationHolder;
import com.example.chist.testprojectmosru.application.SocialUtils;
import com.example.chist.testprojectmosru.application.Utils;
import com.example.chist.testprojectmosru.dialogs.Dialogs;
import com.example.chist.testprojectmosru.R;
import com.example.chist.testprojectmosru.data.DatabaseHelper;
import com.example.chist.testprojectmosru.data.NoteDetails;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 1 on 28.02.2017.
 */
public class NoteActivity extends BaseActivity {
    public static String HEADERTAG = "RESULT_HEADER";
    public static String BODYTAG = "RESULT_BODY";
    public static String MARKERTAG = "RESULT_MARKER";
    public static final int requestCodeUpdateData = 100500;
    public static final int requestCodeUpdateGPS = 100501;

    private HashMap<Integer, Intent> mapPendings = new HashMap<>(); // pendings for update anything
    private TextView header, body, gps;
    private int idNote;

    private NoteDetails noteOld;
    private NoteDetails noteNew;

    public CallbackManager manager; // facebook

    // facebook info after login
    private long idFacebookUser;
    private String nameUser;

    private static String[] sMyScope = new String[]{VKScope.FRIENDS, VKScope.WALL, VKScope.PHOTOS, VKScope.NOHTTPS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.viewer_note));

        manager = CallbackManager.Factory.create();
        NoteDetails details = (NoteDetails)getIntent().getExtras().getSerializable(MainNoteActivity.NOTETAG);
        idNote = details.id;
        noteOld = details;
        noteNew = (NoteDetails)noteOld.clone();
        updateBackground(this, noteNew.marker);
        header = (TextView) findViewById(R.id.header);
        header.setText(noteNew.header);
        body = (TextView) findViewById(R.id.body);
        body.setText(noteNew.body);
        Button changeLocation = (Button) findViewById(R.id.changelocation);
        changeLocation.setOnClickListener(createLocationOnClickListener());
        fillGps();
        fillPhoto();

        ImageView facebook = (ImageView) findViewById(R.id.facebook);
        facebook.setOnClickListener(createFacebookOnClickListener());
        ImageView vk = (ImageView) findViewById(R.id.vk);
        vk.setOnClickListener(createVKOnClickListener());
    }

    private void fillPhoto() {
        ImageView photo = (ImageView) findViewById(R.id.notephoto);
        final Bitmap bitmap = Utils.getSavedBitmap(idNote, true);
        if (bitmap != null) {
            photo.setImageBitmap(bitmap);
            photo.setOnClickListener(createPhotoOnClickListener(bitmap));
        }
        else {
            photo.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                    R.drawable.no_data));
        }
    }

    private void fillGps() {
        gps = (TextView) findViewById(R.id.gps);
        if(noteNew.x != 0 || noteNew.y != 0)
            gps.setText("X: " + noteNew.x + "\n" + "Y: "  + noteNew.y);
        else
            gps.setText("GPS NO DATA");
    }

    @NonNull
    private View.OnClickListener createLocationOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NoteActivity.this, MapChangerActivity.class);
                i.putExtra(DatabaseHelper.NoteColumns.ID, idNote);
                i.putExtra(DatabaseHelper.NoteColumns.MAPX, noteNew.x == 0 ? 55.751244 : noteNew.x); // set default Moskow
                i.putExtra(DatabaseHelper.NoteColumns.MAPY, noteNew.y  == 0 ? 37.618423 : noteNew.y);
                startActivity(i);
            }
        };
    }

    @NonNull
    private View.OnClickListener createFacebookOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idFacebookUser != 0)
                    onFblogin();
                else {
                    Bitmap bitmap = Utils.getSavedBitmap(idNote, true);
                    SocialUtils.shareWithFaceBookDialog(NoteActivity.this, bitmap, noteNew);
                }

            }
        };
    }

    @NonNull
    private View.OnClickListener createVKOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.getID() == 0)
                    VKSdk.login(NoteActivity.this, sMyScope);
                else {
                    final Bitmap photo = Utils.getSavedBitmap(idNote, true);
                    SocialUtils.shareWithDialog(NoteActivity.this, photo, noteNew, getSupportFragmentManager());
                }
            }
        };
    }

    @NonNull
    private View.OnClickListener createPhotoOnClickListener(final Bitmap bitmap) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog showDialog = new Dialogs.ShowPhoto(NoteActivity.this, bitmap);
                showDialog.setCancelable(true);
                showDialog.show();
            }
        };
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
                Dialog modifyDialog = new Dialogs.AddingDialog(this, noteNew);
                modifyDialog.setCancelable(true);
                modifyDialog.show();
                break;
            case R.id.revert:
                header.setText(noteOld.header);
                body.setText(noteOld.body);
                noteNew.header = noteOld.header;
                noteNew.body = noteOld.body;
                noteNew.x = noteOld.x;
                noteNew.y = noteOld.y;
                gps.setText("X: " + noteNew.x + "\n" + "Y: " + noteNew.y);
                break;
            case R.id.deletenotes:
                DatabaseHelper.getInstance().deleteNote(noteOld);
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
        manager.onActivityResult(requestCode, resultCode, data);
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
                NoteDetails details = (NoteDetails)entry.getValue().getSerializableExtra(HEADERTAG);
                noteNew = details;
                header.setText(noteNew.header);
                body.setText(noteNew.body);
                updateBackground(this,  noteNew.marker);
                break;
            case requestCodeUpdateGPS: // update data after modify
                double x = entry.getValue().getDoubleExtra(DatabaseHelper.NoteColumns.MAPX, noteNew.x);
                double y = entry.getValue().getDoubleExtra(DatabaseHelper.NoteColumns.MAPY, noteNew.y);
                if (x != noteNew.x || y != noteNew.y) {
                    noteNew.x = x;
                    noteNew.y = y;
                    gps.setText("X: " + noteNew.x + "\n" + "Y: " + noteNew.y);
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
    protected void onStart() {
        super.onStart();
        if (LocationHolder.onGPSUpdate != null) {
            noteNew.x = Double.parseDouble(LocationHolder.onGPSUpdate.split(MapChangerActivity.SEPARATOR)[0]);
            noteNew.y = Double.parseDouble(LocationHolder.onGPSUpdate.split(MapChangerActivity.SEPARATOR)[1]);
            gps.setText("X: " + noteNew.x + "\n" + "Y: " + noteNew.y);
            LocationHolder.onGPSUpdate = null;
        }
    }

    @Override
    protected void onStop() {
        if (!noteOld.checkEquals(noteNew)) {
            try {
                DatabaseHelper.getInstance().getNoteDao().createOrUpdate(noteNew);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        super.onStop();
    }

    /*
       SOCIAL NETWORKS METHODS
    */

    // Private method to handle Facebook login and callback
    private void onFblogin() {
        manager = CallbackManager.Factory.create();
        // Set permissions
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "user_photos", "public_profile"));

        LoginManager.getInstance().registerCallback(manager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(NoteActivity.this, "Success login to facebook", Toast.LENGTH_LONG).show();
                        GraphRequest.newMeRequest(
                                loginResult.getAccessToken(), createGraphJsonObjectCallback()).executeAsync();
                    }

                    @NonNull
                    private GraphRequest.GraphJSONObjectCallback createGraphJsonObjectCallback() {
                        return new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject json, GraphResponse response) {
                                if (response.getError() != null) {
                                    // handle error
                                    System.out.println("ERROR");
                                } else {
                                    System.out.println("Success");
                                    try {
                                        String jsonresult = String.valueOf(json);
                                        System.out.println("JSON Result" + jsonresult);
                                        //String str_email = json.getString("email");
                                        idFacebookUser = json.getLong("id");
                                        nameUser = json.getString("name");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        };
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(NoteActivity.this, "Canceled login to facebook", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(NoteActivity.this, "Error login to facebook", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
