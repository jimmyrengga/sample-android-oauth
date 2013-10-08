package com.github.jimmyrengga.sample.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MainActivity extends Activity {

    private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;

    private TextView mOut;
    private String namesArray[];
    private AccountManager mAccountManager;
    private Spinner accountsSpinner;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOut = (TextView) findViewById(R.id.message);
        namesArray = getAccountNames();
        accountsSpinner = initializeSpinner(
                R.id.accounts_spinner, namesArray);

        Bundle extras = getIntent().getExtras();
        initializeFetchButton();
//        if (extras.containsKey(EXTRA_ACCOUNTNAME)) {
//            email = extras.getString(EXTRA_ACCOUNTNAME);
//            accountsSpinner.setSelection(getIndex(namesArray, email));
//            getTask(MainActivity.this, email, SCOPE, REQUEST_CODE_RECOVER_FROM_AUTH_ERROR)
//                    .execute();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private String[] getAccountNames() {
        mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR) {
            handleAuthorizeResult(resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleAuthorizeResult(int resultCode, Intent data) {
        if (data == null) {
            show("Unknown error, click the button again");
            return;
        }
        if (resultCode == RESULT_OK) {
            getTask(this, email, SCOPE, REQUEST_CODE_RECOVER_FROM_AUTH_ERROR).execute();
            return;
        }
        if (resultCode == RESULT_CANCELED) {
            show("User rejected authorization.");
            return;
        }
        show("Unknown error, click the button again");
    }

    public void showErrorDialog(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog d = GooglePlayServicesUtil.getErrorDialog(
                        code,
                        MainActivity.this,
                        REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                d.show();
            }
        });
    }

    private Spinner initializeSpinner(int id, String[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, values);
        Spinner spinner = (Spinner) findViewById(id);
        spinner.setAdapter(adapter);

        return spinner;
    }

    private void initializeFetchButton() {
        Button getToken = (Button) findViewById(R.id.get_info);
        getToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int accountIndex = accountsSpinner.getSelectedItemPosition();
                if (accountIndex < 0) {
                    // this happens when the sample is run in an emulator which has no google account
                    // added yet.

                    Toast.makeText(getApplicationContext(), "No Account available yet", Toast.LENGTH_LONG);
                    return;
                }

                email = namesArray[accountIndex];
                Log.i("email", "email : " + email);
                getTask(MainActivity.this, email, SCOPE,
                        REQUEST_CODE_RECOVER_FROM_AUTH_ERROR).execute();
            }
        });
    }

    public void show(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOut.setText(message);
            }
        });
    }

    private AbstractGetInfoTask getTask(
            MainActivity activity, String email, String scope, int requestCode) {

            return new GettingData(activity, email, scope, requestCode);

    }

    private int getIndex(String[] array, String element) {
        for (int i = 0; i < array.length; i++) {
            if (element.equals(array[i])) {
                return i;
            }
        }
        return 0;  // default to first element.
    }
    
}
