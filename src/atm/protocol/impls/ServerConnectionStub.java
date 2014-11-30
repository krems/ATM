package atm.protocol.impls;

import atm.protocol.CallbackConnection;
import atm.protocol.ClientConnection;
import atm.protocol.MessageListener;
import atm.protocol.messages.ProtocolMessage;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/6/13
 * Time: 7:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerConnectionStub implements CallbackConnection {
    private final HashMap<Long, ClientConnection> connectionMap = new HashMap<>(24);
    private MessageListener listener;

    public  void sendMessage(ProtocolMessage message) {
        synchronized (connectionMap) {
            connectionMap.get(message.sourceId).getMessageListener().onMessage(message);
        }
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    public MessageListener getMessageListener() {
        return listener;
    }

    public void addConnection(ClientConnection connection, long sessionId) {
        synchronized (connectionMap) {
            connectionMap.put(sessionId, connection);
        }
    }

    public void removeConnection(ClientConnection connection, long sessionId) {
        synchronized (connectionMap) {
            connectionMap.remove(sessionId);
        }
    }
}
