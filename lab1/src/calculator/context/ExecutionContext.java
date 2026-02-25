package calculator.context;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.HashMap;

public class ExecutionContext {
    private Deque<Double> stack = new ArrayDeque<>();
    private Map<String, Double> variables = new HashMap<>();

    public void push(Double value){
        stack.push(value);
    }

    public double pop(){
        return stack.pop();
    }

    public double peek(){
        return stack.peek();
    }

    public void define(String name, Double value){
        variables.put(name, value);
    }

    public double getVariable(String name){
        return variables.get(name);
    }

    public boolean hasVariable(String name){
        return variables.containsKey(name);
    }

    public int getStackSize(){
        return stack.size();
    }
}
