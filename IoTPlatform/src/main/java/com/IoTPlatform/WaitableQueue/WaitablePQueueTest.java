package com.IoTPlatform.WaitableQueue;

import java.util.Comparator;
import java.util.concurrent.TimeoutException;

class WaitablePQueueTest {
    public static void main(String[] args) throws InterruptedException, TimeoutException {
        WaitablePQueue<Integer> queue = new WaitablePQueue<>(Comparator.reverseOrder());
        WaitablePQueue<Integer> queue2 = new WaitablePQueue<>(Comparator.reverseOrder());

        // enqueue elements
        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3);
        queue.enqueue(4);
        queue.enqueue(5);

        // dequeue elements
        int element = queue.dequeue();
        System.out.println(element); // expected output: 5

        // remove an element
        boolean removed = queue.remove(3);
        System.out.println(removed); // expected output: true

        // dequeue an element with timeout
        element = queue.dequeue(1000);
        System.out.println(element); // expected output: 4

        // dequeue all remaining elements
        while (!queue.isEmpty()) {
            element = queue.dequeue();
            System.out.println(element);
        }
        // expected output: 2 1

        // test dequeue with timeout on an empty queue
        try {
            element = queue2.dequeue(1000);
        } catch (TimeoutException e) {
            System.out.println("queue is empty - timeout");
        }
    }
}
