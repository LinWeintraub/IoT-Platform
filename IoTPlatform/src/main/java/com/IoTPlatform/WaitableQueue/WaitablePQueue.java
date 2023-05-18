package com.IoTPlatform.WaitableQueue;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

public class WaitablePQueue<T> {
    private PriorityQueue<T> queue = null;
    private final Semaphore isEmptySemaphore = new Semaphore(0);
    private final ReentrantLock lock = new ReentrantLock();

    public WaitablePQueue(Comparator<? super T> comparator) {
        this.queue = new PriorityQueue<>(comparator);
    }

    public WaitablePQueue() {
        this.queue = new PriorityQueue<>();
    }

    public void enqueue(T element) {
        lock.lock();
        try {
            queue.add(element);
            isEmptySemaphore.release();

        } finally {
            lock.unlock();
        }
    }

    public T dequeue() throws InterruptedException {
        try {
            isEmptySemaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        lock.lock();
        T value = queue.poll();
        lock.unlock();

        return value;
    }

    //timeout in milliseconds
    public T dequeue(int timeout) throws TimeoutException, InterruptedException {
            boolean isTrue = isEmptySemaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS);

            if (!isTrue) {
                throw new TimeoutException();
            }

            lock.lock();
            T value = queue.poll();
            lock.unlock();

            return value;
    }

    public boolean remove(T element) {
        boolean isRemove = false;
        if (isEmptySemaphore.tryAcquire()) {
            lock.lock();
            isRemove = queue.remove(element);

            if (!isRemove) {
                isEmptySemaphore.release();
            }

            lock.unlock();
        }

        return isRemove;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}