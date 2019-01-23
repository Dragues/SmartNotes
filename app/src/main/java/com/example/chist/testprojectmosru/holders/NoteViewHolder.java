package com.example.chist.testprojectmosru.holders;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.chist.testprojectmosru.NotesActivityPackage.MainNoteActivity;
import com.example.chist.testprojectmosru.NotesActivityPackage.NoteAdapter;
import com.example.chist.testprojectmosru.R;
import com.example.chist.testprojectmosru.application.LaunchApplication;
import com.example.chist.testprojectmosru.application.NotesManager;
import com.example.chist.testprojectmosru.application.Utils;
import com.example.chist.testprojectmosru.data.DatabaseHelper;
import com.example.chist.testprojectmosru.data.NoteDetails;
import com.example.chist.testprojectmosru.dialogs.Dialogs;
import com.example.chist.testprojectmosru.picasso.CropTransformation;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class NoteViewHolder extends RecyclerView.ViewHolder implements NotesManager.NoteDelegate {

    // each data item is just a string in this case
    protected View mView;
    protected TextView header;
    protected TextView body;
    protected ImageView photo;
    private Uri imageUri;
    private int id;
    private TextView coords;
    private TextView timeupdated;
    private ImageView popupActionsView;
    private NoteDetails details;
    protected Context mContext;

    public NoteViewHolder (View convertView) {
        super(convertView);
        mView = convertView;
        this.header = (TextView) convertView.findViewById(R.id.header);
        this.body = (TextView) convertView.findViewById(R.id.body);
        this.photo = (ImageView) convertView.findViewById(R.id.photo);
        this.coords = (TextView) convertView.findViewById(R.id.coords);
        this.popupActionsView = (ImageView) convertView.findViewById(R.id.popup_actions_view);
        this.timeupdated = (TextView) convertView.findViewById(R.id.timeupdated);

        NotesManager.getInstance().addDelegate(this);

    }
    public void build(Context context, NoteDetails noteDetails) {
        if (noteDetails == null)
            return;
        mContext = context;
        details = noteDetails;


        id = details.id;
        header.setText(details.header);
        body.setText(details.body);
        popupActionsView.setOnClickListener(new PopupListener(id, details.header, details.body));

        imageUri = Utils.getImageUri(LaunchApplication.getInstance(), id);
        photo.setOnClickListener(new LoadImageListener(id));
        Bitmap bitmap = Utils.getSavedBitmap(id, false);
        if (bitmap != null) {
            Picasso.with(context)
                    .load(new File(Utils.getImagePathInDevice(false).getAbsolutePath() + "/" + id))
                    .transform(new CropTransformation((int) context.getResources().getDimension(R.dimen.icon_width) * 2))
                    .skipMemoryCache()
                    .into(photo);
        } else {
            photo.setImageBitmap(BitmapFactory.decodeResource(LaunchApplication.getInstance().getResources(),
                    R.drawable.no_data));
        }
        timeupdated.setText(LaunchApplication.getInstance().getResources().getString(R.string.last_udpated) + Utils.getDateFromMillis(details.timestamp));

        // GPS
        if (details.x != 0 && details.y != 0)
            coords.setText("X: " + details.x + "\n" + "Y: " + details.y);
        else {
            coords.setText(LaunchApplication.getInstance().getResources().getString(R.string.no_data)); }
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

    // creating filter_popup
    private void showContextMenu(final View v, final int idNote, final String header, final String body) {
        final PopupWindow filterMenu = new PopupWindow(v.getContext());
        LayoutInflater layoutInflater = (LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_window_edit, null);
        ListView lv = (ListView) layout.findViewById(R.id.editlist);
        lv.setAdapter(new FilterAdapter(v.getContext(), idNote, header, body));
        lv.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                runEdit(v.getContext(), idNote);
            } else if (position == 1) {
                runExport(v.getContext(), idNote, "HEADER: " + header + " BODY: " + body);
            }
            filterMenu.dismiss();
        });

        filterMenu.setContentView(layout);
        filterMenu.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        filterMenu.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        filterMenu.setFocusable(true);
        filterMenu.setBackgroundDrawable(v.getContext().getResources().getDrawable(R.drawable.dialog_shape));
        filterMenu.showAsDropDown(v);
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


    @Override
    public void onNoteChanged(NoteDetails note) {
        if (details.id == note.id) {
            NoteDetails tmp = DatabaseHelper.getInstance().getNoteDao().noteById(note.id);
            if (tmp != null) {
                details = tmp;
                build(mContext,details);
            }
        }
    }

    @Override
    public void onDataChanged(int id) {
        if (details.id == id) {
            details = DatabaseHelper.getInstance().getNoteDao().noteById(id);
            build(mContext, details);
        }
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
}
