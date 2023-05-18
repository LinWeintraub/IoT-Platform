/* ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ *
~ Author        Lin Weintraub                                    *
~ Date          2.03.2023                                        ~
* Reviewer      Ouri Berreby                                     *
~ Description   Thread Pool                                      ~
* Group         FS133                                            *
~ Company       ILRD Ramat Gan                                   ~
* ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ * ~ */

package com.IoTPlatform.ThreadPool;

import com.IoTPlatform.WaitableQueue.WaitablePQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadPool<V> implements Executor {
    private List<Thread> threadList = null;
    private WaitablePQueue<Task> taskPQ = null;
    private boolean isShutdown = false;
    private AtomicBoolean isPaused = new AtomicBoolean(false);
    private int numberOfThreads = 0;
    private Object pauseLock = new Object();

//--------------------------------------------------------------------------------------------------------------------//
    public ThreadPool(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
        this.threadList = new ArrayList<>(numberOfThreads);
        this.taskPQ = new WaitablePQueue<>();

        for (int i = 0; i < numberOfThreads; ++i) {
            Thread thread = new WorkerThread();
            threadList.add(thread);
            thread.start();
        }
    }

    public Future<Void> submit(Runnable runnable, Priority priority) {
        if (isShutdown) {
            throw new RejectedExecutionException("Thread pool is shutdown");
        }

        Callable<Void> callable = () -> {
            runnable.run();
            return null;
        };

        return submit(callable, priority);
    }

    public <V> Future<V> submit(Runnable runnable, Priority priority, V value) {
        if (isShutdown) {
            throw new RejectedExecutionException("Thread pool is shutdown");
        }

        Callable<V> callable = () -> {
            runnable.run();
            return value;
        };

        Task<V> task = new Task<>(callable, priority.getValue());
        taskPQ.enqueue(task);

        return task.getFuture();
    }

    public <V> Future<V> submit(Callable<V> callable) {
        if (isShutdown) {
            throw new RejectedExecutionException("Thread pool is shutdown");
        }

        return submit(callable, Priority.MEDIUM);
    }

    public <V> Future<V> submit(Callable<V> callable, Priority priority) {
        if (isShutdown) {
            throw new RejectedExecutionException("Thread pool is shutdown");
        }

        Task<V> task = new Task<>(callable, priority.getValue());
        taskPQ.enqueue(task);

        return task.getFuture();
    }

    public void setNumOfThreads(int numThreads) {
        if (numThreads < 1) {
            throw new IllegalArgumentException("Number of threads cannot be less than 1");
        }

        if (numThreads > numberOfThreads) {
            int numToAdd = numThreads - numberOfThreads;

            for (int i = 0; i < numToAdd; ++i) {
                Thread thread = new WorkerThread();
                threadList.add(thread);
                thread.start();
            }
        } else if (numThreads < numberOfThreads) {
            int numToRemove = numberOfThreads - numThreads;

            for (int i = 0; i < numToRemove; ++i) {
                Task<V> task = new Task<>(SelfTerminationTask, 4);
                taskPQ.enqueue(task);
            }
        }

        numberOfThreads = numThreads;
    }

    public void pause() {
        if (isShutdown) {
            throw new RejectedExecutionException("Thread pool is shutdown");
        }

        if (isPaused.get()) {
            throw new IllegalStateException("Thread pool is already paused");
        }

        isPaused.set(true);
    }

    public void resume() {
        if (isShutdown) {
            throw new RejectedExecutionException("Thread pool is shutdown");
        }

        if (!isPaused.get()) {
            throw new IllegalStateException("Thread pool is not paused");
        }

        isPaused.set(false);
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

    public void shutdown() {
        if (isPaused.get()) {
            resume();
        }

        isShutdown = true;
        Task<V> task = new Task<>(SelfTerminationTask, 0);

        for (int i = 0; i < numberOfThreads; ++i) {
            taskPQ.enqueue(task);
        }
    }

    public void awaitTermination() {
        if (!isShutdown) {
            throw new RejectedExecutionException("Thread pool is not shutdown");
        }

        for (int i = 0; i < threadList.size(); i++) {
            Thread thread = threadList.get(i);
            try {
                thread.join();
            } catch (InterruptedException e) {
                // continue waiting for thread to terminate
            }
        }
    }

    @Override
    public void execute(Runnable runnable) {
        submit(runnable, Priority.MEDIUM);
    }

    private class Task<V> implements Comparable<Task> {
        private Callable callable = null;
        private int priority = 0;
        private taskFuture future = null;

        private boolean isDone = false;
        private boolean isCancelled = false;

        private Task(Callable callable, int priority) {
            this.callable = callable;
            this.priority = priority;
            this.future = new taskFuture(this);
            this.isCancelled = false;
            this.isDone = false;
        }

        @Override
        public int compareTo(Task task) {
            return task.priority - this.priority;
        }

        private Future getFuture() {
            return this.future;
        }

        private Callable getCallable() {
            return this.callable;
        }

        private class taskFuture implements Future<V> {
            private boolean isDone = false;
            private boolean isCancelled = false;
            private V result = null;
            private Semaphore semaphore = new Semaphore(0);
            private Thread workingThread = new WorkerThread();
            private Task currentTask = null;

            public taskFuture (Task currentTask) {
                this.currentTask = currentTask;
            }
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                if (isDone || isCancelled) {
                    return false;
                }

                isCancelled = true;

                if (taskPQ.remove(currentTask)) {
                    isDone = false;
                    return true;
                }

                else if (mayInterruptIfRunning) {
                    workingThread.interrupt();
                }

                return true;
            }

            @Override
            public boolean isCancelled() {
                return isCancelled;
            }

            @Override
            public boolean isDone() {
                return isDone;
            }

            @Override
            public V get() throws InterruptedException, ExecutionException {
                semaphore.acquire();

                return result;
            }

            @Override
            public V get(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
                if (!semaphore.tryAcquire(timeout, timeUnit)) {
                    throw new TimeoutException();
                }
                return result;

            }

            public void setResult(V result) {
                this.result = result;
                isDone = true;
                semaphore.release();
            }

            public void setWorkingThread(Thread workingThread) {
                this.workingThread = workingThread;
            }

            public Thread getWorkingThread() {
                return this.workingThread;
            }
        }
    }
//--------------------------------------------------------------------------------------------------------------------//
    private class WorkerThread extends Thread {
    private boolean isExecutable = true;
    private Task.taskFuture taskFuture = null;
    V result = null;

    public void setExecutable(boolean executable) {
        isExecutable = executable;
    }

    @Override
    public void run() {
        Task<V> task = null;

        while (isExecutable) {
            try {
                task = taskPQ.dequeue();

                if (isPaused.get()) {
                    synchronized (pauseLock) {
                        pauseLock.wait();
                    }
                }

                taskFuture = (Task.taskFuture) task.getFuture();
                taskFuture.setWorkingThread(Thread.currentThread());

                if (task.getFuture().isCancelled()) {
                    taskFuture.isDone = false;
                    taskFuture.setResult(null);
                    continue;
                }

                Callable<V> callable = task.getCallable();
                result = callable.call();

                taskFuture.setResult(result);
                taskFuture.isDone = true;


            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                taskFuture.setResult(null);
                break;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            threadList.remove(this);
        }
    }
}
//--------------------------------------------------------------------------------------------------------------------//
    Callable<Void> SelfTerminationTask = () -> {
        Thread currentThread = Thread.currentThread();
        ((WorkerThread)currentThread).setExecutable(false);

        return null;
    };
}