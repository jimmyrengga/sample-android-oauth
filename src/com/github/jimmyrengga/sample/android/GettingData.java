package com.github.jimmyrengga.sample.android;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

/**
 * Created by jimmy on 10/8/13.
 */
public class GettingData extends AbstractGetInfoTask {

    public GettingData(MainActivity mainActivity, String scope, String email, int requestCode) {
        super(mainActivity, scope, email, requestCode);
    }

    @Override
    protected String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken(mainActivity, email, scope);
        } catch (GooglePlayServicesAvailabilityException playEx){
            mainActivity.showErrorDialog(playEx.getConnectionStatusCode());
        } catch (UserRecoverableAuthException e) {
            mainActivity.startActivityForResult(e.getIntent(), requestCode);
        } catch (GoogleAuthException fatalException) {
            onError("Unrecoverable error " + fatalException.getMessage(), fatalException);
        }
        return null;
    }
}
