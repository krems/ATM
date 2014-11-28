package atm.server;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/7/13
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class StorageService {

    private HashMap<String, Account> accountHashMap = new HashMap<String, Account>();
    private ConcurrentHashMap<String, Session> sessionHashMap = new ConcurrentHashMap<String, Session>();

    public  Session createSessionById(String sessionId, String userId, String sourceId, byte[] credentials) {

        Session session = new Session(getOrCreateAccount(userId), sessionId, sourceId, credentials);

        sessionHashMap.put(sessionId, session);
        return session;
    }

    private synchronized Account getOrCreateAccount(String accountId) {
        Account res = null;

            res = accountHashMap.get(accountId);

            if(res == null) {
              res = new Account(accountId);
              accountHashMap.put(accountId, res);
            }


        return res;

    }
    public  Session lookupSession(String sessionId) {
        return sessionHashMap.get(sessionId);
    }

    public  Session lookupSessionProxyForAccount(String accountId) {
        return new Session(getOrCreateAccount(accountId), null, null, null);

    }

}
