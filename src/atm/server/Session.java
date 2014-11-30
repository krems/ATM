package atm.server;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 2/19/13
 * Time: 5:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class Session {

    public Session(Account account, long sessionId, long sourceId, byte[] passHash) {
        this.account = account;
        this.sessionId = sessionId;
        this.sourceId = sourceId;
        this.passHash = passHash;
    }

    public Account getAccount() {
        return account;
    }

    public long getSessionId(){
        return sessionId;
    }

    public long getSourceId(){
        return sourceId;
    }

    private final Account account;
    private final long sessionId;
    private final long sourceId;
    private final byte[] passHash;
}
