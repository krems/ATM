package atm.client;


import atm.protocol.ClientConnection;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 2/20/13
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ATM {
    private final ClientTransport clientTransport;
    private long sessionId;
    private String currentUser;

    public ATM(ClientConnection connection) {
        clientTransport = new ClientTransport(connection);
    }

    public void login(String userId, byte[] credentials) {
        currentUser = userId;
        sessionId = clientTransport.sendLogin(currentUser, credentials);
        if (sessionId < 0) {
            throw new RuntimeException("Can't login");
        }
    }

    public void logout() {
        clientTransport.sendLogout(currentUser, sessionId);
        currentUser = null;
        sessionId = -1;
    }

    public void withdraw(double amount) {
        clientTransport.withdraw(sessionId, amount);
    }

    public void increase(double amount) {
        clientTransport.increase(sessionId, amount);
    }

    public void transferTo(double amount, String accountId) {
        clientTransport.transferTo(sessionId, amount, accountId);
    }

    public double getAccountValue() {
        return clientTransport.getAccountValue(sessionId);
    }
}

