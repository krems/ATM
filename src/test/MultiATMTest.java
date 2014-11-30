package test;
import atm.client.ATM;
import atm.server.ProcessingService;
import atm.protocol.ClientConnection;
import atm.protocol.impls.ServerConnectionProxy;
import atm.protocol.impls.InProcessConnectionImpl;
import atm.protocol.impls.ServerConnectionStub;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: shesdmi
 * Date: 4/12/13
 * Time: 9:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class MultiATMTest {
    public static final int QUEUE_NUM = 24;
    public static final int QUEUE_DEPTH = 10000;
    public static final double ACCOUNT_VALUE = 10000;

    private Executor executor = Executors.newFixedThreadPool(QUEUE_NUM);
    private ATM[] atms = new ATM[QUEUE_NUM];
    private CyclicBarrier startBarrier = new CyclicBarrier(QUEUE_NUM);
    private CountDownLatch finishLatch = new CountDownLatch(QUEUE_NUM);

    @Before
    public void init() {
        ServerConnectionStub stub = new ServerConnectionStub();
        ProcessingService service = new ProcessingService(stub);
        for (int i = 0; i < QUEUE_NUM; i++) {
            atms[i] = createATM(stub);
        }
    }

    private ATM createATM(ServerConnectionStub stub) {
        ServerConnectionProxy proxy = new ServerConnectionProxy(stub);
        ClientConnection clientConnection = new InProcessConnectionImpl(proxy);
        return new ATM(clientConnection);
    }

    @Test
    public void testMultiAtms() {
        System.out.println("Test started...");

        long startTime = System.nanoTime();

        for(int i = 0 ; i < QUEUE_NUM; i++) {
            executor.execute(new ActorQueue(finishLatch, startBarrier, atms[i], i, QUEUE_NUM, QUEUE_DEPTH));
        }

        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(int i = 0 ; i < QUEUE_NUM; i++) {
            for (int j = 0; j < QUEUE_DEPTH; j++) {
                String id = "test" +i+"-"+j;
                ATM atm = atms[0];
                atm.login(id, id.getBytes());
                double value = atm.getAccountValue();

                assertEquals("Test failed for account " + id, ACCOUNT_VALUE, value, 0.00000001);

                //System.out.println("Test passed for " + id);

                atm.logout();

            }
        }

        System.out.printf("Elapsed %.3f secs", (System.nanoTime() - startTime)/1000000000.0);

    }
}
