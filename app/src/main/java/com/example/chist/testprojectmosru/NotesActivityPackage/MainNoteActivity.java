package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.example.chist.testprojectmosru.Application.LaunchApplication;
import com.example.chist.testprojectmosru.Application.Utils;
import com.example.chist.testprojectmosru.Dialogs.Dialogs;
import com.example.chist.testprojectmosru.R;
import com.example.chist.testprojectmosru.data.DatabaseHelper;
import com.example.chist.testprojectmosru.data.NoteDetails;
import com.facebook.FacebookSdk;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by 1 on 27.02.2017.
 */
public class MainNoteActivity extends BaseActivity {
    public static final Uri noteUri = Uri.parse("content://" + LaunchApplication.PACKAGE_NAME + "/db/notedata");
    public static final int SELECT_PHOTO = 100; // request code for photo
    public static String NOTETAG = "notetag"; // tag for sending serializable for NoteActivity

    private int idNoteOnUpdate = -1;
    private NoteAdapter adapter;
    private ListView list;
    private TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!FacebookSdk.isInitialized())
            FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.firstlvllayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        list = (ListView) findViewById(R.id.notelist);
        ArrayList<NoteDetails> notesList = new ArrayList<NoteDetails>();
        try {
            notesList.addAll(DatabaseHelper.getInstance().getNoteDao().queryForAll());
        } catch (Exception e) {
            e.printStackTrace();
        }

        adapter = new NoteAdapter(this, notesList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(createItemClickListener());
        list.setOnItemLongClickListener(createItemLongClickListener());

        findViewById(R.id.addnote).setOnClickListener(createNoteOnClickListener());

        prepareTabs();
    }

    private void checkPermission(PermissionActivity.PermissionHandler handler) {
        try {
            BaseActivity activity = (BaseActivity) LaunchApplication.getInstance().getCurrentActivity();
            activity.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, handler);
        } catch (Exception ex) {
            //Timber.d(ex, "Exception while checking permissions");
            //Crashlytics.logException(new Error("can't request permissions", ex));
            handler.onPermissionDenied();
        }
    }

    private void prepareTabs() {
        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("alph");
        tabSpec.setContent(R.id.alphabet);
        tabSpec.setIndicator(getString(R.string.alphabet));
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("time");
        tabSpec.setContent(R.id.time);
        tabSpec.setIndicator(getString(R.string.time));
        tabHost.addTab(tabSpec);

        // tabListener
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                adapter.clear();
                List<NoteDetails> details = null;
                try {
                    details = DatabaseHelper.getInstance().getNoteDao().queryForAll();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                switch (tabId) {
                    case "alph":
                        Collections.sort(details, new OrderComparator(DatabaseHelper.Order.ALPHABETHEADER));
                        break;
                    case "time":
                        Collections.sort(details, new OrderComparator(DatabaseHelper.Order.TIME));
                        break;
                }
                adapter.addAll(details);
                adapter.notifyDataSetChanged();
            }
        });
        tabHost.setCurrentTab(0);
    }

    @NonNull
    private View.OnClickListener createNoteOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog addingDialog = new Dialogs.AddingDialog(MainNoteActivity.this, null);
                addingDialog.setCancelable(true);
                addingDialog.show();
            }
        };
    }

    @NonNull
    private AdapterView.OnItemLongClickListener createItemLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                NoteDetails details = adapter.getItem(position);
                updateNoteList();
                Utils.deletesCachedImages(MainNoteActivity.this, details.id + "");
                return true;
            }
        };
    }

    @NonNull
    private AdapterView.OnItemClickListener createItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NoteDetails details = adapter.getItem(position);
                Intent i = new Intent(MainNoteActivity.this, NoteActivity.class);
                i.putExtra(NOTETAG, details);
                startActivity(i);
            }
        };
    }

    public void setHeaderOnImageUpdate(int idNoteOnUpdate) {
        this.idNoteOnUpdate = idNoteOnUpdate;
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
                Dialog addingDialog = new Dialogs.AddingDialog(this, null);
                addingDialog.setCancelable(true);
                addingDialog.show();
                break;
            case R.id.infoapp:
                Dialog infoDialog = new Dialogs.InfoDialog(this);
                infoDialog.setCancelable(true);
                infoDialog.show();
                break;
            case R.id.deletenotes:
                Dialog dialogConfirm = new Dialogs.ConfirmDialog(this, new Runnable() {
                    @Override
                    public void run() {
                        DatabaseHelper.getInstance().deleteNotes();
                        observers.notifyChange(MainNoteActivity.noteUri, null);
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
        ContentObserver observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                updateNoteList();
            }
        }; // Observer for updating listView
        observers.register(noteUri, false, observer);
        updateNoteList();
        checkPermission(new PermissionActivity.PermissionHandler() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainNoteActivity.this, R.string.perm_storage_success, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPermissionDenied() { // start app without loading custom book
                Toast.makeText(MainNoteActivity.this, R.string.perm_storage_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateNoteList() {
        try {
            List<NoteDetails> details = DatabaseHelper.getInstance().getNoteDao().queryForAll();
            adapter.clear();
            Collections.sort(details, new OrderComparator(DatabaseHelper.Order.ALPHABETHEADER));
            adapter.addAll(details);
            adapter.notifyDataSetChanged();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public class OrderComparator implements Comparator<NoteDetails> {
        DatabaseHelper.Order order;

        public OrderComparator(DatabaseHelper.Order order) {
            this.order = order;
        }

        @Override
        public int compare(NoteDetails o1, NoteDetails o2) {
            switch (order) {
                case ALPHABETHEADER:
                    return o1.header.compareTo(o2.header);
                case TIME:
                    return o1.timestamp < (o2.timestamp) ? -1 : 1;
            }
            return 0;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = intent.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = observers.openInputStream(selectedImage);
                        Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                        Utils.saveBitmap(yourSelectedImage, String.valueOf(idNoteOnUpdate));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (idNoteOnUpdate != -1) {
                        adapter.getObservers().notifyChange(Utils.getImageUri(MainNoteActivity.this, idNoteOnUpdate), null);
                        idNoteOnUpdate = -1;
                    }
                    return;
                }
        }

        if (!VKSdk.onActivityResult(requestCode, resultCode, intent, createVKCallback())) {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @NonNull
    private VKCallback<VKAccessToken> createVKCallback() {
        return new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Toast.makeText(MainNoteActivity.this, "INVK", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(MainNoteActivity.this, "INVK", Toast.LENGTH_LONG).show();
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        };
    }

    int getMyId() {
        final VKAccessToken vkAccessToken = VKAccessToken.currentToken();
        return vkAccessToken != null ? Integer.parseInt(vkAccessToken.userId) : 0;
    }
}
