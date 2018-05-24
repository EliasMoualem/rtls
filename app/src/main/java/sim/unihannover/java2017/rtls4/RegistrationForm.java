package sim.unihannover.java2017.rtls4;

/**
 * Should be transferred into JSON-Object and sent as body while the registration.
 */

public class RegistrationForm {

    String username = "";
    String password = "";

    RegistrationForm(String user, String pass){
        username = user;
        password = pass;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
