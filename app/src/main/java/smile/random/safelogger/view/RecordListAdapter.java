package smile.random.safelogger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import smile.random.safelogger.R;
import smile.random.safelogger.logic.C;
import smile.random.safelogger.logic.models.PreviewRecord;

/**
 * Author : Assaf Attias
 * An Implementation of a List Adapter for a ListView that holds a record in each row.
 * this list support the following operations:
 *  * Search & Filter
 */
public class RecordListAdapter extends BaseAdapter
{
    /**
     * a row representation view that holds the data of the record and connect it to the view (row)
     */
    public class ViewHolder
    {
        public TextView txtLogName, txtUser, txtPass;
        public ImageView durationMarker;
        public PreviewRecord record;
        public boolean show;
    }

    private Context context;
    private int layout;
    private LayoutInflater inflater;

    private List<PreviewRecord> recordList;
    private ArrayList<PreviewRecord> showRecordList;

    /**
     * Constructor
     * @param context - the context (activity) that holds the record list
     * @param recordList - the record list data to show in the list
     */
    public RecordListAdapter(Context context, List<PreviewRecord> recordList)
    {
        this.context = context;
        this.layout = R.layout.row;
        this.inflater = LayoutInflater.from(context);

        this.recordList = new ArrayList<>(recordList);

        this.showRecordList = new ArrayList<>();
        setRecordList(recordList);
    }

    @Override
    public int getCount()
    {
        return this.showRecordList.size();
    }

    @Override
    public Object getItem(int i)
    {
        return this.showRecordList.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup)
    {
        View row = view;

        ViewHolder holder = new ViewHolder();
        if(row == null) // new record data, inflate and create the row with the holder of the data
        {
            row = this.inflater.inflate(this.layout,null);

            holder.txtLogName = row.findViewById(R.id.txtLogName);
            holder.txtUser = row.findViewById(R.id.txtUser);
            holder.txtPass = row.findViewById(R.id.txtPass);
            holder.durationMarker = row.findViewById(R.id.imgDurationMarker);

            holder.show = false;

            row.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)row.getTag();
        }

        // populate the holder with data
        PreviewRecord record = this.showRecordList.get(position);
        holder.record = record;
        // color count marker
        int currDayCount = record.getDayCount();
        int color = currDayCount < C.SHORT_THRESHOLD ? R.color.timeShort : currDayCount < C.MEDIUM_THRESHOLD ? R.color.timeMedium : R.color.timeLong;
        holder.durationMarker.setColorFilter(this.context.getResources().getColor(color,null));
        // text fields
        String logName = record.getLogName();
        if(logName.length() > C.MAX_TEXT_LEN)
            logName = logName.substring(0,C.MAX_TEXT_LEN) + "...";
        holder.txtLogName.setText(logName);

        String userName = record.getUserName();
        if(userName.length() > C.MAX_TEXT_LEN)
            userName = userName.substring(0,C.MAX_TEXT_LEN) + "...";
        holder.txtUser.setText(userName);
        holder.txtPass.setText(C.PASSWORD_HIDDEN);

        return row;
    }

    /**
     * Set the record data list and resets the filter (clear the filter)
     * @param list all the record data
     */
    public void setRecordList(List<PreviewRecord> list)
    {
        this.recordList = new ArrayList<>();
        this.recordList.addAll(list);
        filter("");
    }

    /**
     * Update the records that the list shows and filter entries that their log name not starts with the given filter txt.
     * @param charText - a filter txt, the prefix of the log names needed
     */
    public void filter(String charText)
    {
        String filterText = charText.trim().toLowerCase(Locale.getDefault());
        this.showRecordList.clear();

        if(filterText == null || filterText.isEmpty())
        {
            this.showRecordList.addAll(this.recordList);
        }
        else
        {
            for(PreviewRecord record : this.recordList)
            {
                if(record.getLogName().toLowerCase(Locale.getDefault()).contains(filterText))
                    this.showRecordList.add(record);
            }
        }

        notifyDataSetChanged();
    }
}
