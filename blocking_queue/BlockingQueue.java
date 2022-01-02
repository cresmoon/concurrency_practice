import java.util.concurrent.ThreadLocalRandom;

public class BlockingQueue<T> {
    private T[] queue;
    private int capacity;
    private int size = 0;
    private int head = 0;
    private int tail = 0;

    @SuppressWarnings("unchecked")
    public BlockingQueue(int cap) {
        capacity = cap;
        queue = (T[]) new Object[cap];
    }

    public synchronized void enqueue(T item) throws InterruptedException {
        while (size == capacity) {
            // queue full
            wait();
        }
        if (tail == capacity) {
            tail = 0;
        }
        queue[tail] = item;
        tail++;
        size++;
        notifyAll();
    }
    
    public synchronized T dequeue() throws InterruptedException {
        while (size == 0) {
            // queue empty
            wait();
        }
        if (head == capacity) {
            head = 0;
        }
        T item = queue[head];
        queue[head] = null;
        head++;
        size--;
        notifyAll();
        return item;
    }

    public static void main(String[] args) {
        final BlockingQueue<Integer> blockingQ = new BlockingQueue<>(5);

        Thread producerThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Integer item = ThreadLocalRandom.current().nextInt();
                        System.out.println("Trying to enqueue");
                        blockingQ.enqueue(item);
                        System.out.printf("Enqueued %d\n", item);
                        Thread.sleep(ThreadLocalRandom.current().nextInt(500));    
                    } catch (InterruptedException ie) {
                        // swallow
                    }
                }
            }
        });
        Thread consumerThread1 = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Integer item = blockingQ.dequeue();
                        System.out.printf("Dequeueing %d\n", item);
                        Thread.sleep(ThreadLocalRandom.current().nextInt(100));    
                    } catch (InterruptedException ie) {
                        // swallow
                    }
                }
            }
        });
        Thread consumerThread2 = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        System.out.println("Trying to dequeue");
                        Integer item = blockingQ.dequeue();
                        System.out.printf("Dequeued %d\n", item);
                        Thread.sleep(ThreadLocalRandom.current().nextInt(100));    
                    } catch (InterruptedException ie) {
                        // swallow
                    }
                }
            }
        });

        try {
            producerThread.start();
            Thread.sleep(5000);
            consumerThread1.start();
            consumerThread2.start();

            producerThread.join();
            consumerThread1.join();
            consumerThread2.join();
        } catch (InterruptedException ie) {
            // swallow
        }

    }

}
