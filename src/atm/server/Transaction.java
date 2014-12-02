package atm.server;


import atm.server.operation.Operation;
import atm.server.operation.ResultCallback;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 2/19/13
 * Time: 5:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class Transaction {
    private static final Object globalLock = new Object();
    private final Operation operation;
    private Account a1 = null,a2=null;
    private double value;
    private final ResultCallback resultCallback;

    public Transaction(Operation operation, ResultCallback resultCallback) {
        this.operation = operation;
        this.resultCallback = resultCallback;
    }

    public void execute() throws TransactionException {
        a1 = operation.getSession1().getAccount();
        if (operation.getSession2() != null) {
            a2 = operation.getSession2().getAccount();
        }
        if (a2 == null) {
            synchronized (a1) {
                nonSafeExec();
            }
        } else {
            int a1HashCode = System.identityHashCode(a1);
            int a2HashCode = System.identityHashCode(a2);
            if (a1HashCode > a2HashCode) {
                synchronized (a1) {
                    Thread.yield();
                    synchronized (a2) {
                        nonSafeExec();
                    }
                }
            } else if (a1HashCode < a2HashCode) {
                synchronized (a2) {
                    Thread.yield();
                    synchronized (a1) {
                        nonSafeExec();
                    }
                }
            } else {
                synchronized (globalLock) {
                    synchronized (a1) {
                        Thread.yield();
                        synchronized (a2) {
                            nonSafeExec();
                        }
                    }
                }
            }
        }
        resultCallback.onOperationResult(operation);
    }

    private void nonSafeExec() throws TransactionException {
        switch (operation.getOperationType()) {
            case INCREASE:
                a1.increase(operation.getValue());
                break;
            case TRANSFER_FROM:
                break;
            case TRANSFER_TO:
                if (!a1.transferTo(a2, operation.getValue())) {
                    throw new TransactionException("Can't perform operation for " + a1.getId());
                }
                break;
            case WITHDRAW:
                if (!a1.withdraw(operation.getValue())) {
                    throw new TransactionException("");
                }
                break;
            case GETVALUE:
                value = a1.getBalance();
                break;
        }
        operation.setValue(value);
    }
}
