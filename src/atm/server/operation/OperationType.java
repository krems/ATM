package atm.server.operation;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 2/19/13
 * Time: 5:26 PM
 * To change this template use File | Settings | File Templates.
 */
public enum OperationType {

    TRANSFER_FROM("TF"), TRANSFER_TO("TT"), INCREASE("I"), WITHDRAW("R"), GETVALUE("GV");

    private OperationType(String operation) {
      this.operation = operation;
    }

    private String operation;
}
