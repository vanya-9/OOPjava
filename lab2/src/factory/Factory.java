package src.factory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class Factory<T> {
    private final int capacity;
    private final Queue<T> items;
    private volatile int countAllProducts = 0;
    private static volatile boolean logOn;
    private static final Logger logger = Logger.getLogger(Factory.class.getName());

    public Factory(int capacity) throws IllegalArgumentException{
        if (capacity <= 0){
            throw new IllegalArgumentException("capacity > 0");
        }
        this.capacity = capacity;
        this.items = new LinkedList<>();
    }

    public synchronized void put(T accs) throws InterruptedException{
        while(capacity <= items.size()){
            wait();
        }
        items.add(accs);
        countAllProducts += 1;
        if(logOn){
            logger.info(Thread.currentThread().getName() + " компонент: " + accs.getClass().getSimpleName());
        }
        notifyAll();
    }

    public synchronized T get() throws InterruptedException{
        while(items.isEmpty()) {
            if(logOn){
            logger.info(Thread.currentThread().getName() + "ждем, склад пустует");
        }
            wait();
        }
        notifyAll();
        return items.poll();
    }

    public synchronized void waitSpace()throws InterruptedException{
        while(capacity <=items.size()){
            wait();
        }
    }

    public synchronized int getSizeNotNull(){
        return items.size();
    }

    public synchronized int getTotalProducts(){
        return countAllProducts;
    }
    public static void setLogF(boolean flag){
        logOn = flag;
    }
}
