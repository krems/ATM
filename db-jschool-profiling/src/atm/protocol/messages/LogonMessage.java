package atm.protocol.messages;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 3/6/13
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogonMessage extends  ProtocolMessage {

    public String userId;
    public byte[] credentials;
    public String sessionId;

}