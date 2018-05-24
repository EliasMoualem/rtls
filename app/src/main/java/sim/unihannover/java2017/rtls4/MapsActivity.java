package sim.unihannover.java2017.rtls4;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.prefs.Preferences;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static sim.unihannover.java2017.rtls4.R.id.fabLocation;
import static sim.unihannover.java2017.rtls4.R.id.map;

/**
 * Main Activity. Stores user information, status of the user and the different UserLocations
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    /**
     * The Server URL.
     */
    public static final String SERVER = "http://35.187.120.46/temp";
    /**
     * The Map Fragment.
     */
    private static GoogleMap mMap;
    /**
     * True when the user is logged in.
     */
    static boolean loggedIn = false;
    /**
     * The username:secret authentiation.
     */
    public static String authentication = "";
    /**
     * The username.
     */
    public static String username = "";
    /**
     *
     */
    private FusedLocationProviderClient mFusedLocationClient;
    /**
     * Stores the UserLocations.
     */
    static List<UserLocation> userLocations;
    /**
     * Used for placing Markers.
     */
    static Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        userLocations = new LinkedList<UserLocation>();


        // create the fusedLocationClient used to retrieve the location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // open the login activity in case the user is not logged in, but only once.
        final Intent startLogin = new Intent(this, LoginActivity.class);

        SharedPreferences userData = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String username = userData.getString("username", null);
        String secret = userData.getString("secret", null);

        if (username == null || secret == null) {
            if (username == null) Log.d("Username: ", "NULL");
            if (secret == null) Log.d("secret: ", "NULL");
            // there is no current login, so show the login
            startActivity(startLogin);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
            loggedIn = false;
        } else {
            authentication = username + ":" + secret;
            MapsActivity.username = username;
            loggedIn = true;
        }
        // check for valid credentials here (looks like we aren't checking for valid credentials. Instead, whenever an action fails, we should look whether this is caused by invalid credentials.
        if (1 == 0) {
            // the credentials are false, so remove the userData and open login
            userData.edit().clear().commit();
            startActivity(startLogin);
            loggedIn = false;
        }
    }

    @Override
    protected void onStart() {

        //später: login überprüfen,
        super.onStart();

        final Intent startLogin = new Intent(this, LoginActivity.class);
        final FloatingActionButton fabLoc = (FloatingActionButton) findViewById(R.id.fabLocation);
        fabLoc.setOnClickListener(new View.OnClickListener() {

            class SendLocationTask extends AsyncTask<Void, Void, Void> {

                private final Location location;

                SendLocationTask(Location location1) {
                    location = location1;
                }

                @Override
                protected Void doInBackground(Void... params) {

                    OkHttpClient client = new OkHttpClient();
                    final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                    RequestBody body = RequestBody.create(JSON, String.valueOf(new JSONObject(new HashMap<String, String>() {{
                        put("lat", Double.toString(location.getLatitude()));
                        put("lon", Double.toString(location.getLongitude()));
                        put("acc", "0");
                        put("time", Long.toString(System.currentTimeMillis()));
                    }})));
                    try {
                        Request request = new Request.Builder()
                                .url(SERVER + "/users/" + getUsername() + "/location")
                                .addHeader("authorization", "Basic " + getAuthenticationEncoded())
                                .addHeader("Content-Type", "application/json")
                                .put(body)
                                .build();
                        client.newCall(request).execute();
                        Log.d("Location", "Location shared");
                        Snackbar.make(findViewById(R.id.map), "Standort gesendet", Snackbar.LENGTH_SHORT).setAction("",null).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            }

            public void onClick(View v) {
                if(!loggedIn){
                    startActivity(startLogin);
                    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                } else {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                                requestPermissions(perms, 0);
                            };
                            Log.d("NOTICE", "Missing permissions for location services.");
                            Snackbar.make(findViewById(R.id.map), "Aktiviere die Ortungsdienste, um deinen Standort senden zu können.", Snackbar.LENGTH_LONG).setAction("",null).show();
                            return;
                        }

                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(new OnSuccessListener<Location>() {
                                            @Override
                                            public void onSuccess(final Location location) {
                                                // Got last known location. In some rare situations this can be null.
                                                if (location != null) {
                                                    Log.d("Location","ist nicht null");
                                                    new SendLocationTask(location).execute();
                                                }
                                                if(location == null){
                                                    Log.d("Location","ist null");
                                                }
                                            }
                                        }
                                );

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        });

        FloatingActionButton fabFriends = (FloatingActionButton) findViewById(R.id.fabFriends);
        fabFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!loggedIn) {
                    startActivity(startLogin);
                    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                } else {
                    Intent frndLst = new Intent(MapsActivity.this, FriendlistActivity.class);
                    startActivity(frndLst);
                    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                }

            }
        });

        class GetFriends extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... params) {

                String username = MapsActivity.getUsername();

                try {
                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .url(SERVER + "/users/" + username + "/friends")
                            .get()
                            .addHeader("authorization", "Basic " + MapsActivity.getAuthenticationEncoded())
                            .build();

                    Response response = client.newCall(request).execute();
                    String answer = response.body().string();

                    JSONArray friends = new JSONArray(answer);

                    Log.d("Antwort1" , answer);

                    if(answer != null){
                        MapsActivity.getLocationForFriends(friends);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        new GetFriends().execute();

    }

    public static void getLocationForFriends(JSONArray friends) {
        class GetFriendLocation extends AsyncTask<Void, Void, Void> {

            private String friendUserName;

            GetFriendLocation(String friendUserName1) {
                friendUserName = friendUserName1;
            }

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .url(SERVER + "/users/" + friendUserName + "/location")
                            .get()
                            .addHeader("authorization", "Basic " + MapsActivity.getAuthenticationEncoded())
                            .build();

                    Response response = client.newCall(request).execute();

                    String answer = response.body().string();

                    Log.d("NOTICE", "Found response location data: " + answer);

                    if (answer.contains("username")) {
                        JSONObject friendLocation = new JSONObject(answer);

                        HashMap<String, String> friendLocationData = new HashMap<String, String>();
                        Iterator<?> keys = friendLocation.keys();

                        while( keys.hasNext() ){
                            String key = (String)keys.next();
                            String value = friendLocation.getString(key);
                            friendLocationData.put(key, value);
                        }

                        Log.d("NOTICE", "Location found for user: " + friendLocationData.get("username"));

                        putUserLocation(getUserFromRemoteMessage(friendLocationData));

                        updateUserPositions();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        for (int i = 0; i < friends.length(); i++) {
            try {
                new GetFriendLocation(friends.getString(i)).execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng uni = new LatLng(52.3864096, 9.7097395);
        mMap.addMarker(new MarkerOptions().position(uni).title("Hier ist die Uni"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(uni));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMyLocationEnabled(true);
        updateUserPositions();
        mMap.moveCamera(CameraUpdateFactory.zoomTo(12.0f));
        //CameraUpdateFactory.newLatLng(uni);

    }

    /**
     * Called with the remote message containing an update for a users' location
     * @param remoteMessage The incoming remote message from the firebase service
     */
    public static void didReceiveRemoteMessage(RemoteMessage remoteMessage) {
        Log.d("MAPS", "message received: " + remoteMessage.getData().toString());
        putUserLocation(getUserFromRemoteMessage(remoteMessage.getData()));
        updateUserPositions();
        return;
    }

    /**
     * Get the current logged in username
     * @return The username
     */
    public static String getUsername() {
        return MapsActivity.username;
    }

    /**
     * Get the current authentication (username:secret)
     * @return The authentication
     */
    public static String getAuthentication() {
        return MapsActivity.authentication;
    }

    /**
     * Get the current authentication in encoded format (username:secret base64 encoded)
     * @return The encoded authentication
     */
    public static String getAuthenticationEncoded() {
        return new String(android.util.Base64.encode(MapsActivity.authentication.getBytes(), Base64.NO_WRAP));
    }

    /**
     * Called with the username and secret of a user on successful login
     * @param username The username of the now logged in user
     * @param secret The secret of the now logged in user
     */
    public static void loggedInWithUsernameAndSecret(String username, String secret) {
        MapsActivity.authentication = username + ":" + secret;
        MapsActivity.username = username;
        loggedIn = true;
    }

    /**
     * Turns a Firebase-Message into a UserLocation Object.
     * @param user The userdata as a Map. Contains username, location, acccuracy, time and a custom Message.
     * @return UserLocation
     */
    public static UserLocation getUserFromRemoteMessage(Map<String,String> user){

        return new UserLocation(
            user.get("username"),
            Double.parseDouble(user.get("lat")),
            Double.parseDouble(user.get("lon")),
            Integer.parseInt(user.get("acc")),
            Long.parseLong(user.get("time")),
            user.get("custom_string")
        );

    }

    /**
     * Puts a UserLocation Object in the userLocation List in MapsActivity. If there is already information saved for this particular username, the information will be updated.
     * @param user New (information about) user.
     */
    public static void putUserLocation(UserLocation user) {
        Log.d("NOTICE", "Inside putUserLocation");
        if (userLocations.size() < 1){
            MapsActivity.userLocations.add(user);
            return;
        }
        Iterator<UserLocation> iter = userLocations.iterator();

        Log.d("NOTICE", "Now starting to loop through the map");

        while (iter.hasNext()){
            UserLocation u = iter.next();
            Log.d("NOTICE", "  Current iterator username: " +u.getUsername());
            if(u.getUsername().equals(user.getUsername())){
                Log.d("NOTICE", " Iterator " + u.getUsername() + " equals to " + user.getUsername());
                iter.remove();
            } else {
                Log.d("NOTICE", " Iterator " + u.getUsername() + " equals not to " + user.getUsername());
            }
        }
        Log.d("NOTICE", "Looping through the map finished");
        MapsActivity.userLocations.add(user);
        Log.d("NOTICE", "Userlocations now: " +MapsActivity.userLocations.toString());
    }

    static Runnable updateMarkers = new Runnable() {
        @Override
        public void run() {
            Log.d("NOTICE","Inside runnable with content: " + MapsActivity.userLocations.toString());
            MapsActivity.mMap.clear();
            for(UserLocation u : MapsActivity.userLocations) {
                LatLng userPos = u.getLatLng();
                Log.d("NOTICE","Pin with data set: Username: " + u.getUsername() + ", position: " + userPos.toString());
                mMap.addMarker(new MarkerOptions().position(userPos).title(u.getUsername()).snippet(new Date(u.getTime()).toString())).showInfoWindow();
            }
            //handler.postDelayed(this, 2000);
        }
    };

    /**
     * Places new Markers for updated UserLocations.
     */

    public  static void updateUserPositions() {
        Log.d("XXXXupdateUserPositions","userLocation ist  nicht null");
        if(userLocations == null){
            Log.d("updateUserPositions","userLocation ist null");
            return;
        }
        handler.postDelayed(updateMarkers, 2000);


    }



}
