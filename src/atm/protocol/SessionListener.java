package atm.protocol;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/6/13
 * Time: 3:44 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SessionListener {
    void onConnect(String sessionId);
    void onDisconnect(String sessionId, String reason);
}
