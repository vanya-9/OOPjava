package src.threadpool;

import src.factory.ExecFactory;
import src.factory.Factory;
import src.parts.Accessory;
import src.parts.Carcase;
import src.parts.Engine;
import src.parts.Machine;

public class Controller extends Thread {
    Factory<Machine> machineFactory;
    Factory<Carcase> carcaseFactory;
    Factory<Accessory> accessFactory;
    Factory<Engine> engineFactory;
    ThreadPool threadPool;

    public Controller(Factory<Machine> mcF, Factory<Carcase> carF, Factory<Accessory> AcF, Factory<Engine> enF, ThreadPool threadPool){
        this.machineFactory = mcF;
        this.carcaseFactory = carF;
        this.accessFactory = AcF;
        this.engineFactory = enF;
        this.threadPool = threadPool;
    }

    @Override
    public void run(){
        try{
            while(!isInterrupted()){
                machineFactory.waitSpace();
                ExecFactory task = new ExecFactory(machineFactory, carcaseFactory, accessFactory, engineFactory);
                threadPool.put(task);
            }
        }catch(InterruptedException e){
            System.out.println(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
