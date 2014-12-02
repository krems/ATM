package atm.server;

import atm.server.operation.Operation;
import atm.server.operation.ResultCallback;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 2/19/13
 * Time: 5:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionController {
    private TransactionController() {}

    public static TransactionController getController() {
        return TransactionControllerHolder.instance;
    }

    public Transaction createTransaction(Operation operation, ResultCallback resultCallback) {
        return new Transaction(operation, resultCallback);
    }

    private static class TransactionControllerHolder {
        private static final TransactionController instance = new TransactionController();
    }

}
