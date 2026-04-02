package src.factory;

import src.parts.Accessory;
import src.parts.Carcase;
import src.parts.Machine;
import src.parts.Engine;

import java.util.concurrent.atomic.AtomicInteger;

import src.factory.Factory;

public class ExecFactory implements Runnable {
    Factory<Machine> machineFactory;
    Factory<Carcase> carcaseFactory;
    Factory<Accessory> accessFactory;
    Factory<Engine> engineFactory;
    private static final AtomicInteger GenetatorID = new AtomicInteger(0);
    
    public ExecFactory(Factory<Machine> mcF, Factory<Carcase> carF, Factory<Accessory> AcF, Factory<Engine> enF){
        this.machineFactory = mcF;
        this.carcaseFactory = carF;
        this.accessFactory = AcF;
        this.engineFactory = enF;
    }

    @Override
    public void run(){
        try{
            int id = GenetatorID.incrementAndGet();
            Carcase carcase = carcaseFactory.get();
            Accessory accessory = accessFactory.get();
            Engine engine = engineFactory.get();
            Machine machine = new Machine(id, accessory, carcase, engine);
            machineFactory.put(machine);
        }catch(InterruptedException e){
            System.out.println("Потоки " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
}
