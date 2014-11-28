package atm.protocol;

import atm.protocol.messages.ProtocolMessage;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/6/13
 * Time: 3:42 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MessageListener {
    public void onMessage(ProtocolMessage message);
}
