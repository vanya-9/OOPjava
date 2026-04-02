package src;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Supplier;

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
import src.threadpool.Controller;
import src.threadpool.ThreadPool;

public class Main {
    public static void main(String[] args){
        Venv venvApp = new Venv();
        ArrayList<AccessorySupplier> accessorySuppliers = new ArrayList<>();
        ArrayList<Dealer> dealers = new ArrayList<>();
        CarcaseSupplier carcaseSupplier;
        EngineSupplier engineSupplier;
        Controller controller;
        ThreadPool threadPool;
        try{
            InputData config = new InputData("src/config.properties");
            Factory<Accessory> accessoryFactory = new Factory<>(config.getInt("FactoryAccessorySize"));
            Factory<Carcase> carcaseFactory = new Factory<>(config.getInt("FactoryCarcaseSize"));
            Factory<Engine> engineFactory = new Factory<>(config.getInt("FactoryEngineSize"));
            Factory<Machine> machineFactory = new Factory<>(config.getInt("FactoryMachineSize"));

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

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Покидаем программу");
                for(Thread thread : accessorySuppliers){
                    thread.interrupt();
                }

                for(Thread thread : dealers){
                    thread.interrupt();
                }

                carcaseSupplier.interrupt();
                engineSupplier.interrupt();
                threadPool.stopAll();
                controller.interrupt();
                System.out.println("[SYSTEM] Все потоки остановлены. Программа завершена.");
                venvApp.dispose();
            }));
        

        } catch(IOException e){
            System.err.println("Ошибка чтения конфигурации " + e.getMessage());
        }
        
    }
}
