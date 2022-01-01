import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;


public class DiningPhilosophers {
    public Semaphore[] forks;

    public DiningPhilosophers() {
        forks = new Semaphore[5];
        for (int i = 0; i < 5; i++) {
            forks[i] = new Semaphore(1);
        }
    }

    public void think() throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextInt(1000));
    }

    public void eat(int id) throws InterruptedException {
        // let the 1st philosopher be the left-handed
        if (id == 0) {
            // pickup the right fork first
            forks[(id + 1) % 5].acquire();
            // pickup the left fork
            forks[id].acquire();
        } else {
            // pickup the left fork first
            forks[id].acquire();
            // pickup the right fork
            forks[(id + 1) % 5].acquire();
        }
        System.out.printf("Philosopher %d is eating\n", id);
        // release the right fork
        forks[(id + 1) % 5].release();
        // release the left fork
        forks[id].release();
    }

    public void philosopherLife(int id) throws InterruptedException {
        while (true) {
            think();
            eat(id);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        DiningPhilosophers dp = new DiningPhilosophers();

        // we have 5 philosophers
        Thread[] philosopherThreads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            int j = i;
            philosopherThreads[i] = new Thread( new Runnable() {
                public void run() {
                    try {
                        dp.philosopherLife(j);
                    } catch (InterruptedException ie) {
                        // swallow it
                    }
                    
                }
            });
            philosopherThreads[i].start();
        }

        for (int i = 0; i < 5; i++) {
            philosopherThreads[i].join();
        }
    }
}
