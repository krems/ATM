package atm.client;

import atm.protocol.*;
import atm.protocol.messages.AccountOperationMessage;
import atm.protocol.messages.LogonMessage;
import atm.protocol.messages.ProtocolMessage;
import atm.protocol.messages.ProtocolMessageType;

import java.security.MessageDigest;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/6/13
 * Time: 3:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientTransport implements MessageListener, SessionListener {
    private static final byte[] EMPTY_CREDS = new byte[0];
    private final ClientConnection protocol;
    private final BlockingQueue<ProtocolMessage> responseQueue = new ArrayBlockingQueue<>(1);

    public ClientTransport(ClientConnection connection) {
        protocol = connection;
        protocol.setMessageListener(this);
        protocol.setSessionListener(this);
    }

    public boolean isConnected() {
        return false;
    }

    public void connect() {}

    long sendLogin(String userId, byte[] credentials) {
        protocol.connect();
        LogonMessage message = new LogonMessage();
        message.messageType = ProtocolMessageType.LOGIN;
        message.userId = userId;
        message.credentials = encodeCredentials(credentials);
        protocol.sendMessage(message);
        return processLogonBack();
    }

    void sendLogout(String userId, long sessionId) {
        LogonMessage message = new LogonMessage();
        message.messageType = ProtocolMessageType.LOGOUT;
        message.sessionId = sessionId;
        message.userId = userId;
        protocol.sendMessage(message);
        protocol.disconnect();
    }

    protected long processLogonBack() {
        long res = -1;
        ProtocolMessage message = getMessageBack();
        if (message != null && message.messageType.equals(ProtocolMessageType.LOGIN)) {
            res = ((LogonMessage) message).sessionId;
        }
        return res;
    }

    protected double processGetValueBack() {
        double res = Double.NaN;
        ProtocolMessage message = getMessageBack();
        if (message != null && message.messageType.equals(ProtocolMessageType.GETVALUE)) {
            res = ((AccountOperationMessage) message).amount;
        }
        return res;
    }

    protected void processAck() {
        ProtocolMessage message = getMessageBack();
    }

    private ProtocolMessage getMessageBack() {
        try {
            return responseQueue.take();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public void withdraw(long sessionId, double amount) {
        AccountOperationMessage message = new AccountOperationMessage();
        message.messageType = ProtocolMessageType.WITHDRAW;
        message.sessionId = sessionId;
        message.amount = amount;
        protocol.sendMessage(message);
        processAck();
    }

    public void increase(long sessionId, double amount) {
        AccountOperationMessage message = new AccountOperationMessage();
        message.messageType = ProtocolMessageType.INCREASE;
        message.sessionId = sessionId;
        message.amount = amount;
        protocol.sendMessage(message);
        processAck();
    }

    public void transferTo(long sessionId, double amount, String accountId) {
        AccountOperationMessage message = new AccountOperationMessage();
        message.messageType = ProtocolMessageType.TRANSFER_TO;
        message.sessionId = sessionId;
        message.amount = amount;
        message.toAccountId = accountId;
        protocol.sendMessage(message);
        processAck();
    }

    public double getAccountValue(long sessionId) {
        AccountOperationMessage message = new AccountOperationMessage();
        message.messageType = ProtocolMessageType.GETVALUE;
        message.sessionId = sessionId;
        protocol.sendMessage(message);
        return processGetValueBack();
    }

    public void onMessage(ProtocolMessage message) {
        responseQueue.add(message);
    }

    public void onConnect(String sessionId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onDisconnect(String sessionId, String reason) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private byte[] encodeCredentials(byte[] password) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return EMPTY_CREDS;
    }
}
