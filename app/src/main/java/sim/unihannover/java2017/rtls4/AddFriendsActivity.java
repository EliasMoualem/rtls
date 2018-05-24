package sim.unihannover.java2017.rtls4;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Adds a friend by his/her username.
 */
public class AddFriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        final SharedPreferences userData = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Button bAdd = (Button) findViewById(R.id.AddFriendButton);
        bAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                EditText friendNameView = (EditText) findViewById(R.id.AddFriendText);
                final String friendName = friendNameView.getText().toString();



                class AddFriendTask extends AsyncTask<Void, Void, Void> {

                    /**
                     * Handles async communication with the server to add a Friend.
                     * @param params
                     * @return
                     */

                    @Override
                    protected Void doInBackground(Void... params) {



                        try{

                            OkHttpClient client = new OkHttpClient();

                            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                            Request request1 = new Request.Builder()
                                    .url(MapsActivity.SERVER + "/users")
                                    .get()
                                    .addHeader("authorization", "Basic " + MapsActivity.getAuthenticationEncoded())
                                    .build();

                            Response response1 = client.newCall(request1).execute();

                            String answer1 = response1.body().string();

                            try {
                                JSONArray JArray = new JSONArray(answer1);
                                int length = JArray.length();
                                String[] strArr = new String[length];
                                for (int i = 0; i < length; i++) {
                                    try {
                                        strArr[i] = JArray.getString(i);
                                        System.out.println("si");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                for(int i = 0; i < length; i++){
                                    if(strArr[i].equals(friendName)){
                                        AddFriendForm friend = new AddFriendForm(friendName);
                                        Log.d("FriendName", friendName);
                                        Log.d("Array", friend.getAddZero());
                                        String username = MapsActivity.getUsername();
                                        Log.d("User", username);
                                        RequestBody body = RequestBody.create(JSON, new Gson().toJson(friend));

                                        Log.d("authentication", "Basic " + MapsActivity.getAuthentication());





                                        Request request = new Request.Builder()
                                                .url(MapsActivity.SERVER + "/users/" + username + "/friends")
                                                .post(body)
                                                .addHeader("authorization", "Basic " + MapsActivity.getAuthenticationEncoded())
                                                .addHeader("Content-Type", "application/json")
                                                .build();
                                        Response response = client.newCall(request).execute();
                                        String answer = response.body().string();
                                        Log.d("Antwort", answer);
                                        if(!answer.equals("")){
                                            Snackbar.make(findViewById(R.id.AddFriendText), "Hinzufügen fehlgeschlagen", Snackbar.LENGTH_LONG).setAction("",null).show(); //Hier später Layout
                                            Log.d("ifCondition","HalliHallo");
                                        }
                                        finish();
                                    }

                                    Snackbar.make(findViewById(R.id.AddFriendText), "Benutzer nicht vorhanden", Snackbar.LENGTH_LONG).setAction("",null).show(); //Hier später Layout

                                }
                            }
                            catch(Exception e){}





                        }
                        catch(IOException e){
                            return null;
                        }
                        return null;
                    }

                }

                try {
                    new AddFriendTask().execute();


                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        });


        
    }


}
