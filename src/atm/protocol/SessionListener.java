package atm.protocol;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/6/13
 * Time: 3:44 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SessionListener {

    public void onConnect(String sessionId);
    public void onDisconnect(String sessionId, String reason);
}
