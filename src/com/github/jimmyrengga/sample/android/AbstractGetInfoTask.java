package com.github.jimmyrengga.sample.android;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jimmy on 10/8/13.
 */
public abstract class AbstractGetInfoTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "TokenInfoTask";
    private static final String NAME_KEY = "given_name";

    protected MainActivity mainActivity;
    protected String scope;
    protected String email;
    protected int requestCode;

    public AbstractGetInfoTask(MainActivity mainActivity, String scope, String email, int requestCode) {
        this.mainActivity = mainActivity;
        this.scope = scope;
        this.email = email;
        this.requestCode = requestCode;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try{
            fetchNameFromProfileServer();
        } catch(IOException ioe) {
            onError("Following Error occured, please try again. " + ioe.getMessage(), ioe);
        } catch(JSONException jsone) {
            onError("Bad response: " + jsone.getMessage(), jsone);
        }
        return null;
    }

    protected void onError(String msg, Exception e) {
        if (e != null) {
            Log.e(TAG, "Exception: ", e);
        }
        mainActivity.show(msg);
    }

    protected abstract String fetchToken() throws IOException;

    private void fetchNameFromProfileServer() throws IOException, JSONException {
        String token = fetchToken();
        if (token == null) {
            // error has already been handled in fetchToken()
            return;
        }
        URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        int sc = con.getResponseCode();
        if (sc == 200) {
            InputStream is = con.getInputStream();
            String name = getFirstName(readResponse(is));
            mainActivity.show("Hello " + name + "!");
            is.close();
            return;
        } else if (sc == 401) {
            GoogleAuthUtil.invalidateToken(mainActivity, token);
            onError("Server auth error, please try again.", null);
            Log.i(TAG, "Server auth error: " + readResponse(con.getErrorStream()));
            return;
        } else {
            onError("Server returned the following error code: " + sc, null);
            return;
        }
    }

    private static String readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while ((len = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, len);
        }
        return new String(bos.toByteArray(), "UTF-8");
    }

    /**
     * Parses the response and returns the first name of the user.
     * @throws JSONException if the response is not JSON or if first name does not exist in response
     */
    private String getFirstName(String jsonResponse) throws JSONException {
        JSONObject profile = new JSONObject(jsonResponse);
        return profile.getString(NAME_KEY);
    }

}
