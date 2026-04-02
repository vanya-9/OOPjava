package src.suppliers;

import src.factory.Factory;
import src.parts.Machine;

public class Dealer extends Thread {
    private final int frequency;
    private final Factory<Machine> machineFactory;

    public Dealer(int frequency, Factory<Machine> mchF){
        this.frequency = frequency;
        this.machineFactory = mchF;
    }

    @Override
    public void run(){
        try{
            while(!isInterrupted()){
                Machine machine = machineFactory.get();
                System.out.println("Dealer [" + getName() + "]: Куплена машина ID: " + machine.getId());
                Thread.sleep(frequency);
            }

        }catch(InterruptedException e){
            System.out.println("исклчючение  " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
