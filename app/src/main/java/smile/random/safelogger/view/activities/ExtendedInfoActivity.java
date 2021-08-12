package smile.random.safelogger.view.activities;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import smile.random.safelogger.R;
import smile.random.safelogger.logic.C;
import smile.random.safelogger.logic.InfoManager;
import smile.random.safelogger.logic.models.PreviewRecord;
import smile.random.safelogger.logic.models.RecordArchive;
import smile.random.safelogger.view.ArchiveListAdapter;

/**
 * Author : Assaf Attias
 * This activity in charge of displaying a log-record archive and extended information
 */
public class ExtendedInfoActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extend_info);

        // Init
        TextView txtLogName = findViewById(R.id.extend_txt_log_name);
        final TextView txtLogPassword = findViewById(R.id.extend_txt_password);
        TextView txtLogStart = findViewById(R.id.extend_txt_start_date);

        ImageButton btnBack = findViewById(R.id.extend_btn_back);
        final Button btnShow = findViewById(R.id.extend_btn_show);

        ProgressBar progressBar = findViewById(R.id.extend_progress_bar);
        TextView txtDayCount = findViewById(R.id.extend_txt_days);

        ListView archiveList = findViewById(R.id.extend_archive_list);

        // Get Data - record information and archive
        Intent intent = getIntent();
        final PreviewRecord logRecord = (PreviewRecord) intent.getSerializableExtra("record");
        List<RecordArchive> logArchive = InfoManager.get().getRecordArchive(logRecord.getId());

        // validate
        boolean decrypted = logArchive != null;
        for(int i = 0; decrypted && i < logArchive.size(); i++)
            decrypted = logArchive.get(i).getPassword() != null;

        if(logRecord == null || !decrypted || logRecord.getPassword() == null)
        {
            Toast.makeText(this,"Operation Failed, dec=" + decrypted + ", pass_null=" + (logRecord.getPassword() == null),Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (logArchive.isEmpty())
            Toast.makeText(this,"Archive is empty",Toast.LENGTH_LONG).show();

        // set
        String logName = logRecord.getLogName();
        if(logName.length() > C.MAX_TEXT_LEN)
            logName = logName.substring(0,C.MAX_TEXT_LEN) + "...";
        txtLogName.setText(logName);

        txtLogStart.setText(logRecord.getStartDate());
        txtLogPassword.setText(C.PASSWORD_HIDDEN);

        int currDayCount = logRecord.getDayCount();
        txtDayCount.setText(String.valueOf(currDayCount));

        int color = currDayCount < C.SHORT_THRESHOLD ? R.color.timeShort : currDayCount < C.MEDIUM_THRESHOLD ? R.color.timeMedium : R.color.timeLong;
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(color,null),android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar.setMax(C.MEDIUM_THRESHOLD);
        progressBar.setProgress(currDayCount < C.MEDIUM_THRESHOLD ? currDayCount : C.MEDIUM_THRESHOLD,true);

        final ArchiveListAdapter listAdapter = new ArchiveListAdapter(this,logArchive);
        archiveList.setAdapter(listAdapter);

        // show / hide passwords
        btnShow.setOnClickListener(view -> {

            listAdapter.setShowPass(!listAdapter.isShowPass());

            if (listAdapter.isShowPass())
            {
                txtLogPassword.setText(logRecord.getPassword());
                btnShow.setText("Hide Passwords");
            }
            else
            {
                txtLogPassword.setText(C.PASSWORD_HIDDEN);
                btnShow.setText("Show Passwords");
            }

            listAdapter.notifyDataSetChanged();
        });

        // back to main activity
        btnBack.setOnClickListener(view -> finish());
    }
}
