package sim.unihannover.java2017.rtls4;

/**
 * Should be transferred into JSON-Object and sent as body when making Add-Friend Requests.
 */

public class AddFriendForm {
    String[] add;

    AddFriendForm(String friendToAdd){
        add = new String[1];
        add[0] = friendToAdd;
    }

    public String[] getAdd() {
        return add;
    }

    public String getAddZero() {
        return add[0];
    }

    public void setAdd(String[] add) {
        this.add = add;
    }
}
