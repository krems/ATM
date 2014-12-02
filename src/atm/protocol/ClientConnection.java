package atm.protocol;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/6/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ClientConnection extends CallbackConnection {
    void connect();
    void disconnect();
    void setSessionListener(SessionListener listener);
    SessionListener getSessionListener();
}
