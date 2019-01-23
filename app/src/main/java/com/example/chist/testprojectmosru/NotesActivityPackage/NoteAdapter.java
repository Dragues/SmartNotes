package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chist.testprojectmosru.R;
import com.example.chist.testprojectmosru.data.DatabaseHelper;
import com.example.chist.testprojectmosru.data.NoteDetails;
import com.example.chist.testprojectmosru.holders.NoteViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.chist.testprojectmosru.NotesActivityPackage.MainNoteActivity.NOTETAG;

/**
 * Created by 1 on 27.02.2017.
 */
public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    public NoteDetails getItem(int position) {
        return mDetails.get(position);
    }

    public void setItems(ArrayList<NoteDetails> notesList) {
        mDetails = notesList;
        notifyDataSetChanged();
    }

    public enum SortType {
        SORT_SEARCH,
        SORT_FILTER
    }

    private List<NoteDetails> mDetails;
    protected Context ctx;

    public NoteAdapter(Context ctx, List<NoteDetails> mDetails) {
        this.ctx = ctx;
        this.mDetails = mDetails;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        NoteViewHolder vh = new NoteViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NoteViewHolder) {
            ((NoteViewHolder)holder).build(ctx, mDetails.get(position));
            holder.itemView.setOnClickListener(view -> {
                NoteDetails details = getItem(position);
                Intent i = new Intent(ctx, NoteActivity.class);
                i.putExtra(NOTETAG, details);
                ctx.startActivity(i);
            });
            holder.itemView.setOnLongClickListener(view -> {
                NoteDetails details = getItem(position);
                DatabaseHelper.getInstance().deleteNote(details);
                ((MainNoteActivity)ctx).refresh();
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDetails.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        ctx = recyclerView.getContext();
    }

    public void sortItems(DatabaseHelper.Order order) {
        Collections.sort(mDetails, new OrderComparator(order));
        notifyDataSetChanged();
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
    public long getItemId(int position) {
        return mDetails.get(position).id;
    }
}
