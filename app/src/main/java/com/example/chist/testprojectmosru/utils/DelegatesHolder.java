package com.example.chist.testprojectmosru.utils;

import org.apache.commons.collections4.Closure;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import timber.log.Timber;

public class DelegatesHolder<T> {
    public void add(T delegate) {
        final int idx = _itemIndex(delegate);
        if (idx < 0)
            mDelegates.add(new WeakReference<>(delegate));
    }

    public <U> void remove(U delegate) {
        final int idx = _itemIndex(delegate);
        if (idx >= 0)
            mDelegates.remove(idx);
    }

    public void removeAll() {
        mDelegates.clear();
    }

    public int size() {
        return mDelegates.size();
    }

    public void forAllDo(Closure<T> closure) {
        mDelegatesCopy.clear();
        mDelegatesCopy.addAll(mDelegates);

        for (WeakReference<T> wr : mDelegatesCopy)
            if (wr.get() != null)
                try {
                    closure.execute(wr.get());
                } catch (Exception e) {
                    //TODO: add to log
                    Timber.d(e, "Error delegate execute");
                }
    }

    public void removeNulled() {
        int i = mDelegates.size() - 1;
        while (i >= 0) {
            if (mDelegates.get(i).get() == null)
                mDelegates.remove(i);
            i--;
        }
    }

    private <U> int _itemIndex(U delegate) {
        int idx = mDelegates.size() - 1;
        while (idx >= 0 && mDelegates.get(idx).get() != delegate)
            idx--;

        return idx;
    }

    private List<WeakReference<T>> mDelegates = new CopyOnWriteArrayList<>();
    private List<WeakReference<T>> mDelegatesCopy = new CopyOnWriteArrayList<>();
}
