package smile.random.safelogger.view.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import smile.random.safelogger.R;
import smile.random.safelogger.logic.C;
import smile.random.safelogger.logic.InfoManager;

/**
 * Author : Assaf Attias
 * Starting activity for authentication and log into the main application
 */
public class LoginActivity extends Activity {

    private boolean firstTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        TextView logInfo = findViewById(R.id.login_info);
        final EditText logPassword = findViewById(R.id.login_password);
        Button logSubmit = findViewById(R.id.login_confirm);

        ProgressBar progressBar = findViewById(R.id.login_progressBar);
        progressBar.setVisibility(View.GONE);

        InfoManager.initialize(this);

        firstTime = InfoManager.get().firstTime();

        if(firstTime)
        {
            logInfo.setText("Create an authentication password to secure and access logging information");
            logSubmit.setText("Create");
        }

        logSubmit.setOnClickListener(view -> {
            // reset and init
            logPassword.setError(null);
            String potentialKey = logPassword.getText().toString().trim();
            // validate
            InfoManager.ValidationResult valRes = InfoManager.get().validateKey(potentialKey);
            switch (valRes)
            {
                case Legal:
                    progressBar.setVisibility(View.VISIBLE);
                    logSubmit.setVisibility(View.GONE);
                    AsyncTask<Void, Integer, Boolean> execute = new LoginTask(potentialKey,logPassword,progressBar,logSubmit).execute();
                    break;
                case BadChars:
                    logPassword.setError("Password must contain at least one digit, lower and upper chars.");
                    logPassword.requestFocus();
                    return;
                case BadShort:
                    logPassword.setError("Password must have at least " + C.MIN_KEY_LEN + " chars.");
                    logPassword.requestFocus();
                    return;
                default:
                    return;
            }
        });
    }

    /**
     * Async Task for authentication and login in the background
     */
    private class LoginTask extends AsyncTask<Void,Integer,Boolean>
    {
        private String potentialKey;
        private EditText logPassword;
        private ProgressBar progressBar;
        private Button logSubmit;

        public LoginTask(String potentialKey, EditText logPassword, ProgressBar progressBar, Button logSubmit)
        {
            this.potentialKey = potentialKey;
            this.logPassword = logPassword;
            this.progressBar = progressBar;
            this.logSubmit = logSubmit;
        }
        @Override
        protected Boolean doInBackground(Void... params) {

            boolean actionRes = firstTime ? InfoManager.get().updateKey(this.potentialKey) : true;
            boolean authenticated = InfoManager.get().authentication(this.potentialKey);

            return actionRes && authenticated;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success)
            {
                this.logPassword.setText(""); // reset for log-off
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
            else
                this.logPassword.setError("Authentication Failed");

            this.progressBar.setVisibility(View.GONE);
            this.logSubmit.setVisibility(View.VISIBLE);
            this.logPassword.requestFocus();
        }
    }
}
