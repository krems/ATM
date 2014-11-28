package atm.protocol.impls;

import atm.protocol.CallbackConnection;
import atm.protocol.ClientConnection;
import atm.protocol.MessageListener;
import atm.protocol.messages.ProtocolMessage;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/6/13
 * Time: 7:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerConnectionStub implements CallbackConnection {
    private ConcurrentHashMap<String, ClientConnection> connectionMap = new ConcurrentHashMap<String, ClientConnection>(24, 0.75f, 2);
    private MessageListener listener;

    public  void sendMessage(ProtocolMessage message) {
        connectionMap.get(message.sourceId).getMessageListener().onMessage(message);
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    public MessageListener getMessageListener() {
        return listener;
    }

    public void addConnection(ClientConnection connection,String sessionId) {
        connectionMap.put(sessionId, connection);
    }

    public void removeConnection(ClientConnection connection, String sessionId) {
        connectionMap.remove(sessionId);
    }
}
