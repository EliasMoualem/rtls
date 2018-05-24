package sim.unihannover.java2017.rtls4;

/**
 * Should be transferred into JSON-Object and sent as body when a friend gets removed.
 */

public class RemoveFriendForm {

    String[] remove;

    RemoveFriendForm(String friendToRemove){
        remove = new String[1];
        remove[0] = friendToRemove;
    }

    public String[] getRemove() {
        return remove;
    }

    public String getRemoveZero() {
        return remove[0];
    }

    public void setRemove(String[] remove) {
        this.remove = remove;
    }

}
