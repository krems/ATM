package atm.protocol;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/6/13
 * Time: 7:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CallbackConnection extends Connection {
    void setMessageListener(MessageListener listener);
    MessageListener getMessageListener();
}
