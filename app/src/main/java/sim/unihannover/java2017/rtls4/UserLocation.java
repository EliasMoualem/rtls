package sim.unihannover.java2017.rtls4;

import com.google.android.gms.maps.model.LatLng;

import java.util.Map;

/**
 * Stores information about the user and his/her location.
 */

public class UserLocation {

    String username;
    double lat;
    double lon;
    int acc;
    long time;
    String custom_string;

    public UserLocation(String username, double lat, double lon, int acc, long time, String custom_string) {
        this.username = username;
        this.lat = lat;
        this.lon = lon;
        this.acc = acc;
        this.time = time;
        this.custom_string = custom_string;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getAcc() {
        return acc;
    }

    public void setAcc(int acc) {
        this.acc = acc;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getCustom_string() {
        return custom_string;
    }

    public void setCustom_string(String custom_string) {
        this.custom_string = custom_string;
    }

    public LatLng getLatLng(){return new LatLng(this.lat, this.lon);}
}
