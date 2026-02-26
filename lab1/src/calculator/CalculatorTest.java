package calculator;

import calculator.command.*;
import calculator.context.ExecutionContext;
import calculator.factory.CommandFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class CalculatorTest {

    private ExecutionContext context;

    @BeforeMethod
    public void setUp() {
        context = new ExecutionContext();
    }

    @Test
    public void testAddition() {
        context.push(1.0);
        context.push(2.0);

        Command add = new Add();
        add.execute(context, Collections.emptyList());

        assertEquals(context.pop(), 3.0, "1 + 2 должно быть 3");
    }

    @Test
    public void testSubtraction() {
        context.push(10.0);
        context.push(4.0);

        Command sub = new Sub();
        sub.execute(context, Collections.emptyList());

        assertEquals(context.pop(), -6.0, "Порядок: верхний - нижний");
    }

    @Test
    public void testDivision() {
        context.push(8.0);
        context.push(4.0);

        Command div = new Div();
        div.execute(context, Collections.emptyList());

        assertEquals(context.pop(), 0.5, "Порядок: верхний / нижний");
    }

    @Test
    public void testDivisionByZero() {
    context.push(0.0);
    context.push(8.0);

    Command div = new Div();

    try {
        div.execute(context, Collections.emptyList());
        Assert.fail("Ожидалось ArithmeticException");
    } catch (ArithmeticException e) {
        Assert.assertEquals(e.getMessage(), "Division by zero");
    }
}

    @Test
    public void testMultiplication() {
        context.push(9.0);
        context.push(9.0);

        Command mul = new Mul();
        mul.execute(context, Collections.emptyList());

        assertEquals(context.pop(), 81.0, "9 * 9 = 81");
    }

    @Test
    public void testSQRT() {
        context.push(9.0);

        Command sqrt = new Sqrt();
        sqrt.execute(context, Collections.emptyList());

        assertEquals(context.pop(), 3.0, "sqrt(9) = 3");
    }

    // @Test
    // public void testUnknownCommand() {
    //     CommandFactory factory = new CommandFactory();
    //     try {
    //         factory.create("UNKNOWN_COMMAND");
    //         Assert.fail("Ожидалось RuntimeException");
    //     } catch (RuntimeException e) {
    //         Assert.assertTrue(e.getMessage().contains("Unknow command"));
    //     }
    // }

    @Test
    public void testDefineAndPush() {
        Command define = new Define();
        define.execute(context, Arrays.asList("a", "4.0"));

        Command push = new Push();
        push.execute(context, Collections.singletonList("a"));

        assertEquals(context.pop(), 4.0, "Переменная a = 4.0");
    }

    @Test
    public void testPopEmptyStack() {
        try {
            context.pop();
            Assert.fail("Ожидалось исключение");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
        }
    }

    @Test
    public void testPeekEmptyStack() {
        try {
            context.peek();
            Assert.fail("Ожидалось исключение");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
        }
    }

    @Test
    public void testFullScenario() {
        new Define().execute(context, Arrays.asList("a", "4.0"));
        new Push().execute(context, Collections.singletonList("a"));
        new Sqrt().execute(context, Collections.emptyList());

        assertEquals(context.peek(), 2.0, "sqrt(4) = 2");
    }
}