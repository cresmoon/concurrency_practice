import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.PriorityQueue;
import java.util.Comparator;

class Job {
    public long executeAt;
    public String execMessage;

    public Job(long execAfter, String execMsg) {
        this.executeAt = System.currentTimeMillis() + execAfter;
        this.execMessage = execMsg;
    }

    public void execute() {
        // only print the message for now
        System.out.println(execMessage);
    }
}

public class DelayedJobScheduler {
    PriorityQueue<Job> pq;
    Lock lock = new ReentrantLock();
    Condition newJobArrived = lock.newCondition();

    public DelayedJobScheduler() {
        pq = new PriorityQueue<>(new Comparator<Job>() {
            public int compare(Job j1, Job j2) {
                return (int) (j1.executeAt - j2.executeAt); // TODO: address the casting issue
            }
        });
    }

    public void registerJob(Job job) {
        lock.lock();
        pq.add(job);
        newJobArrived.signal();
        lock.unlock();
    }

    public void executeJob(Job job) {
        job.execute();
    }
    
    public void start() throws InterruptedException {
        while (true) {
            lock.lock();
            
            while (pq.size() == 0) {
                newJobArrived.await();
            }

            while (pq.size() != 0) {
                long sleepTime = pq.peek().executeAt - System.currentTimeMillis();
                if (sleepTime <= 0) {
                    break;
                }
                newJobArrived.await(sleepTime, TimeUnit.MILLISECONDS);
            }

            Job j = pq.poll();
            executeJob(j);

            lock.unlock();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        DelayedJobScheduler scheduler = new DelayedJobScheduler();

        Thread service = new Thread(new Runnable() {
            public void run() {
                try {
                    scheduler.start();
                } catch (InterruptedException ie) {
                }
            }
        });

        service.start();

        Thread lateThread = new Thread(new Runnable() {
            public void run() {
                Job job = new Job(8000, "Hello this is the callback submitted first");
                scheduler.registerJob(job);
            }
        });
        lateThread.start();

        Thread.sleep(3000);

        Thread earlyThread = new Thread(new Runnable() {
            public void run() {
                Job job = new Job(1000, "Hello this is callback sumbitted second");
                scheduler.registerJob(job);
            }
        });
        earlyThread.start();
        
        lateThread.join();
        earlyThread.join();
    }
    
}
