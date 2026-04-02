package src.parts;

public class Machine {
    private final int id;
    private final Accessory accessory;
    private final Carcase carcase;
    private final Engine engine;


    public Machine(int id, Accessory acces, Carcase carc, Engine eng){
        this.id = id;
        this.accessory = acces;
        this.carcase = carc;
        this.engine = eng;
    }

    public int getId(){
        return this.id;
    }
}
