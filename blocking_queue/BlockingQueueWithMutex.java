import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueueWithMutex<T> {
	private T[] queue;
	private int capacity;
	private int size = 0;
	private int head = 0;
	private int tail = 0;
	private Lock lock = new ReentrantLock();

	@SuppressWarnings("unchecked")
	public BlockingQueueWithMutex(int capacity) {
		queue = (T[]) new Object[capacity];
		this.capacity = capacity;
	}

	public void enqueue(T item) throws InterruptedException {
		lock.lock();
		while (size == capacity) {
			lock.unlock();
			lock.lock();
		}

		// at this point size is less than capacity
		// do enqueueing
		if (tail == capacity) {
			tail = 0;
		}
		queue[tail] = item;
		tail++;
		size++;

		lock.unlock();
	}

	public T dequeue() throws InterruptedException {
		lock.lock();
		while (size == 0) {
			lock.unlock();
			lock.lock();
		}
		
		// at this point size is zero
		// do dequeueing
		if (head == capacity) {
			head = 0;
		}
		T item = queue[head];
		queue[head] = null;
		head++;
		size--;

		lock.unlock();
		return item;
	}

	public static void main(String[] args) {
		final BlockingQueueWithMutex<Integer> bq = new BlockingQueueWithMutex<>(5);

		Thread producerThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					int item = ThreadLocalRandom.current().nextInt();
					try {
						System.out.println("Trying to enqueue...");
						bq.enqueue(item);
						System.out.printf("Enqueued %d\n", item);
						Thread.sleep(ThreadLocalRandom.current().nextInt(500));
					} catch (InterruptedException ie) {

					}
				}
			}
		});

		Thread consumerThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						System.out.println("Trying to dequeue...");
						int item = bq.dequeue();
						System.out.printf("Dequeued %d\n", item);
						Thread.sleep(ThreadLocalRandom.current().nextInt(500));
					} catch (InterruptedException ie) {

					}
				}
			}
		});

		producerThread.start();
		consumerThread.start();

		try {
			producerThread.join();
			consumerThread.join();
		} catch (InterruptedException ie) {

		}
	}
}