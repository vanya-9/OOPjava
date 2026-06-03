package src.threadpool;

import java.util.LinkedList;
import java.util.Queue;

public class TaskQueue{
    Queue<Runnable> tasks = new LinkedList<>();
    private final int maxTasks;

    public TaskQueue(int maxCountTasks){
        this.maxTasks = maxCountTasks;
    }

    public synchronized void put(Runnable task)throws InterruptedException{
        while(tasks.size() >= maxTasks){
            wait();
        }
        tasks.add(task);
        notifyAll();
    }

    public synchronized Runnable take()throws InterruptedException{
        while(tasks.isEmpty()){
            if(Thread.currentThread().isInterrupted()){
                throw new InterruptedException();
            }
            wait();
        }
        Runnable task = tasks.poll();
        notifyAll();
        return task;
    }
}