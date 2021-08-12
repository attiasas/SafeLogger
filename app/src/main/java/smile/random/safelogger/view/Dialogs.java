package smile.random.safelogger.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import smile.random.safelogger.R;
import smile.random.safelogger.logic.C;
import smile.random.safelogger.logic.InfoManager;
import smile.random.safelogger.logic.models.PreviewRecord;
import smile.random.safelogger.view.activities.MainActivity;

/**
 * Author : Assaf Attias
 * Static Class that holds all the Dialogs operations creation and display
 */
public class Dialogs
{
    /**
     * Create and show a dialog to insert a new log information.
     * Validate the input, updates the database and then gives feedback to the user
     * @param activity - main activity that the dialog is shown above.
     */
    public static void showDialogInsert(final MainActivity activity)
    {
        // init view
        final Dialog dialog = new Dialog(activity);

        dialog.setContentView(R.layout.add_dialog);
        dialog.setTitle("Add New Log Information");

        int width = (int)(activity.getResources().getDisplayMetrics().widthPixels * C.D_W);
        int height = (int)(activity.getResources().getDisplayMetrics().heightPixels * C.D_H);
        dialog.getWindow().setLayout(width,height);

        final EditText edtLogName = dialog.findViewById(R.id.addLogName);
        final EditText edtUser = dialog.findViewById(R.id.addEdtUser);
        final EditText edtPass = dialog.findViewById(R.id.addEdtPass);

        // set add action
        Button btnAdd = dialog.findViewById(R.id.btnGen);
        btnAdd.setOnClickListener(view -> {

            String logName = edtLogName.getText().toString().trim();
            String userName = edtUser.getText().toString().trim();
            String password = edtPass.getText().toString().trim();

            InfoManager.ValidationResult valRes = InfoManager.get().validateLog(logName,userName,password);
            switch (valRes)
            {
                case Legal:
                    boolean actionRes = InfoManager.get().addRecord(logName,userName,password);

                    if (actionRes)
                    {
                        dialog.dismiss();
                        activity.updateRecordList();
                        Toast.makeText(activity,"Record Added successfully", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(activity,"Record Creation Failed", Toast.LENGTH_SHORT).show();

                    break;
                case BadShortName:
                    edtLogName.setError("Log name cannot be empty.");
                    edtLogName.requestFocus();
                    return;
                case BadShortUserName:
                    edtUser.setError("Log user-name cannot be empty.");
                    edtUser.requestFocus();
                    return;
                case BadShortPassword:
                    edtPass.setError("Log password cannot be empty.");
                    edtPass.requestFocus();
                    return;
                case BadExist:
                    edtLogName.setError("Log name already exists.");
                    edtLogName.requestFocus();
                    return;
                default:
                    return;
            }
        });

        // set clear view action
        Button btnReset = dialog.findViewById(R.id.btnCopy);
        btnReset.setOnClickListener(view -> {
            // reset view
            edtLogName.setText("");
            edtUser.setText("");
            edtPass.setText("");
        });

        dialog.show();
    }

    /**
     * Create and show a Alert Dialog to delete a given record from the database.
     * Validate a confirmation from the user, updates the database and then gives feedback to the user
     * @param activity - main activity that the dialog is shown above.
     * @param record - the given record the user wants to delete
     */
    public static void showDialogDelete(final MainActivity activity, final PreviewRecord record)
    {
        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(activity);

        dialogDelete.setTitle("Warning! (Delete Record)");
        dialogDelete.setMessage("Are you sure you want to delete " + record.getLogName() + "?");
        dialogDelete.setPositiveButton("Yes", (dialogInterface, i) -> {
            boolean actionRes = InfoManager.get().removeRecord(record);

            if (actionRes) Toast.makeText(activity,"Record Removed successfully", Toast.LENGTH_SHORT).show();
            else Toast.makeText(activity,"Record Remove Failed", Toast.LENGTH_SHORT).show();

            activity.updateRecordList();
        });
        dialogDelete.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
        dialogDelete.show();
    }

    /**
     * Create a warning dialog (with option to cancle)
     * @param activity - main activity that the dialog is shown above.
     * @return the dialog builder with all the information sets except the positive action.
     */
    public static AlertDialog.Builder showDialogWarning(final MainActivity activity)
    {
        final AlertDialog.Builder warningDialog = new AlertDialog.Builder(activity);
        warningDialog.setTitle("Warning! (Security Problem)");
        warningDialog.setMessage("Password has been used before, are you sure?");

        warningDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        return warningDialog;
    }

    /**
     *  Execute the update record action and transform and make changes to the view according to the result.
     * @param activity - main activity that the dialog is shown above.
     * @param dialog - the dialog that called the action.
     * @param record - the recorod that needs to be updated.
     * @param logName - the new log name.
     * @param userName - the new user-name of the log.
     * @param password - the new plain-text password of the log.
     */
    private static void makeUpdateAction(MainActivity activity,Dialog dialog,PreviewRecord record,String logName,String userName, String password)
    {
        boolean actionRes = InfoManager.get().updateRecord(record, logName, userName, password);

        if (actionRes) {
            dialog.dismiss();
            activity.updateRecordList();
            Toast.makeText(activity, "Record updated successfully", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(activity, "Record update failed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Create and show a dialog to generate random password.
     * @param activity - main activity that the dialog is shown above.
     */
    public static void showDialogGenerate(final MainActivity activity)
    {
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.generate_dialog);

        int width = (int)(activity.getResources().getDisplayMetrics().widthPixels * C.D_W);
        int height = (int)(activity.getResources().getDisplayMetrics().heightPixels * C.D_H);
        dialog.getWindow().setLayout(width,height);

        DecimalFormat decim = new DecimalFormat("0.00");

        SeekBar seekLen = dialog.findViewById(R.id.lenSeekBar);

        TextView txtLen = dialog.findViewById(R.id.lenNum);
        txtLen.setText(String.valueOf(seekLen.getProgress() + C.MIN_KEY_LEN));

        CheckBox digitCheck = dialog.findViewById(R.id.digitCheckBox);
        CheckBox upperCheck = dialog.findViewById(R.id.upperCheckBox);

        TextView txtPassGen = dialog.findViewById(R.id.textPassGen);
        txtPassGen.setVisibility(View.GONE);

        LinearLayout layoutRatio = dialog.findViewById(R.id.layoutChecked);
        layoutRatio.setVisibility(digitCheck.isChecked() || upperCheck.isChecked() ? View.VISIBLE : View.GONE);

        TextView txtRatio = dialog.findViewById(R.id.ratioNum);
        SeekBar seekRatio = dialog.findViewById(R.id.ratioSeekBar);

        txtRatio.setText(decim.format((double)(seekRatio.getProgress() + 50) / (seekRatio.getMax() + 50)));

        Button btnGen = dialog.findViewById(R.id.btnGen);
        Button btnCopy = dialog.findViewById(R.id.btnCopy);
        btnCopy.setEnabled(false);

        digitCheck.setOnCheckedChangeListener((compoundButton, b) -> layoutRatio.setVisibility(b || upperCheck.isChecked() ? View.VISIBLE : View.GONE));
        upperCheck.setOnCheckedChangeListener((compoundButton, b) -> layoutRatio.setVisibility(b || digitCheck.isChecked() ? View.VISIBLE : View.GONE));

        seekRatio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txtRatio.setText(decim.format((double)(seekBar.getProgress() + 50) / (seekBar.getMax() + 50)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        seekLen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txtLen.setText(String.valueOf(i + C.MIN_KEY_LEN));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        btnGen.setOnClickListener(view -> {

            int passLen = seekLen.getProgress() + C.MIN_KEY_LEN;
            boolean withDigit = digitCheck.isChecked();
            boolean withUpper = upperCheck.isChecked();
            double ratio = Double.parseDouble(txtRatio.getText().toString());

            txtPassGen.setText(InfoManager.generatePassword(passLen, withDigit, withUpper, ratio));

            btnCopy.setEnabled(true);
            txtPassGen.setVisibility(View.VISIBLE);
        });

        btnCopy.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("SafeLogger", txtPassGen.getText().toString().trim());
            clipboard.setPrimaryClip(clip);
        });

        dialog.show();
    }

    /**
     * Create and show a dialog to update a given log information record.
     * Validate the new input, updates the database and then gives feedback to the user
     * @param activity - main activity that the dialog is shown above.
     * @param record - the given record the user wants to update
     */
    public static void showDialogUpdate(final MainActivity activity, final PreviewRecord record)
    {
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.update_dialog);

        int width = (int)(activity.getResources().getDisplayMetrics().widthPixels * C.D_W);
        int height = (int)(activity.getResources().getDisplayMetrics().heightPixels * C.D_H);
        dialog.getWindow().setLayout(width,height);

        final EditText edtLogName = dialog.findViewById(R.id.updLogName);
        edtLogName.setText(record.getLogName());
        final EditText edtUserName = dialog.findViewById(R.id.updEdtUser);
        edtUserName.setText(record.getUserName());
        final EditText edtPassword = dialog.findViewById(R.id.updEedtPass);
        edtPassword.setText(record.getPassword());

        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(view -> {

            final String logName = edtLogName.getText().toString().trim();
            final String userName = edtUserName.getText().toString().trim();
            final String password = edtPassword.getText().toString().trim();

            InfoManager.ValidationResult valRes = InfoManager.get().validateLogUpdate(record,logName,userName,password);
            switch (valRes) {
                case WarnExist:
                    AlertDialog.Builder warningDialog = showDialogWarning(activity);
                    warningDialog.setPositiveButton("Yes", (dialogInterface, i) -> makeUpdateAction(activity,dialog,record,logName,userName,password));
                    warningDialog.show();
                    break;
                case Legal:
                    makeUpdateAction(activity,dialog,record,logName,userName,password);
                    break;
                case BadShortName:
                    edtLogName.setError("Log name cannot be empty.");
                    edtLogName.requestFocus();
                    return;
                case BadShortUserName:
                    edtUserName.setError("Log user-name cannot be empty.");
                    edtUserName.requestFocus();
                    return;
                case BadShortPassword:
                    edtPassword.setError("Log password cannot be empty.");
                    edtPassword.requestFocus();
                    return;
                case BadExist:
                    edtLogName.setError("Log name already exists.");
                    edtLogName.requestFocus();
                    return;
                default:
                    return;
            }
        });
        dialog.show();
    }

    /**
     * Create and show a dialog to update the authentication key.
     * @param activity - main activity that the dialog is shown above.
     */
    public static void showKeyUpdateDialog(final MainActivity activity)
    {
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.update_passkey);

        int width = (int)(activity.getResources().getDisplayMetrics().widthPixels * C.D_W);
        int height = (int)(activity.getResources().getDisplayMetrics().heightPixels * C.D_H);
        dialog.getWindow().setLayout(width,height);

        EditText edtKeyPass = dialog.findViewById(R.id.keyPass);
        Button btnUpdate = dialog.findViewById(R.id.btnUpdateKey);

        ProgressBar progressBar = dialog.findViewById(R.id.update_progressBar);
        progressBar.setVisibility(View.GONE);

        btnUpdate.setOnClickListener(view -> {
            // reset and init
            edtKeyPass.setError(null);
            String potentialKey = edtKeyPass.getText().toString().trim();
            // validate
            InfoManager.ValidationResult valRes = InfoManager.get().validateKey(potentialKey);
            switch (valRes)
            {
                case Legal:
                    final AlertDialog.Builder warningDialog = new AlertDialog.Builder(activity);
                    warningDialog.setTitle("Warning! - Change Password");
                    warningDialog.setMessage("Are you sure?");
                    warningDialog.setNegativeButton("Cancel", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        dialog.dismiss();
                    });
                    warningDialog.setPositiveButton("Yes", (dialogInterface, i) -> {
                        progressBar.setVisibility(View.VISIBLE);
                        btnUpdate.setVisibility(View.GONE);

                        AsyncTask<Void, Integer, Boolean> execute = new UpdateKeyTask(potentialKey,activity,dialogInterface,dialog,progressBar,btnUpdate).execute();
                    });
                    warningDialog.show();
                    break;
                case BadChars:
                    edtKeyPass.setError("Password must contain at least one digit, lower and upper chars.");
                    edtKeyPass.requestFocus();
                    return;
                case BadShort:
                    edtKeyPass.setError("Password must have at least " + C.MIN_KEY_LEN + " chars.");
                    edtKeyPass.requestFocus();
                    return;
                default:
                    return;
            }
        });

        dialog.show();
    }

    /**
     * Async Task to update key in the background
     */
    private static class UpdateKeyTask extends AsyncTask<Void,Integer,Boolean>
    {
        private String potentialKey;
        private Activity activity;
        private DialogInterface dialogInterface;
        private Dialog dialog;
        private ProgressBar progressBar;
        private Button button;

        public UpdateKeyTask(String potentialKey, Activity activity, DialogInterface dialogInterface, Dialog dialog, ProgressBar progressBar, Button button)
        {
            this.potentialKey = potentialKey;
            this.activity = activity;
            this.dialogInterface = dialogInterface;
            this.dialog = dialog;
            this.progressBar = progressBar;
            this.button = button;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            return InfoManager.get().updateKey(this.potentialKey);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success)
            {
                Toast.makeText(this.activity, "Authentication password updated successfully.", Toast.LENGTH_SHORT).show();
                dialogInterface.dismiss();
                dialog.dismiss();
                return;
            }
            else
            {
                Toast.makeText(this.activity, "Authentication password update failed.", Toast.LENGTH_SHORT).show();
                this.dialogInterface.dismiss();
                this.progressBar.setVisibility(View.GONE);
                this.button.setVisibility(View.VISIBLE);
            }
        }
    }
}
