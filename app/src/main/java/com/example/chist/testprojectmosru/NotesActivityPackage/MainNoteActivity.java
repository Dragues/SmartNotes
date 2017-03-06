package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.app.Dialog;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chist.testprojectmosru.Application.LaunchApplication;
import com.example.chist.testprojectmosru.Application.Utils;
import com.example.chist.testprojectmosru.Dialogs.Dialogs;
import com.example.chist.testprojectmosru.R;

import com.facebook.FacebookSdk;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by 1 on 27.02.2017.
 */
public class MainNoteActivity extends BaseNoteActivity {

    public static final Uri noteUri = Uri.parse("content://" + LaunchApplication.getInstance().getPackageName() + "/db/notedata");
    public static final int SELECT_PHOTO = 100; // request code for photo

    private ListView view;
    private int idNoteOnUpdate = -1;
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!FacebookSdk.isInitialized())
            FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.firstlvllayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        view = (ListView) findViewById(R.id.notelist);
        adapter = new NoteAdapter(this, helper.getNotesCursor(), true);
        view.setAdapter(adapter);

        registerContentObservers();

        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) adapter.getItem(position);
                Intent i = new Intent(MainNoteActivity.this, NoteActivity.class);
                i.putExtra(DBHelper.NoteColumns.ID, c.getInt(c.getColumnIndex(DBHelper.NoteColumns.ID)));
                startActivity(i);
            }
        });
        view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) adapter.getItem(position);
                helper.deleteNote(c.getString(c.getColumnIndex(DBHelper.NoteColumns.HEADER)),
                        c.getString(c.getColumnIndex(DBHelper.NoteColumns.BODY)));
                Utils.deletesCachedImages(MainNoteActivity.this, c.getInt(c.getColumnIndex(DBHelper.NoteColumns.ID)) + "");
                return true;
            }
        });

        findViewById(R.id.addnote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog addingDialog = new Dialogs.AddingDialog(MainNoteActivity.this, null, helper);
                addingDialog.setCancelable(true);
                addingDialog.show();
            }
        });
    }

    public void setHeaderOnImageUpdate(int idNoteOnUpdate) {
        this.idNoteOnUpdate = idNoteOnUpdate;
    }

    private void registerContentObservers() {
        ContentObserver observer; // Observer for updating listView
        getContentResolver().registerContentObserver(noteUri, false, observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                adapter.swapCursor(helper.getNotesCursor());
            }
        });
        observers.add(observer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notemenu, menu);
        getMenuInflater().inflate(R.menu.clear, menu);
        getMenuInflater().inflate(R.menu.show_all_notices, menu);
        getMenuInflater().inflate(R.menu.info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_note:
                Dialog addingDialog = new Dialogs.AddingDialog(this, null, helper);
                addingDialog.setCancelable(true);
                addingDialog.show();
                break;
            case R.id.infoapp:
                Dialog infoDialog = new Dialogs.InfoDialog(this);
                infoDialog.setCancelable(true);
                infoDialog.show();
                break;
            case R.id.deletenotes:
                Dialog dialogConfirm = new Dialogs.ConfirmDialog(this,new Runnable() {
                    @Override
                    public void run() {
                       helper.deleteAll();
                    }
                });
                dialogConfirm.setCancelable(true);
                dialogConfirm.show();
                break;
            case R.id.show_all:
                Intent i = new Intent(MainNoteActivity.this, MapChangerActivity.class);
                i.putExtra(MapChangerActivity.LAUNCHMODETAG, true);
                startActivity(i);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.swapCursor(helper.getNotesCursor());
        if(observers.size() == 0)
            registerContentObservers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = intent.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = MainNoteActivity.this.getContentResolver().openInputStream(selectedImage);
                        Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                        Utils.saveBitmap(yourSelectedImage, String.valueOf(idNoteOnUpdate));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if(idNoteOnUpdate != -1) {
                        this.getContentResolver().notifyChange(Utils.getImageUri(idNoteOnUpdate),null);
                        idNoteOnUpdate = -1;
                    }
                    return;
                }
        }

        if (!VKSdk.onActivityResult(requestCode, resultCode, intent, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Toast.makeText(MainNoteActivity.this,"INVK", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(MainNoteActivity.this,"INVK", Toast.LENGTH_LONG).show();
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }


    int getMyId() {
        final VKAccessToken vkAccessToken = VKAccessToken.currentToken();
        return vkAccessToken != null ? Integer.parseInt(vkAccessToken.userId) : 0;
    }

}
