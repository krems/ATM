package atm.server;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 2/19/13
 * Time: 7:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransactionException extends Exception {
    public TransactionException(String message) {
        super(message);
    }
    public boolean isTemporary;
}
