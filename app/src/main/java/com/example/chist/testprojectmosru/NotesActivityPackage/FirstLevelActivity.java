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
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.chist.testprojectmosru.Application.LaunchApplication;
import com.example.chist.testprojectmosru.Application.Utils;
import com.example.chist.testprojectmosru.Dialogs.Dialogs;
import com.example.chist.testprojectmosru.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;

/**
 * Created by 1 on 27.02.2017.
 */
public class FirstLevelActivity extends AppCompatActivity {

    public static final Uri noteUri = Uri.parse("content://" + LaunchApplication.getInstance().getPackageName() + "/db/notedata");
    public static String HEADERTAG = "header";
    public static String BODYTAG = "body";
    public static String MARKERTAG = "marker";
    public static String ID = "id";

    public static final int SELECT_PHOTO = 100;
    DBHelper helper;

    private ListView view;
    private int idNoteOnUpdate = -1;

    public void setHeaderOnImageUpdate(int idNoteOnUpdate) {
        this.idNoteOnUpdate = idNoteOnUpdate;
    }

    private NoteAdapter adapter;
    private LinkedList<ContentObserver> observers = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstlvllayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        helper = new DBHelper(this);
        view = (ListView) findViewById(R.id.notelist);
        adapter = new NoteAdapter(this, helper.getNotesCursor(), true);
        view.setAdapter(adapter);

        registerContentObservers();

        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) adapter.getItem(position);
                Intent i = new Intent(FirstLevelActivity.this, NoteActivity.class);
                i.putExtra(HEADERTAG, c.getString(c.getColumnIndex(DBHelper.NoteColumns.HEADER)));
                i.putExtra(BODYTAG, c.getString(c.getColumnIndex(DBHelper.NoteColumns.BODY)));
                i.putExtra(MARKERTAG, c.getInt(c.getColumnIndex(DBHelper.NoteColumns.MARKER)));
                i.putExtra(ID, c.getInt(c.getColumnIndex(DBHelper.NoteColumns.ID)));
                startActivity(i);
            }
        });
        view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) adapter.getItem(position);
                helper.deleteNote(c.getString(c.getColumnIndex(DBHelper.NoteColumns.HEADER)),
                        c.getString(c.getColumnIndex(DBHelper.NoteColumns.BODY)));
                Utils.deletesCachedImages(FirstLevelActivity.this, c.getInt(c.getColumnIndex(DBHelper.NoteColumns.ID)) + "");
                return true;
            }
        });
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
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (helper.isOpen())
            helper.close();
        for (ContentObserver item : observers) {
            getContentResolver().unregisterContentObserver(item);
        }
        observers.clear();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!helper.isOpen()){
            helper.open();
            adapter.swapCursor(helper.getNotesCursor()); // We can have modifications after NoteActivity.
                                                         // ....i can find another way for notify about modifications
        }
        if(observers.size() == 0)
            registerContentObservers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = FirstLevelActivity.this.getContentResolver().openInputStream(selectedImage);
                        Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                        Utils.saveBitmap(yourSelectedImage, String.valueOf(idNoteOnUpdate));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if(idNoteOnUpdate != -1) {
                        this.getContentResolver().notifyChange(Utils.getImageUri(idNoteOnUpdate),null);
                        idNoteOnUpdate = -1;
                    }
                }
        }
    }

}
