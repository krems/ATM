package atm.protocol.impls;

import atm.protocol.ClientConnection;
import atm.protocol.Connection;
import atm.protocol.messages.ProtocolMessage;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/6/13
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerConnectionProxy implements Connection {

    private ServerConnectionStub stub;

    public ServerConnectionProxy(ServerConnectionStub stub) {
        this.stub = stub;
    }

    public void addConnection(ClientConnection connection, String sessionId) {
      stub.addConnection(connection, sessionId);
    }

    public void removeConnection(ClientConnection connection, String sessionId) {
      stub.removeConnection(connection, sessionId);
    }

    public void sendMessage(ProtocolMessage message) {
        stub.getMessageListener().onMessage(message);
    }
}
