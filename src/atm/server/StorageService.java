package atm.server;

import atm.util.LongObjectHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/7/13
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class StorageService {
    private final Map<String, Account> accountHashMap = new HashMap<>(240000);
    private final Map<Long, Session> sessionHashMap = new HashMap<>();

    public Session createSessionById(long sessionId, String userId, long sourceId, byte[] credentials) {
        Session session = new Session(getOrCreateAccount(userId), sessionId, sourceId, credentials);
        synchronized (sessionHashMap) {
            sessionHashMap.put(sessionId, session);
        }
        return session;
    }

    public Session lookupSession(long sessionId) {
        synchronized (sessionHashMap) {
            return sessionHashMap.get(sessionId);
        }
    }

    public Session lookupSessionProxyForAccount(String accountId) {
        return new Session(getOrCreateAccount(accountId), -1, -1, null);
    }

    public void cleanUpSession(long sessionId) {
        synchronized (sessionHashMap) {
            sessionHashMap.remove(sessionId);
        }
    }

    private Account getOrCreateAccount(String accountId) {
        synchronized (accountHashMap) {
            Account res = accountHashMap.get(accountId);
            if (res == null) {
                res = new Account(accountId);
                accountHashMap.put(accountId, res);
            }
            return res;
        }
    }
}
