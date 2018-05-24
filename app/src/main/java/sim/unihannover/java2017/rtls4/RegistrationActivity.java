package sim.unihannover.java2017.rtls4;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A registration screen, that requires a username and password
 */
public class RegistrationActivity extends AppCompatActivity {
    
    private UserRegTask mAuthTask = null;

    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mUserView = (AutoCompleteTextView) findViewById(R.id.user_registration);

        mPasswordView = (EditText) findViewById(R.id.password_reg);
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

        Button mUserSignInButton = (Button) findViewById(R.id.register_button);
        mUserSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }



    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        mUserView.setError(null);
        mPasswordView.setError(null);

        String user = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(user)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        } else if (!isUserValid(user)) {
            mUserView.setError(getString(R.string.error_invalid_user));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mAuthTask = new UserRegTask(user, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUserValid(String user) {
        return user.length() > 2;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }



    /**
     * A Registration Task that registers the user via username and password
     */
    public class UserRegTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private final String mPassword;

        UserRegTask(String user, String password) {
            mUser = user;
            mPassword = password;
        }

        /**
         * Handles async communication with the server to register the user.
         * @param params
         * @return Returns a boolean whether the login was successful or not.
         */
        @Override
        protected Boolean doInBackground(Void... params) {

            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            try {

                RegistrationForm registrationToken = new RegistrationForm(mUser, mPassword);

                RequestBody body = RequestBody.create(JSON, new Gson().toJson(registrationToken));

                OkHttpClient client = new OkHttpClient();


                Request request = new Request.Builder()
                        .url(MapsActivity.SERVER + "/users")
                        .post(body)
                        .addHeader("authorization", "AMGOpYiXcg57uXkjBkwt9weRH8kabF87AbKVhG7b-OI=")
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = client.newCall(request).execute();

                Log.d("Serverantwort = " , response.body().string());
                Log.d("JsonString = " , new JsonParser().parse(new Gson().toJson(registrationToken)).toString());

                int answer = response.code();
                if(answer >= 400){
                    return false;
                }

                return true;

            }  catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        /**
         * Is called when the communication with the server is done.
         * @param success True, when registration was successful.
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {

                finish();

            } else {
                Snackbar.make(findViewById(R.id.register_form), "Benutzername bereits vergeben", Snackbar.LENGTH_LONG).setAction("",null).show();
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

}

