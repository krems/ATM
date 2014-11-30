package atm.protocol.messages;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/6/13
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class AccountOperationMessage extends ProtocolMessage {
    public long sessionId;
    public double amount;
    public String toAccountId;
}