package atm.protocol.impls;

import atm.protocol.ClientConnection;
import atm.protocol.MessageListener;
import atm.protocol.messages.ProtocolMessage;
import atm.protocol.SessionListener;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/6/13
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class InProcessConnectionImpl implements ClientConnection {

    private MessageListener msgListener;
    private SessionListener sessionListener;
    private ServerConnectionProxy serverConnection;
    private String connectionId;

    public  InProcessConnectionImpl (ServerConnectionProxy proxy) {
        serverConnection = proxy;
        connectionId = getConnectionId();
    }

    private String getConnectionId() {
        return toString();
    }

    public void connect() {
        serverConnection.addConnection(this, connectionId);
    }

    public void disconnect() {
        serverConnection.removeConnection(this, connectionId);
    }

    public void sendMessage(ProtocolMessage message) {
        message.sourceId = connectionId;
        serverConnection.sendMessage(message);
    }

    public void setMessageListener(MessageListener listener) {
        msgListener = listener;
    }

    public void setSessionListener(SessionListener listener) {
        sessionListener = listener;
    }

    public MessageListener getMessageListener() {
        return msgListener;
    }

    public SessionListener getSessionListener() {
        return sessionListener;
    }
}
