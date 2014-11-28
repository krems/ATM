package atm.server;

import atm.server.operation.Operation;
import atm.server.operation.ResultCallback;
import atm.protocol.CallbackConnection;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 2/19/13
 * Time: 6:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessingService implements ResultCallback {

    public static final int EXEC_TRIES = 3;
    public static final int COMMIT_TRIES = 3;

    public static final int EXEC_POOL_SIZE = 16;

    private AtomicInteger sessionIdGen= new AtomicInteger(0);
    private ServerTransport transport;

    private Executor executor = Executors.newFixedThreadPool(EXEC_POOL_SIZE);



    public ProcessingService(CallbackConnection connection){

        transport = new ServerTransport(this, connection, new StorageService());

    }

    public String userLogin(Credentials username) {
       if (validateCredentials(username)) {
           return issueNewSessionId();
       }
       return null;
    }

    public void userLogout(String sessionId) {

    }

    public void processOperation(String sessionId, Operation operation) throws InvalidSessionException {
        validateSession(sessionId);
        final Transaction transaction = transactionController.createTransaction(operation, this);

        executor.execute(() -> {
            try {
                processTransaction(transaction);
            } catch (TransactionException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void processTransaction(Transaction transaction) throws TransactionException {
        for (int i = 0; i < EXEC_TRIES; i++) {
            try {
                transaction.execute();
                break;
            } catch (TransactionException ex) {
                if (ex.isTemporary && i != EXEC_TRIES - 1) {
                    continue;
                } else {
                    throw ex;
                }
            }
        }
    }


    private synchronized void validateSession(String sessionId) throws InvalidSessionException{
        double res = 0;
        for(int i = 0; i < 100; i++) {
            res = res + res*i;
            Thread.yield();
        }
        if(res < 0 ) {
            throw new InvalidSessionException();
        }
    }

    private boolean validateCredentials(Credentials credentials) {
        return true;
    }


    private String issueNewSessionId() {
        return "Session " + sessionIdGen.incrementAndGet();
    }


    public  void onOperationResult(Operation operation) {
        transport.publishOperationResult(operation);
    }

    private TransactionController transactionController = TransactionController.getController();
}
