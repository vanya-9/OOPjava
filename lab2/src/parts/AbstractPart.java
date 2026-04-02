package src.parts;

public abstract class AbstractPart implements Detail {
    private final int id;
    
    public AbstractPart(int id){
        this.id = id;
    }

    @Override
    public int getId(){
        return this.id;
    }
}
