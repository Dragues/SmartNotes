package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.chist.testprojectmosru.Application.LaunchApplication;
import com.example.chist.testprojectmosru.Application.LocationHolder;
import com.example.chist.testprojectmosru.Application.Utils;
import com.example.chist.testprojectmosru.Dialogs.Dialogs;
import com.example.chist.testprojectmosru.R;
import com.example.chist.testprojectmosru.picasso.CropTransformation;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Observer;
import java.util.TimeZone;

/**
 * Created by 1 on 27.02.2017.
 */
// In this task i want to use CursorAdapter
public class NoteAdapter extends CursorAdapter {

    private Context ctx;
    private ArrayList<ContentObserver> listObservers = new ArrayList<>();

    protected static class ViewHolder {
        protected TextView header;
        protected TextView body;
        protected ImageView photo;
        private Uri imageUri;
        private int id;
        private TextView coords;
        private TextView timeupdated;
    }

    public NoteAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.ctx = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.note_item, null);
        ViewHolder holder = new ViewHolder();
        holder.header = (TextView) view.findViewById(R.id.header);
        holder.body = (TextView) view.findViewById(R.id.body);
        holder.photo = (ImageView) view.findViewById(R.id.photo);
        holder.coords = (TextView) view.findViewById(R.id.coords);
        holder.timeupdated = (TextView) view.findViewById(R.id.timeupdated);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        final String header = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.HEADER));
        final String body = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.BODY));
        final int marker = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.MARKER));
        final double X = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.MAPX));
        final double Y = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.MAPY));
        final long time = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.TIME));

        holder.id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.ID));
        holder.header.setText(header);
        holder.body.setText(body);

        view.findViewById(R.id.popup).setOnClickListener(new PopupListener(ctx, holder.id, header, body));

        // IMAGE (обновляем сколько угодно раз, оставляю наблюдатель пока жив адаптер)
        holder.imageUri = Utils.getImageUri(context, holder.id);
        holder.photo.setOnClickListener(new LoadImageListener(ctx, holder.id));
        Bitmap bitmap = Utils.getSavedBitmap(holder.id, false);
        if (bitmap != null) {
            //holder.photo.setImageBitmap(bitmap);
            Picasso.with(ctx)
                    .load(new File(Utils.getImagePathInDevice(false).getAbsolutePath() + "/" + holder.id))
                    .transform(new CropTransformation((int) (Math.min(bitmap.getHeight(), bitmap.getWidth()) / 2)))
                    .into(holder.photo);
        } else {
            holder.photo.setImageBitmap(BitmapFactory.decodeResource(ctx.getResources(),
                    R.drawable.no_data));
        }
        ContentObserver observer = createImageContentObserver(holder);
        ctx.getContentResolver().registerContentObserver(holder.imageUri, false, observer);
        listObservers.add(observer);

        //TIME
        holder.timeupdated.setText(ctx.getResources().getString(R.string.last_udpated) + getDateFromMillis(time));

        // GPS
        if (X != 0 && Y != 0)
            holder.coords.setText("X: " + X + "\n" + "Y: " + Y);
        else {
            holder.coords.setText(ctx.getResources().getString(R.string.no_data));
            final ContentObserver gpsObserver = createGPSContentObserver(context, holder, X, Y, time);
            ctx.getContentResolver().registerContentObserver(Utils.getGeoDataUriAdapter(context), false, gpsObserver);
            listObservers.add(gpsObserver);
        }
        view.setTag(holder);
    }

    private class PopupListener implements View.OnClickListener {

        private int id;
        private String body;
        private String header;

        public PopupListener(Context ctx, int id, String header, String body) {
            this.id = id;
            this.header = header;
            this.body = body;
        }

        @Override
        public void onClick(View v) {
            showContextMenu(v, id, header, body);
        }
    }

    private String getDateFromMillis(long time) {
        //1322018752992-Nov 22, 2011 9:25:52 PM
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Central"));
        calendar.setTimeInMillis(time);
        return sdf.format(calendar.getTime());
    }

    @NonNull
    private ContentObserver createGPSContentObserver(final Context context, final ViewHolder holder, final double x, final double y, final long time) {
        return new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                if ((x == 0 || y == 0) && System.currentTimeMillis() - time < LocationHolder.validDeltaTime) {
                    ctx.getContentResolver().notifyChange(Utils.getGeoDataUri(context), null);
                    holder.coords.setText("X: " + LocationHolder.getInstance(null).getLastX() + "\n" +
                            "Y: " + LocationHolder.getInstance(null).getLastY());
                    ctx.getContentResolver().unregisterContentObserver(this); // сделал единоразовую подписку
                    listObservers.remove(this);
                }

            }
        };
    }

    @NonNull
    private ContentObserver createImageContentObserver(final ViewHolder holder) {
        return new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                Bitmap bitmap = Utils.getSavedBitmap(holder.id, false);
                if (bitmap != null) {
                    holder.photo.setImageBitmap(bitmap);
                    Picasso.with(ctx)
                            .load(new File(Utils.getImagePathInDevice(false).getAbsolutePath() + holder.id))
                            .transform(new CropTransformation(Math.min(bitmap.getHeight(), bitmap.getWidth()) / 2))
                            .into(holder.photo);
                } else
                    holder.photo.setImageBitmap(BitmapFactory.decodeResource(ctx.getResources(),
                            R.drawable.no_data));
            }
        };
    }

    private class LoadImageListener implements View.OnClickListener {

        private int id;
        private Context ctx;

        public LoadImageListener(Context ctx, int id) {
            this.id = id;
            this.ctx = ctx;
        }

        @Override
        public void onClick(View v) {
            // Send action to gallery for choosing image
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            ((MainNoteActivity) ctx).setHeaderOnImageUpdate(id);
            photoPickerIntent.setType("image/*");
            ((Activity) ctx).startActivityForResult(photoPickerIntent, MainNoteActivity.SELECT_PHOTO);
        }
    }


    // creating filter_popup
    private void showContextMenu(View v, final int idNote, final String header, final String body) {
        final PopupWindow filterMenu = new PopupWindow(ctx);
        LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_window_edit, null);
        ListView lv = (ListView) layout.findViewById(R.id.editlist);
        lv.setAdapter(new FilterAdapter(ctx, idNote, header, body));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    runEdit(idNote);
                } else if (position == 1) {
                    runExport(idNote, "HEADER: " + header + " BODY: " + body);
                }
                filterMenu.dismiss();
            }
        });

        filterMenu.setContentView(layout);
        filterMenu.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        filterMenu.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        filterMenu.setFocusable(true);
        filterMenu.setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.dialog_shape));
        filterMenu.showAsDropDown(v);
    }

    private void runEdit(int idNote) {
        Cursor c = ((MainNoteActivity) ctx).helper.getNote(idNote);
        ContentValues values = Utils.getContentValuesFromCursor(c);
        Dialog dialog = new Dialogs.AddingDialog(ctx, values, ((MainNoteActivity) ctx).helper);
        dialog.setCancelable(true);
        dialog.show();
    }

    private void runExport(int id, String content) {
        if (!Utils.containsFile(String.valueOf(id)))
            Utils.exportToFile(ctx, String.valueOf(id), content);
        else
            Utils.deleteFileFromSd(ctx, String.valueOf(id));
    }

    private class FilterAdapter extends BaseAdapter {

        ArrayList<String> itemlabels;
        Context ctx;
        LayoutInflater lInflater;
        int id;
        String header;
        String body;

        public FilterAdapter(Context ctx, int id, String header, String body) {
            this.ctx = ctx;
            this.lInflater = (LayoutInflater) ctx
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.id = id;
            this.header = header;
            this.body = body;
            itemlabels = new ArrayList<>();
            itemlabels.add(ctx.getResources().getString(R.string.edit));
            itemlabels.add(ctx.getResources().getString(R.string.export));
        }

        @Override
        public int getCount() {
            return itemlabels.size();
        }

        @Override
        public String getItem(int position) {
            return itemlabels.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = lInflater.inflate(R.layout.popup_item, parent, false);
            view.findViewById(R.id.label).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.label)).setText(itemlabels.get(position));
            if (position == 0) {
                ((ImageView) view.findViewById(R.id.operationimage)).setBackground(ctx.getResources().getDrawable(R.drawable.icn_edit_dark));
            } else {
                ((ImageView) view.findViewById(R.id.operationimage)).setAlpha(Utils.containsFile(String.valueOf(id)) ? 1.0f : 0.5f);
            }
            return view;
        }
    }

    public void unregisterObservers() {
        for (ContentObserver observer : listObservers)
            ctx.getContentResolver().unregisterContentObserver(observer);
        listObservers.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        this.unregisterObservers();
        super.finalize();
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        this.unregisterObservers();
        return super.swapCursor(newCursor);
    }
}
