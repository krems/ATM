package atm.server.operation;

import atm.server.Session;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 2/19/13
 * Time: 6:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class Operation {
    public Operation(OperationType optype, Session session1, Session session2, double value) {
        operationType = optype;
        this.session1 = session1;
        this.session2 = session2;
        this.setValue(value);
    }


    public OperationType getOperationType() {
        return operationType;
    }

    public Session getSession1() {
        return session1;
    }

    public Session getSession2() {
        return session2;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    protected final OperationType operationType;
    protected final Session session1;
    protected final Session session2;
    protected double value;
}
