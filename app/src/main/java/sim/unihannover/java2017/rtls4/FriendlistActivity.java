package sim.unihannover.java2017.rtls4;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

//import com.google.android.gms.vision.Frame;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Displays all friends.
 */

public class FriendlistActivity extends AppCompatActivity {

    /**
     * authentication stores the authentication for Logging Out
     */
    public static String authentication = "";

    public FloatingActionButton FriendsAddfloatingActionButton;

    ListView myListView;
    ArrayAdapter<String> adapter;

    /**
     * this Method initializes the FriendlistActivity and sets an onClick Listener
     * on FriendsAddfloatingActionButton
     */

    public void init(){
        FriendsAddfloatingActionButton = (FloatingActionButton)findViewById(R.id.FriendsAddfloatingActionButton);
        FriendsAddfloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendlistActivity.this, AddFriendsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            }
        });

    }

    /**
     * Actualize the friendlist.
     */
    public void actualize(){
        ShowFriendsTask sFt = new ShowFriendsTask();
        sFt.execute();
    }


    /**
     * Initialize an onClick Listener on every Element in the Friendlist, to start a Dialog.
     * The user can confirm his action in the pop-up window.
     */

    public void initDialog(){
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(FriendlistActivity.this);



                builder.setMessage("Freund entfernen?");
                //.setTitle(R.string.dialog_title);

                final String friendName = adapter.getItem(position);
                final SharedPreferences userData = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


                builder.setPositiveButton("entfernen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        /**
                         * A Class that is called when a friend gets removed. Sends a request to the server.
                         */
                        class RemoveFriendTask extends AsyncTask<Void, Void, Void> {

                            @Override
                            protected Void doInBackground(Void... params) {
                                try {

                                    final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                                    RemoveFriendForm friend = new RemoveFriendForm(friendName);
                                    String username = userData.getString("username", null);

                                    Log.d("FriendName", friendName);
                                    Log.d("Array", friend.getRemoveZero());
                                    Log.d("User", username);
                                    Log.d("authentication", "Basic " + MapsActivity.getAuthentication());


                                    RequestBody body = RequestBody.create(JSON, new Gson().toJson(friend));

                                    OkHttpClient client = new OkHttpClient();

                                    Request request = new Request.Builder()
                                            .url("http://35.187.120.46/temp/users/" + username + "/friends")
                                            .post(body)
                                            .addHeader("authorization", "Basic " + MapsActivity.getAuthenticationEncoded())
                                            .addHeader("Content-Type", "application/json")
                                            .build();
                                    Response response = client.newCall(request).execute();
                                    String answer = response.body().string();
                                    int code = response.code();
                                    Log.d("Antwort", answer);
                                    Log.d("Code", Integer.toString(code));


                                    FriendlistActivity.this.actualize();
                                    return null;

                                } catch (Exception e) {
                                    System.out.println("fehler");
                                    e.printStackTrace();
                                    return null;
                                }
                            }

                        }
                        try {
                            new RemoveFriendTask().execute();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton("abbruch", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendlist);
        init();

    }

    /**
     * this Method creates the menu Button
     * @param menu is the menu it self
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logoutmenu,menu);
        return true;
    }

    /**
     * this Method detects if the menu Button is clicked
     * @param item is the Logout item in Menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Intent startLogin = new Intent(this, LoginActivity.class);
        int id = item.getItemId();
        if(id == R.id.OverFlowLogOutMenu){
            new LogoutTask(authentication).execute();
            finish();
            startActivity(startLogin);
            
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ShowFriendsTask sFt = new ShowFriendsTask();
        sFt.execute();
    }

    /**
     * this Method establishes connection with the Server and reqeuests all Friends of the User
     * puts them in an JSONArray
     * @return JArray is a JASONArray that includes all Friends of a particular User
     */
    public class ShowFriendsTask extends AsyncTask<Void, Void, JSONArray>{

        /**
         *
         * @param params
         * @return
         */
        @Override
        protected JSONArray doInBackground(Void... params) {

            String username = MapsActivity.getUsername();

            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                    .url(MapsActivity.SERVER + "/users/" + username + "/friends")
                    .get()
                    .addHeader("authorization", "Basic " + MapsActivity.getAuthenticationEncoded())
                    .build();

                Response response = client.newCall(request).execute();
                String answer = response.body().string();

                JSONArray JArray = new JSONArray(answer);

                Log.d("Antwort" , answer);

                if(answer != null){
                return JArray;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         *
         * @param JArray
         */
        @Override
        protected void onPostExecute(JSONArray JArray) {
            if(JArray == null){return;}

            if(JArray.length() > 0) {
                int length = JArray.length();
                String[] strArr = new String[length];
                for (int i = 0; i < length; i++) {
                    try {
                        strArr[i] = JArray.getString(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                myListView = (ListView) findViewById(R.id.FriendsListListView);
                adapter = new ArrayAdapter<String>(FriendlistActivity.this, android.R.layout.simple_expandable_list_item_1, strArr);
                myListView.setAdapter(adapter);

                initDialog();
            }
            else {
                String[] noFriends = {"Keine Freunde vorhanden"};
                myListView = (ListView) findViewById(R.id.FriendsListListView);
                adapter = new ArrayAdapter<String>(FriendlistActivity.this, android.R.layout.simple_expandable_list_item_1, noFriends);
                myListView.setAdapter(adapter);
            }
        }
    }

    /**
     *
     */
    private class LogoutTask extends AsyncTask<Void, Void, Void>{

        private final String mAuthentication;

        LogoutTask(String authentication) {
            mAuthentication = authentication;
        }

        /**
         *
         * @param params
         * @return
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(MapsActivity.SERVER + "/logout")
                        .addHeader("authorization", "Basic " + MapsActivity.getAuthenticationEncoded())
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(null, ""))
                        .build();
                client.newCall(request).execute();
                MapsActivity.loggedIn = false;
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().clear().commit();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}