package calculator.context;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.HashMap;

public class ExecutionContext {
    private Deque<Double> stack = new ArrayDeque<>();
    //Обычно ArrayDeque используют как более быструю и экономичную по памяти 
    // альтернативу Stack и LinkedList. Он поддерживает вставку и удаление 
    // элементов с обоих концов за константное время O(1). хранит элементы внутри обычного 
    // циклического массива
    private Map<String, Double> variables = new HashMap<>();
    private boolean shouldContinue = true; //эта переменная используется в основном классе Calculator, где Main, проверяем читать ли дальше строки или нет.

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

    public boolean shouldContinue(){
        return shouldContinue;
    }

    public void stop(){
        this.shouldContinue = false;
    }
}
