package smile.random.safelogger.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import smile.random.safelogger.R;
import smile.random.safelogger.logic.C;
import smile.random.safelogger.logic.InfoManager;
import smile.random.safelogger.logic.models.PreviewRecord;
import smile.random.safelogger.view.Dialogs;
import smile.random.safelogger.view.RecordListAdapter;

/**
 * Author : Assaf Attias
 * Main Activity of the application. in charge of displaying the list of records.
 * this activity has a Menu with extra actions:
 * *    Update Authentication-Key
 * *    Generate Random Password
 * *    Search and filter records
 * *    Clear the database and Import Log data and populate the database with it
 * *    Export the logging data as a file
 * Tap On row in list opens a menu to manipulate it:
 * *    show / hide the password (toggle)
 * *    Update (edit) the current log information
 * *    delete the current log
 * *    Go (show) to the full log information page
 */
public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private RecordListAdapter mRecordListAdapter;

    /**
     * Update the display of the record list in the activity
     */
    public void updateRecordList()
    {
        List<PreviewRecord> records = InfoManager.get().getRecordsPreview();
        if (records == null)
        {
            InfoManager.get().logOff();
            Toast.makeText(this,"Authentication Is Needed",Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mRecordListAdapter.setRecordList(records);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init
        mListView = findViewById(R.id.listView);

        List<PreviewRecord> records = InfoManager.get().getRecordsPreview();
        if (records == null)
        {
            InfoManager.get().logOff();
            Toast.makeText(this,"Authentication Is Needed.",Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (records.isEmpty())
            Toast.makeText(this,"Record list is empty.",Toast.LENGTH_LONG).show();

        mRecordListAdapter = new RecordListAdapter(this,records);
        mListView.setAdapter(mRecordListAdapter);

        // display row (log) actions on click
        mListView.setOnItemClickListener((adapterView, view, i, l) -> {
            final RecordListAdapter.ViewHolder holder = (RecordListAdapter.ViewHolder)view.getTag();

            CharSequence[] items = C.ROW_OPTIONS.clone();
            if(holder.show) items[C.TOGGLE_PASSWORD_SHOW] = C.HIDE_OPTION;

            final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

            dialog.setTitle(C.TAP_ACTION_TITLE);
            dialog.setItems(items, (dialogInterface, i1) -> {
                switch (i1)
                {
                    case C.TOGGLE_PASSWORD_SHOW:

                        if(holder.show) holder.txtPass.setText(C.PASSWORD_HIDDEN);
                        else holder.txtPass.setText(holder.record.getPassword());
                        holder.show = !holder.show;
                        break;
                    case C.UPDATE_ROW_DATA:
                        Dialogs.showDialogUpdate(MainActivity.this,holder.record);
                        mRecordListAdapter.setRecordList(InfoManager.get().getRecordsPreview());
                        break;
                    case C.DELETE_ROW:
                        Dialogs.showDialogDelete(MainActivity.this,holder.record);
                        break;
                    case C.SHOW_MORE_INFO:
                        Intent intent = new Intent(MainActivity.this,ExtendedInfoActivity.class);
                        intent.putExtra("record",holder.record);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            });

            dialog.show();
        });

        // Add new log button
        FloatingActionButton btnAddLog = findViewById(R.id.add_log_button);

        btnAddLog.setOnClickListener(view -> Dialogs.showDialogInsert(MainActivity.this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        // search action implementation
        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String s)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s)
            {
                if(TextUtils.isEmpty(s))
                {
                    mRecordListAdapter.filter("");
                    mListView.clearTextFilter();
                }
                else
                {
                    mRecordListAdapter.filter(s);
                }

                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // drop down menu - more actions
        int id = item.getItemId();

        switch (id)
        {
            case R.id.action_update_key:
                Dialogs.showKeyUpdateDialog(MainActivity.this);
                return true;
            case R.id.action_generate:
                Dialogs.showDialogGenerate(MainActivity.this);
                return true;
            case R.id.action_log_off:
                InfoManager.get().logOff();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
