package sim.unihannover.java2017.rtls4;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


import okhttp3.*;
import okio.BufferedSink;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that requires username and password to login.
 */
public class LoginActivity extends AppCompatActivity
 {

    private UserLoginTask mAuthTask = null;

    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUserView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUserSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mUserSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });



        final Intent startRegistration = new Intent(this,RegistrationActivity.class );
        Button buttonregister = (Button) findViewById(R.id.email_register_button);
        buttonregister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                startActivity(startRegistration);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            }
        });
    }


    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mUserView.setError(null);
        mPasswordView.setError(null);

        String email = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        } else if (!isUserNameValid(email)) {
            mUserView.setError(getString(R.string.error_invalid_user));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUserNameValid(String email) {
        return email.length() > 2;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }



    /**
     * Login Task to authenticate the user via username and password
     */

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mUser = email;
            mPassword = password;
        }

        /**
         * Handles async communication with the server to login the user.
         * @param params
         * @return Returns a boolean whether the login was successful or not.
         */
        @Override
        protected Boolean doInBackground(Void... params) {

             final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            try {
                String loginCredentials = mUser + ":" + mPassword;
                byte[] authkey = android.util.Base64.encode(loginCredentials.getBytes(), Base64.NO_WRAP); // Mit Base64.DEFAULT wird newLine char geaddet
                byte[] decoded = android.util.Base64.decode(authkey, Base64.DEFAULT);


                Log.d("Test", "AuthKey =  " + new String(authkey));
                Log.d("Test", "Decoded =  " + new String(decoded));
                Log.d("Test", "SollKey =  " + "YWJjZGU6MTIzNDU2");
                Log.d("Test", "SollDecoded =  " + new String(android.util.Base64.decode("YWJjZGU6MTIzNDU2", Base64.DEFAULT)));


                LoginForm firebaseToken = new LoginForm(FirebaseInstanceId.getInstance().getToken());

                RequestBody body = RequestBody.create(JSON, new Gson().toJson(firebaseToken));

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(MapsActivity.SERVER + "/login")
                        .post(body)
                        .addHeader("authorization", "Basic " + new String(authkey)) //new String(authkey)
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = client.newCall(request).execute();
                String answer  = response.body().string();
                Log.d("Serverantwort = " , answer);
                Log.d("JsonString = " , new JsonParser().parse(new Gson().toJson(firebaseToken)).toString());

                if (answer.contains("secret")) {

                    String secret = new JSONObject(answer).getString("secret");
                    SharedPreferences userData = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor userDataEditor = userData.edit();
                    userDataEditor.putString("username", mUser);
                    userDataEditor.putString("secret", secret);
                    userDataEditor.commit();

                    Log.d("Successful login: " , secret);

                    MapsActivity.loggedInWithUsernameAndSecret(mUser, secret);
                    return true;
                }

                return false;

            }  catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        /**
         * Is called when the communication with the server is done.
         * @param success True, when login was successful.
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                finish();
            } else {
                Snackbar.make(findViewById(R.id.login_form), "Login fehlgeschlagen", Snackbar.LENGTH_LONG).setAction("",null).show();
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

}

