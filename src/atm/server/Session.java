package atm.server;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 2/19/13
 * Time: 5:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class Session {

    public Session(Account account, String sessionId, String sourceId, byte[] passHash) {
        this.account = account;
        this.sessionId = sessionId;
        this.sourceId = sourceId;
        this.passHash = passHash;
    }

    public Account getAccount() {
        return account;
    }

    public String getSessionId(){
        return sessionId;
    }

    public String getSourceId(){
        return sourceId;
    }

    private Account account;
    private String sessionId;
    private String sourceId;
    private byte[] passHash;
}
