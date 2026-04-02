package src.suppliers;

import src.parts.Carcase;
import src.factory.Factory;
import java.util.concurrent.atomic.AtomicInteger;

public class CarcaseSupplier extends Thread {
    private int frequency;
    private Factory<Carcase> carcaseFactory;
    private static final AtomicInteger GlobalID = new AtomicInteger(0);

    public CarcaseSupplier(int frq, Factory<Carcase> crcFac){
        this.frequency = frq;
        this.carcaseFactory = crcFac;
    }

    @Override
    public void run(){
        try{
            while(!isInterrupted()){
                int id = GlobalID.incrementAndGet();
                Carcase carcase = new Carcase(id);
                carcaseFactory.put(carcase);
                Thread.sleep(frequency);
            }
        }catch(InterruptedException e){
            System.out.println("исклчючение  " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
