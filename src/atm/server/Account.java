package atm.server;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 2/19/13
 * Time: 3:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class Account {

    private volatile double balance;
    private String id;

    public Account(String id) {
        this.id = id;
    }

    public boolean transferTo(Account to, double delta) {
        if(balance > delta) {
            to.balance = to.balance + delta;
            this.balance = this.balance - delta;
            Thread.yield();
            return true;
        }
        return false;
    }

    public void increase(double delta) {
        balance = balance + delta;
    }

    public boolean withdraw(double delta) {
        if(balance - delta > 0.00000001) {
            balance = balance - delta;
            return true;
        }
        return false;
    }

    public Account createCopy() {
        Account res = new Account(id);
        res.balance = balance;
        return res;
    }

    public void rollbackToCopy(Account account) {
        if(id.equals(account.id)) {
            balance = account.balance;
        }
    }

    public double getBalance() {
        return balance;
    }

    public String getId() {
        return id;
    }
}
