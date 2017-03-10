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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chist.testprojectmosru.Application.LaunchApplication;
import com.example.chist.testprojectmosru.Application.LocationHolder;
import com.example.chist.testprojectmosru.Application.SocialUtils;
import com.example.chist.testprojectmosru.Application.Utils;
import com.example.chist.testprojectmosru.Dialogs.Dialogs;
import com.example.chist.testprojectmosru.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 1 on 28.02.2017.
 */
public class NoteActivity extends BaseNoteActivity {
    private static final String AUTHURL = "https://api.instagram.com/oauth/authorize/";
    //Used for Authentication.
    private static final String TOKENURL ="https://api.instagram.com/oauth/access_token/";
    //Used for getting token and User details.
    public static final String APIURL = "https://api.instagram.com/v1/";
    //Used to specify the API version which we are going to use.
    public static String CALLBACKURL = "Your Redirect URI";
    //The callback url that we have used while registering the application.

    public static String HEADERTAG = "RESULT_HEADER";
    public static String BODYTAG = "RESULT_BODY";
    public static String MARKERTAG = "RESULT_MARKER";
    public static final int requestCodeUpdateData = 100500;
    public static final int requestCodeUpdateGPS = 100501;

    private HashMap<Integer, Intent> mapPendings = new HashMap<>(); // pendings for update anything
    private TextView header, body, gps;
    private ImageView photo;
    private int idNote;
    private Button changeLocation;

    private Note noteOld;
    private Note noteNew;

    // vk fields
    private ImageView facebook;
    private ImageView vk;
    public CallbackManager manager;

    // facebook info after login
    private long idFacebookUser;
    private String nameUser;

    // twitter
    private ImageView twitter;

    private ImageView instagram;


    private static String[] sMyScope = new String[]{VKScope.FRIENDS, VKScope.WALL, VKScope.PHOTOS, VKScope.NOHTTPS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.viewer_note));
        manager = CallbackManager.Factory.create();

        Bundle bundle = getIntent().getExtras();
        idNote = bundle.getInt(DBHelper.NoteColumns.ID);

        Cursor c = helper.getNote(idNote);
        noteOld = new Note(c);
        noteNew = (Note)noteOld.clone();

        header = (TextView) findViewById(R.id.header);
        body = (TextView) findViewById(R.id.body);
        photo = (ImageView) findViewById(R.id.notephoto);
        gps = (TextView) findViewById(R.id.gps);
        changeLocation = (Button)findViewById(R.id.changelocation);
        changeLocation = (Button)findViewById(R.id.changelocation);

        changeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NoteActivity.this, MapChangerActivity.class);
                i.putExtra(DBHelper.NoteColumns.ID, idNote);
                i.putExtra(DBHelper.NoteColumns.MAPX, noteNew.x == 0 ? 55.751244 : noteNew.x); // set default Moskow
                i.putExtra(DBHelper.NoteColumns.MAPY, noteNew.y  == 0 ? 37.618423 : noteNew.y);
                startActivity(i);
            }
        });

        if(noteNew.x != 0 || noteNew.y != 0)
            gps.setText("X: " + noteNew.x + "\n" + "Y: "  + noteNew.y);
        else
            gps.setText("GPS NO DATA");

        header.setText(noteNew.header);
        body.setText(noteNew.body);

        updateBackground(this, noteNew.marker);
        final Bitmap bitmap = Utils.getSavedBitmap(idNote, true);
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


        if(noteNew.x == 0 && noteNew.y == 0) {
            final ContentObserver observer = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    // Если данные свежие то добавляю последние с GPS
                    if (Math.abs(noteNew.timestamp - LocationHolder.getInstance(null).getLastTimeUpdate()) < LocationHolder.validDeltaTime) {
                        noteNew.x = noteOld.x = LocationHolder.getInstance(null).getLastX();
                        noteNew.y = noteOld.y = LocationHolder.getInstance(null).getLastY();
                        gps.setText("X: " + noteNew.x + "\n" + "Y: "  + noteNew.y);
                        getContentResolver().unregisterContentObserver(this);
                    }

                }
            };
            observers.add(observer);
        }

        facebook = (ImageView)findViewById(R.id.facebook);
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idFacebookUser != 0)
                    onFblogin();
                else {
                    Bitmap bitmap = Utils.getSavedBitmap(idNote, true);
                    SocialUtils.shareWithFaceBookDialog(NoteActivity.this, bitmap, noteNew);
                }

            }
        });
        vk = (ImageView)findViewById(R.id.vk);
        vk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.getID() == 0)
                    VKSdk.login(NoteActivity.this, sMyScope);
                else {
                    final Bitmap photo = Utils.getSavedBitmap(idNote, true);
                    SocialUtils.shareWithDialog(NoteActivity.this, photo, noteNew, getSupportFragmentManager());
                }
            }
        });
        instagram = (ImageView)findViewById(R.id.inst);
        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SocialUtils.createInstagramIntent(NoteActivity.this, idNote);
            }
        });
//        TwitterLoginButton btn =
//        twitter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });


        if(bitmap != null)
            photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog showDialog = new Dialogs.ShowPhoto(NoteActivity.this, bitmap);
                    showDialog.setCancelable(true);
                    showDialog.show();
                }
            });

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
                Dialog modifyDialog = new Dialogs.ModifyDialog(this, noteNew.header, noteNew.body, noteNew.marker);
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
                helper.deleteNote(String.valueOf(noteOld.id));
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
                noteNew.header = entry.getValue().getStringExtra(HEADERTAG);
                noteNew.body = entry.getValue().getStringExtra(BODYTAG);
                noteNew.marker = entry.getValue().getIntExtra(MARKERTAG, 0);
                header.setText(noteNew.header);
                body.setText(noteNew.body);
                updateBackground(this,  noteNew.marker);
                break;
            case requestCodeUpdateGPS: // update data after modify
                double x = entry.getValue().getDoubleExtra(DBHelper.NoteColumns.MAPX, noteNew.x);
                double y = entry.getValue().getDoubleExtra(DBHelper.NoteColumns.MAPY, noteNew.y);
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
    protected void onStop() {
        if (!noteOld.equals(noteNew)) { // equals is overrided
            ContentValues values = Utils.prepareContentValues(noteNew);
            if(noteNew.x != 0)
                values.put(DBHelper.NoteColumns.MAPX, noteNew.x);
            if(noteNew.y != 0)
                values.put(DBHelper.NoteColumns.MAPY, noteNew.y);
            helper.insertNote(values);
        }
        super.onStop();
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

    /*
       SOCIAL NETWORKS METHODS
    */

    // Private method to handle Facebook login and callback
    private void onFblogin()
    {
        manager = CallbackManager.Factory.create();

        // Set permissions
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "user_photos", "public_profile"));

        LoginManager.getInstance().registerCallback(manager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        Toast.makeText(NoteActivity.this, "Success login to facebook", Toast.LENGTH_LONG).show();
                        GraphRequest.newMeRequest(
                                loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
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

                                }).executeAsync();

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
