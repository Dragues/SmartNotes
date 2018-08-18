package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
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
import com.example.chist.testprojectmosru.data.DatabaseHelper;
import com.example.chist.testprojectmosru.data.NoteDetails;
import com.example.chist.testprojectmosru.picasso.CropTransformation;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by 1 on 27.02.2017.
 */
public class NoteAdapter extends ArrayAdapter<NoteDetails> {

    private ObserversHolder observers; // localObserver for adapter
    public ObserversHolder getObservers() {
        return observers;
    }

    public LayoutInflater inflater;

    protected static class ViewHolder {
        protected TextView header;
        protected TextView body;
        protected ImageView photo;
        private Uri imageUri;
        private int id;
        private TextView coords;
        private TextView timeupdated;
    }

    public NoteAdapter(BaseActivity activity, List<NoteDetails> notes) {
        super(activity, R.layout.note_item, notes);
        this.inflater = activity.getLayoutInflater();
        observers = new ObserversHolder(activity.getContentResolver());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;

        if (v == null) {
            v = inflater.inflate(R.layout.note_item, null);
            // cache view fields into the holder
            holder = new ViewHolder();
            holder.header = (TextView) v.findViewById(R.id.header);
            holder.body = (TextView) v.findViewById(R.id.body);
            holder.photo = (ImageView) v.findViewById(R.id.photo);
            holder.coords = (TextView) v.findViewById(R.id.coords);
            holder.timeupdated = (TextView) v.findViewById(R.id.timeupdated);
            // associate the holder with the view for later lookup
            v.setTag(holder);
        }
        else {
            // view already exists, get the holder instance from the view
            holder = (ViewHolder) v.getTag();
        }

        NoteDetails details = getItem(position);
        holder.id = getItem(position).id;
        holder.header.setText(details.header);
        holder.body.setText(details.body);

        v.findViewById(R.id.popup).setOnClickListener(new PopupListener(holder.id, details.header, details.body));

        // IMAGE (обновляем сколько угодно раз, оставляю наблюдатель пока жив адаптер)
        holder.imageUri = Utils.getImageUri(LaunchApplication.getInstance(), holder.id);
        holder.photo.setOnClickListener(new LoadImageListener(holder.id));
        Bitmap bitmap = Utils.getSavedBitmap(holder.id, false);
        if (bitmap != null) {
            //holder.photo.setImageBitmap(bitmap);
            Picasso.with(getContext())
                    .load(new File(Utils.getImagePathInDevice(false).getAbsolutePath() + "/" + holder.id))
                    .transform(new CropTransformation((int) (Math.min(bitmap.getHeight(), bitmap.getWidth()) / 2)))
                    .into(holder.photo);
        } else {
            holder.photo.setImageBitmap(BitmapFactory.decodeResource(LaunchApplication.getInstance().getResources(),
                    R.drawable.no_data));
        }
        ContentObserver observer = createImageContentObserver(LaunchApplication.getInstance(), holder);
        observers.register(holder.imageUri, false, observer);

        //TIME
        holder.timeupdated.setText(LaunchApplication.getInstance().getResources().getString(R.string.last_udpated) + getDateFromMillis(details.timestamp));

        // GPS
        if (details.x != 0 && details.y != 0)
            holder.coords.setText("X: " + details.x + "\n" + "Y: " + details.y);
        else {
            holder.coords.setText(LaunchApplication.getInstance().getResources().getString(R.string.no_data));
            final ContentObserver gpsObserver = createGPSContentObserver(LaunchApplication.getInstance(), holder, details.x, details.x, details.timestamp);
            observers.register(Utils.getGeoDataUriAdapter(LaunchApplication.getInstance()), false, gpsObserver);
        }
        return v;
    }

    private class PopupListener implements View.OnClickListener {
        private int id;
        private String body;
        private String header;

        public PopupListener(int id, String header, String body) {
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
                    observers.notifyChange(Utils.getGeoDataUri(context), null);
                    holder.coords.setText("X: " + LocationHolder.getInstance(null).getLastX() + "\n" +
                            "Y: " + LocationHolder.getInstance(null).getLastY());
                    observers.unregister(this);
                }

            }
        };
    }

    @NonNull
    private ContentObserver createImageContentObserver(final Context context, final ViewHolder holder) {
        return new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                Bitmap bitmap = Utils.getSavedBitmap(holder.id, false);
                if (bitmap != null) {
                    holder.photo.setImageBitmap(bitmap);
//                    Picasso.with(context)
//                            .load(new File(Utils.getImagePathInDevice(false).getAbsolutePath() + "/" + holder.id))
//                            .transform(new CropTransformation((int) (Math.min(bitmap.getHeight(), bitmap.getWidth()) / 2)))
//                            .into(holder.photo);
                } else
                    holder.photo.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.no_data));
            }
        };
    }

    private class LoadImageListener implements View.OnClickListener {
        private int id;

        public LoadImageListener(int id) {
            this.id = id;
        }

        @Override
        public void onClick(View v) {
            // Send action to gallery for choosing image
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            ((MainNoteActivity) v.getContext()).setHeaderOnImageUpdate(id);
            photoPickerIntent.setType("image/*");
            ((Activity) v.getContext()).startActivityForResult(photoPickerIntent, MainNoteActivity.SELECT_PHOTO);
        }
    }


    // creating filter_popup
    private void showContextMenu(final View v, final int idNote, final String header, final String body) {
        final PopupWindow filterMenu = new PopupWindow(v.getContext());
        LayoutInflater layoutInflater = (LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_window_edit, null);
        ListView lv = (ListView) layout.findViewById(R.id.editlist);
        lv.setAdapter(new FilterAdapter(v.getContext(), idNote, header, body));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    runEdit(v.getContext(), idNote);
                } else if (position == 1) {
                    runExport(v.getContext(), idNote, "HEADER: " + header + " BODY: " + body);
                }
                filterMenu.dismiss();
            }
        });

        filterMenu.setContentView(layout);
        filterMenu.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        filterMenu.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        filterMenu.setFocusable(true);
        filterMenu.setBackgroundDrawable(v.getContext().getResources().getDrawable(R.drawable.dialog_shape));
        filterMenu.showAsDropDown(v);
    }

    private void runEdit(Context context, int idNote) {
        NoteDetails details = DatabaseHelper.getInstance().getNote(idNote);
        Dialog dialog = new Dialogs.AddingDialog(context, details);
        dialog.setCancelable(true);
        dialog.show();
    }

    private void runExport(Context context, int id, String content) {
        if (!Utils.containsFile(String.valueOf(id)))
            Utils.exportToFile(context, String.valueOf(id), content);
        else
            Utils.deleteFileFromSd(context, String.valueOf(id));
    }

    private class FilterAdapter extends BaseAdapter {
        ArrayList<String> itemlabels;
        LayoutInflater lInflater;
        int id;
        String header;
        String body;

        public FilterAdapter(Context ctx, int id, String header, String body) {
            this.lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                ((ImageView) view.findViewById(R.id.operationimage)).setBackground(view.getContext().getResources().getDrawable(R.drawable.icn_edit_dark));
            } else {
                ((ImageView) view.findViewById(R.id.operationimage)).setAlpha(Utils.containsFile(String.valueOf(id)) ? 1.0f : 0.5f);
            }
            return view;
        }
    }
}
