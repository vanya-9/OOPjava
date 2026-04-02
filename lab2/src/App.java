package src;
import src.threadpool.Controller;
import java.util.ArrayList;

import src.factory.Factory;
import src.inputdata.InputData;
import src.parts.Accessory;
import src.parts.Carcase;
import src.parts.Engine;
import src.parts.Machine;
import src.suppliers.AccessorySupplier;
import src.suppliers.CarcaseSupplier;
import src.suppliers.Dealer;
import src.suppliers.EngineSupplier;
import src.threadpool.ThreadPool;

public class App {
    private final InputData config;
    
    private final ArrayList<AccessorySupplier> accessorySuppliers = new ArrayList<>();
    private final ArrayList<Dealer> dealers = new ArrayList<>();

    private Factory<Machine> machineFactory;
    private CarcaseSupplier carcaseSupplier;
    private EngineSupplier engineSupplier;
    private ThreadPool threadPool;
    private Controller controller;

    public boolean logOn;

    public App(InputData cfg){
        this.config = cfg;
        this.logOn = this.config.getBoolean("LogOn");
    }

    public void start(){
        Factory<Accessory> accessoryFactory = new Factory<>(config.getInt("FactoryAccessorySize"));
        Factory<Carcase> carcaseFactory = new Factory<>(config.getInt("FactoryCarcaseSize"));
        Factory<Engine> engineFactory = new Factory<>(config.getInt("FactoryEngineSize"));
        machineFactory = new Factory<>(config.getInt("FactoryMachineSize"));

        int countAccessorySupliers = config.getInt("AccessorySuppliers");
        int accessorySupplierFrequency = config.getInt("AccessorySupplierFrequency");
        int carcaseSupplierFrequency = config.getInt("CarcaseSupplierFrequency");
        int engineSupplierFrequency = config.getInt("EngineSupplierFrequency");
        int countWorkers = config.getInt("Workers");
        for(int i = 0; i < countAccessorySupliers; i++){
            AccessorySupplier supplier = new AccessorySupplier(accessorySupplierFrequency, accessoryFactory);
            accessorySuppliers.add(supplier);
            supplier.start();
        }
        carcaseSupplier = new CarcaseSupplier(carcaseSupplierFrequency, carcaseFactory);
        engineSupplier = new EngineSupplier(engineSupplierFrequency, engineFactory);
        carcaseSupplier.start();
        engineSupplier.start();

        threadPool = new ThreadPool(countWorkers);
        controller = new Controller(machineFactory, carcaseFactory, accessoryFactory, engineFactory, threadPool);
        controller.start();

        int dealerCount = config.getInt("Dealers");
        int dealerFrequency = config.getInt("DealerFrequency");
        for (int i = 0; i < dealerCount; i++) {
            Dealer dealer = new Dealer(dealerFrequency, machineFactory);
                dealer.start();
                dealers.add(dealer);
            }
    }

    public void stop(){
        System.out.println("Покидаем программу...");
        controller.interrupt();

        for(Thread thread : accessorySuppliers){
            thread.interrupt();
        }
        carcaseSupplier.interrupt();
        engineSupplier.interrupt();
        threadPool.stopAll();

        for(Thread thread : dealers){
            thread.interrupt();
        }
        System.out.println("[SYSTEM] Все сигналы остановки отправлены. Выход...");
    }

    public int sizeMachineFactory(){
        return machineFactory == null ? 0 : machineFactory.getSizeNotNull();
    }

    public int getTotatalWorkMachine(){
        return machineFactory.getTotalProducts();
    }

    public void setDealerFrequency(int frequency){
        for(Dealer dealer : dealers){
            dealer.setFrequency(frequency);
        }
    }

    public void setLog(boolean flag){
        logOn = flag;
        Factory.setLogF(flag);
    }


}
