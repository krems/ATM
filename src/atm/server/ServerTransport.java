package atm.server;

import atm.protocol.messages.AccountOperationMessage;
import atm.protocol.messages.LogonMessage;
import atm.protocol.messages.ProtocolMessage;
import atm.protocol.messages.ProtocolMessageType;
import atm.server.operation.Operation;
import atm.server.operation.OperationType;
import atm.protocol.*;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/6/13
 * Time: 6:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerTransport implements MessageListener {

    public ServerTransport(ProcessingService service, CallbackConnection connection, StorageService storage) {
        this.service = service;
        this.connection = connection;
        this.storage = storage;
        connection.setMessageListener(this);
    }

    public void onMessage(ProtocolMessage message) {
        switch (message.messageType) {
            case LOGIN:
                LogonMessage logon = (LogonMessage) message;
                Credentials credentials = new Credentials();
                credentials.userId = logon.userId;
                credentials.rawcred = logon.credentials;
                long sessionId = service.userLogin(credentials);
                if (sessionId >= 0) {
                    storage.createSessionById(sessionId, logon.userId, logon.sourceId, credentials.rawcred);
                    LogonMessage responseBack = new LogonMessage();
                    responseBack.messageType = ProtocolMessageType.LOGIN;
                    responseBack.sourceId = logon.sourceId;
                    responseBack.userId = logon.userId;
                    responseBack.sessionId = sessionId;
                    connection.sendMessage(responseBack);
                }
                break;
            case LOGOUT:
                LogonMessage logout = (LogonMessage) message;
                if (logout.sessionId >= 0) {
                    storage.cleanUpSession(logout.sessionId);
                }
                break;
            case INCREASE:
            case TRANSFER_TO:
            case GETVALUE:
            case WITHDRAW:
                AccountOperationMessage operationMsg = (AccountOperationMessage) message;
                processOperation(operationMsg);
                break;
        }
    }

    protected void processOperation(AccountOperationMessage msg) {
        OperationType optype;
        Operation operation = null;
        switch (msg.messageType) {
            case INCREASE:
                optype = OperationType.INCREASE;
                operation = new Operation(optype, storage.lookupSession(msg.sessionId), null, msg.amount);
                break;
            case GETVALUE:
                optype = OperationType.GETVALUE;
                operation = new Operation(optype, storage.lookupSession(msg.sessionId), null, msg.amount);
                break;
            case TRANSFER_TO:
                optype = OperationType.TRANSFER_TO;
                operation = new Operation(optype, storage.lookupSession(msg.sessionId), storage.lookupSessionProxyForAccount(msg.toAccountId), msg.amount);
                break;
            case WITHDRAW:
                optype = OperationType.WITHDRAW;
                operation = new Operation(optype, storage.lookupSession(msg.sessionId), null, msg.amount);
                break;
        }
        try {
            service.processOperation(msg.sessionId, operation);
        } catch (Exception ex) {
            publishNak(operation);
        }
    }

    public void publishOperationResult(Operation operation) {
        if (!requiredResponse(operation)) {
            return;
        }
        AccountOperationMessage msg = new AccountOperationMessage();
        if (operation.getOperationType() != OperationType.GETVALUE) {
            msg.messageType = ProtocolMessageType.ACK;
        } else {
            msg.messageType = ProtocolMessageType.GETVALUE;
            msg.amount = operation.getValue();
        }
        msg.sourceId = operation.getSession1().getSourceId();
        connection.sendMessage(msg);
    }

    private void publishNak(Operation operation) {
        AccountOperationMessage msg = new AccountOperationMessage();
        msg.messageType = ProtocolMessageType.NAK;
        msg.sourceId = operation.getSession1().getSourceId();
        connection.sendMessage(msg);
    }

    private boolean requiredResponse(Operation operation) {
        return true;
    }

    private final Connection connection;
    private final ProcessingService service;
    private final StorageService storage;
}
