package atm.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/7/13
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class StorageService {
    private final Map<String, Account> accountHashMap = new HashMap<>();
    private final ConcurrentHashMap<Long, Session> sessionHashMap = new ConcurrentHashMap<>();

    public Session createSessionById(long sessionId, String userId, String sourceId, byte[] credentials) {
        Session session = new Session(getOrCreateAccount(userId), sessionId, sourceId, credentials);
        sessionHashMap.put(sessionId, session);
        return session;
    }

    public Session lookupSession(long sessionId) {
        return sessionHashMap.get(sessionId);
    }

    public Session lookupSessionProxyForAccount(String accountId) {
        return new Session(getOrCreateAccount(accountId), -1, null, null);
    }

    public void cleanUpSession(long sessionId) {
        sessionHashMap.remove(sessionId);
    }

    private Account getOrCreateAccount(String accountId) {
        synchronized(accountHashMap) {
            Account res = accountHashMap.get(accountId);
            if (res == null) {
                res = new Account(accountId);
                accountHashMap.put(accountId, res);
            }
            return res;
        }
    }
}
