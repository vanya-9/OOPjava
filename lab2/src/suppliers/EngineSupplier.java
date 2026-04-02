package src.suppliers;

import java.util.concurrent.atomic.AtomicInteger;

import src.factory.Factory;
import src.parts.Engine;

public class EngineSupplier extends Thread{
    private int frequency;
    private final Factory<Engine> engineFactory;
    private static final AtomicInteger GlobalId = new AtomicInteger(0);

    public EngineSupplier(int frequency, Factory<Engine> engF){
        this.frequency = frequency;
        this.engineFactory = engF;
    }

    @Override
    public void run(){
        try {
            while(!isInterrupted()){
                int id = GlobalId.incrementAndGet();
                Engine engine = new Engine(id);
                engineFactory.put(engine);
                Thread.sleep(frequency);
            }
        } catch (InterruptedException e) {
            System.err.println("Ты поймал исключение " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
}
