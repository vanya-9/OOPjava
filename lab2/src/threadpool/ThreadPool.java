package src.threadpool;

import java.util.ArrayList;
import java.util.List;

import src.factory.ExecFactory;

public class ThreadPool {
    private final List<WorkerThread> workers = new ArrayList<>();
    private final TaskQueue queue = new TaskQueue();

    public ThreadPool(int threadsCount){
        for (int i = 0; i < threadsCount; i++){
            WorkerThread worker = new WorkerThread();
            workers.add(worker);
            worker.start();
        }
    }

    public void put(ExecFactory task) throws InterruptedException{
        queue.put(task);
    }

    public void stopAll(){
        for(Thread thread : workers){
            thread.interrupt();
        }
    }

    private class WorkerThread extends Thread{

        @Override
        public void run(){

            try{
                while(!isInterrupted()){
                    Runnable task = queue.take();
                    task.run();
                }
            }catch(InterruptedException e){
                System.out.println("Ошибка " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        
    }
}
