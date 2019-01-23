package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.Manifest;
import android.app.Dialog;
import android.app.MediaRouteButton;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TabHost;
import android.widget.Toast;

import com.example.chist.testprojectmosru.application.LaunchApplication;
import com.example.chist.testprojectmosru.application.NotesManager;
import com.example.chist.testprojectmosru.application.Utils;
import com.example.chist.testprojectmosru.dialogs.Dialogs;
import com.example.chist.testprojectmosru.R;
import com.example.chist.testprojectmosru.data.DatabaseHelper;
import com.example.chist.testprojectmosru.data.NoteDetails;
import com.facebook.FacebookSdk;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import timber.log.Timber;

/**
 * Created by 1 on 27.02.2017.
 */
public class MainNoteActivity extends BaseActivity {
    public static final Uri noteUri = Uri.parse("content://" + LaunchApplication.PACKAGE_NAME + "/db/notedata");
    public static final int SELECT_PHOTO = 100; // request code for photo
    public static String NOTETAG = "notetag"; // tag for sending serializable for NoteActivity

    private int idNoteOnUpdate = -1;
    private NoteAdapter adapter;
    private RecyclerView list;
    private TabHost tabHost;
    private NotesManager.NoteDelegate noteDelegate;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean isLoading;
    private ArrayList<NoteDetails> notesList;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!FacebookSdk.isInitialized())
            FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.firstlvllayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        list = (RecyclerView) findViewById(R.id.notelist);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        mSwipeRefreshLayout.setRefreshing(false);
        emptyView = findViewById(R.id.no_notes_view);
        mSwipeRefreshLayout.setOnRefreshListener(() -> refresh());
        notesList = new ArrayList<NoteDetails>();
        try {
            notesList.addAll(DatabaseHelper.getInstance().getNoteDao().queryForAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (notesList.isEmpty()) {
            showEmpty();
        } else {
        }

        adapter = new NoteAdapter(this, notesList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        findViewById(R.id.addnote).setOnClickListener(createNoteOnClickListener());

        prepareTabs();

        noteDelegate = new NotesManager.NoteDelegate() {
            @Override
            public void onNoteChanged(NoteDetails note) {

            }

            @Override
            public void onDataChanged(int id) {
                adapter.notifyDataSetChanged();
            }
        };
    }

    private void reloadNotes() {
        if (isLoading)
            return;
        isLoading = true;

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
                switch (tabId) {
                    case "alph":
                        adapter.sortItems(DatabaseHelper.Order.ALPHABETHEADER);
                        break;
                    case "time":
                        adapter.sortItems(DatabaseHelper.Order.TIME);
                        break;
                }
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
//                NoteDetails details = adapter.getItem(position);
//                updateNoteList();
//                Utils.deletesCachedImages(MainNoteActivity.this, details.id + "");
                return true;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = intent.getData();
                    try {
                        Bitmap yourSelectedImage = Utils.getCorrectlyOrientedImage(MainNoteActivity.this, selectedImage);
                        Utils.saveBitmap(yourSelectedImage, String.valueOf(idNoteOnUpdate));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (idNoteOnUpdate != -1) {
                        NotesManager.getInstance().notifyOnDataChanged(idNoteOnUpdate);
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

    public void updateNotesList() {
        notesList.clear();
        try {
            notesList.addAll(DatabaseHelper.getInstance().getNoteDao().queryForAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
        adapter.setItems(notesList);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermission(new PermissionActivity.PermissionHandler() {
            @Override
            public void onPermissionGranted() {
            }

            @Override
            public void onPermissionDenied() { // start app without loading custom book
            }
        });
    }

    public void refresh() {
        if (adapter != null) {
            isLoading = false;
            updateNotesList();
            mSwipeRefreshLayout.setRefreshing(false);
            if (notesList.isEmpty()) {
                showEmpty();
            } else {
                showContent();
            }
        }
    }

    protected void showContent() {
        emptyView.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
    }

    protected void showEmpty() {
        Timber.d("show empty", new Object[0]);
        emptyView.setVisibility(View.VISIBLE);
        list.setVisibility(View.GONE);
    }
}
