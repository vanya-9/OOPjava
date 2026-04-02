package src.factory;

import java.util.LinkedList;
import java.util.Queue;

public class Factory<T> {
    private final int capacity;
    private final Queue<T> items;

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
        notifyAll();
    }

    public synchronized T get() throws InterruptedException{
        while(items.isEmpty()) {
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
}
