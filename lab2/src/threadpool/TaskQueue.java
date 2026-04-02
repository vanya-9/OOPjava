package src.threadpool;

import java.util.LinkedList;
import java.util.Queue;

public class TaskQueue{
    Queue<Runnable> tasks = new LinkedList<>();
    private final int MAX_TASKS = 100;

    public synchronized void put(Runnable task)throws InterruptedException{
        while(tasks.size() >= MAX_TASKS){
            wait();
        }
        tasks.add(task);
        notifyAll();
    }

    public synchronized Runnable take()throws InterruptedException{
        while(tasks.isEmpty()){
            wait();
        }
        notifyAll();
        return tasks.poll();
    }
}