package test;

import atm.client.ATM;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 4/12/13
 * Time: 8:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ActorQueue implements Runnable{

    private CountDownLatch finishLatch;
    private CyclicBarrier startBarrier;
    private ATM atm;
    private final int queueId;
    private final int queueNum;
    private final int queueDepth;

    public ActorQueue(CountDownLatch finishLatch, CyclicBarrier startBarrier, ATM atm, int queueId, int queueNum, int queueDepth) {
        this.startBarrier = startBarrier;
        this.finishLatch = finishLatch;
        this.atm = atm;
        this.queueId = queueId;
        this.queueNum = queueNum;
        this.queueDepth = queueDepth;
    }

    public void run() {

        try {
            startBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            finishLatch.countDown();
            return;
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
            finishLatch.countDown();
            return;
        }

        for(int i = 0 ; i < queueDepth; i++) {
            String id = queueId+"-"+i;
            String account1 = queueId > 0 ? "test" + (queueId - 1) +"-"+i : null;
            String account2 = queueId < queueNum -1 ? "test" + (queueId + 1) +"-"+i : null;

            atm.login(id, id.getBytes());
            atm.increase(10000);

            if( account1 != null ) {
                atm.transferTo(200, account1);
            }

            if( account2 != null ) {
                atm.transferTo(200, account2);
            }
            atm.logout();
        }
        finishLatch.countDown();
    }
}
