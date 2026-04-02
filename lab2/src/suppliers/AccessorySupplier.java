package src.suppliers;

import java.util.concurrent.atomic.AtomicInteger;

import src.factory.Factory;
import src.parts.Accessory;


public class AccessorySupplier extends Thread{
    private int frequency;
    private final Factory<Accessory> accessoryFactory;
    private static final AtomicInteger GlobalID = new AtomicInteger(0);

    public AccessorySupplier(int frequency, Factory<Accessory> accFact){
        this.frequency = frequency;
        this.accessoryFactory = accFact;
    }

    @Override
    public void run(){
        try{
            while(!isInterrupted()){
                int id = GlobalID.incrementAndGet();
                Accessory accessory = new Accessory(id);
                accessoryFactory.put(accessory);
                Thread.sleep(frequency);
            }
        }catch(InterruptedException e){
            System.err.println("Ты поймал исключение " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
