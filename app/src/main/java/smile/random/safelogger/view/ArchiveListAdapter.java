package smile.random.safelogger.view;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import smile.random.safelogger.R;
import smile.random.safelogger.logic.C;
import smile.random.safelogger.logic.models.RecordArchive;

/**
 * Author : Assaf Attias
 * An Implementation of a List Adapter for a ListView that holds a record archive in each row.
 */
public class ArchiveListAdapter extends BaseAdapter
{
    /**
     * a row representation view that holds the data of the archive and connect it to the view (row)
     */
    private class ViewHolder
    {
        public TextView startDate, endDate, days, password;
    }

    private Context context;
    private int layout;
    private LayoutInflater inflater;
    private List<RecordArchive> archiveList;
    private boolean showPass;

    /**
     * Constructor
     * @param context - the context (activity) that holds the record list
     * @param list - the record archives list data to show in the list
     */
    public ArchiveListAdapter(Context context, List<RecordArchive> list)
    {
        this.context = context;
        this.layout = R.layout.row_archive;
        this.inflater = LayoutInflater.from(context);
        this.archiveList = list;

        this.showPass = false;
    }

    @Override
    public int getCount() { return archiveList.size(); }
    @Override
    public Object getItem(int i) { return archiveList.get(i); }
    @Override
    public long getItemId(int i) { return i; }

    /**
     * Set the password field in the adapter to show/hide the password's plain-text
     * @param b - true will display plain-text, false will hide it.
     */
    public void setShowPass(boolean b) { showPass = b; }

    /**
     * Check if the adapter displays or hides the plain-text password
     * @return true if the plain-text is shown, flase otherwise
     */
    public boolean isShowPass() { return showPass; }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup)
    {
        View row = view;
        ViewHolder holder = new ViewHolder();

        if(row == null)
        {
            row = inflater.inflate(layout,null);
            holder.startDate = row.findViewById(R.id.showStart);
            holder.endDate = row.findViewById(R.id.showEnd);
            holder.days = row.findViewById(R.id.showDays);
            holder.password = row.findViewById(R.id.showPass);

            row.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)row.getTag();
        }

        RecordArchive model = archiveList.get(position);

        holder.startDate.setText(model.getStartDate());
        holder.endDate.setText(model.getEndDate());
        holder.days.setText(String.valueOf(model.getDayCount()));

        if(showPass)
        {
            holder.password.setText(model.getPassword());
        }
        else
        {
            holder.password.setText(C.PASSWORD_HIDDEN);
        }
//
//        DisplayMetrics dm = new DisplayMetrics();
//        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int SCREEN_WIDTH = dm.widthPixels;
//        holder.startDate.setWidth((int)(SCREEN_WIDTH * 0.25));
//        holder.endDate.setWidth((int)(SCREEN_WIDTH * 0.25));
//        holder.days.setWidth((int)(SCREEN_WIDTH * 0.1));
//        holder.password.setWidth((int)(SCREEN_WIDTH * 0.4));

        return row;
    }
}
