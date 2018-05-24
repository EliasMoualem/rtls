package sim.unihannover.java2017.rtls4;

/**
 * Should be transferred into JSON-Object and sent as body when logging in.
 */

public class LoginForm {

    String firebase = "";

    LoginForm(String fb){
        firebase = fb;
    }

    public void setFirebase(String firebase) {
        this.firebase = firebase;
    }


}
