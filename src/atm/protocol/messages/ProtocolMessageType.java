package atm.protocol.messages;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/6/13
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ProtocolMessageType {
    LOGIN(0),
    LOGOUT(1),
    TRANSFER_TO(2),
    WITHDRAW(3),
    INCREASE(4),
    GETVALUE(5),
    ACK(6),
    NAK(7);

    private ProtocolMessageType(int msgType) {
        this.msgType = msgType;
    }

    public int getType() {
        return msgType;
    }

    private final int msgType;
}
