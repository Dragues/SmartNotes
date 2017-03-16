package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by chist on 3/16/17.
 */

public class ObserversHolder {
    private ContentResolver resolver;
    private ArrayList<ContentObserver> listObservers;

    ObserversHolder(ContentResolver resolver){
        this.resolver = resolver;
        listObservers = new ArrayList<>();
    }
    public void register(Uri uri, boolean notifyForDescendants, ContentObserver observer){
        resolver.registerContentObserver(uri, notifyForDescendants, observer);
        listObservers.add(observer);
    }

    public void unregister(ContentObserver observer){
        resolver.unregisterContentObserver(observer);
        listObservers.remove(observer);
    }

    public void unregisterAll(){
        for (ContentObserver observer : listObservers)
            resolver.unregisterContentObserver(observer);
        listObservers.clear();
    }

    public void notifyChange(Uri uri, ContentObserver observer) {
        resolver.notifyChange(uri, observer);
    }

    public InputStream openInputStream(Uri uri) throws FileNotFoundException {
        return resolver.openInputStream(uri);
    }
}
