package calculator.factory;

import java.io.InputStream;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import calculator.command.Command; 
public class CommandFactory {
    private Map<String, String> commands = new HashMap<>();//Возможные команды для калькулятора
    private static final Logger logger = Logger.getLogger(CommandFactory.class.getName());

    public CommandFactory(){ //Открывает файл properties по сути и загружает, потом заполняет возможные комманды из command.propertiec
        try(InputStream input = CommandFactory.class.getResourceAsStream("commands.properties")){  //Найди в пакете где лежит CommandFactory файл commands.properties
            Properties properties = new Properties(); 
            if(input==null){
                logger.severe("File not found, properties");
                throw new RuntimeException("Can not load properties");
            }
            properties.load(input); 

            for(String key : properties.stringPropertyNames()){ 
                commands.put(key, properties.getProperty(key)); 
            }
        }
        catch(Exception e){
            logger.severe("Mistake working file properties");
            throw new RuntimeException();
        }
    }

    public Command create(String commandName){ 
        String className = commands.get(commandName); //Пользователь пишет PUSH, мы тут получаем ИМЯ КЛАССА, реализующего эту команду
        if (className == null){
            logger.severe("Command not found");
            throw new RuntimeException("Unknow command"); 
        }
                
        try {
            Class<?> commandClass = Class.forName(className); //Поиск класса по имени
            return (Command) commandClass.getDeclaredConstructor().newInstance(); 
        } catch (Exception e) {
            logger.severe("Mistake create command");
            throw new RuntimeException();
        }
    }
}
// В твоем коде использование **дженериков (Generics)** и механизм **затирания типов (Type Erasure)** ярче всего проявляются в двух местах: при работе с коллекциями и при динамической загрузке классов через рефлексию.

// Давай разберем, где именно они используются и как их «затирает» Java во время компиляции.

// ---

// ### 1. Дженерики в коллекциях (`List<String>`, `Map<String, Double>`)

// Ты активно используешь параметризованные типы в интерфейсах и классах:

// * В сигнатуре метода: `List<String> commandArgs`
// * В контексте: `Deque<Double> stack` и `Map<String, Double> variables`
// * В фабрике: `Map<String, String> commands`

// #### Как это видит компилятор (Дженерики)

// Компилятор Java использует эти типы для **строгой проверки безопасности** во время написания кода. Например, когда ты пишешь:

// ```java
// String arg = commandArgs.get(0);

// ```

// Компилятор точно знает, что из списка достается именно `String`, поэтому тебе не нужно писать ручное приведение типов вроде `(String) commandArgs.get(0)`.

// #### Что происходит в скомпилированном байт-коде (Затирание типов)

// В Java дженерики существуют **только на этапе компиляции**. Как только компилятор проверил, что код безопасен, он активирует механизм **затирания типов (Type Erasure)**. Он удаляет всю информацию о типах в угловых скобках `<...>` и заменяет их на базовый тип `Object` (или на верхнюю границу, если она указана).

// В итоге, после компиляции твои коллекции внутри JVM превращаются в «сырые» (Raw) типы, а в местах извлечения данных компилятор сам подставляет явное приведение типов.

// **Вот как твой код выглядит для тебя и как он превращается в байт-код после затирания:**

// | Твой исходный код | Что получается после затирания типов (байт-код) |
// | --- | --- |
// | `List<String> commandArgs` | `List commandArgs` |
// | `Map<String, Double> variables` | `Map variables` |
// | `String arg = commandArgs.get(0);` | `String arg = (String) commandArgs.get(0);` |
// | `double value = context.getVariable(arg);` | `double value = ((Double) context.getVariable(arg)).doubleValue();` |

// ---

// ### 2. Дженерики в Рефлексии (`Class<?>`)

// В классе `CommandFactory` при динамическом создании команд используется так называемый **wildcard (символ подстановки)**:

// ```java
// Class<?> commandClass = Class.forName(className); 
// return (Command) commandClass.getDeclaredConstructor().newInstance();

// ```

// #### Что здесь происходит:

// * `Class<?>` означает «класс неизвестного типа». Поскольку на этапе написания кода фабрика не знает, какой именно класс она загрузит из файла конфигурации (`Add`, `Sub` или `Push`), ты не можешь написать `Class<Add>`.
// * Из-за затирания типов метод `Class.forName()` возвращает просто сырой объект `Class`. Использование `Class<?>` — это способ сказать компилятору: *«Я знаю, что тип здесь динамический, не ругайся на отсутствие типизации»*.
// * Так как после затирания типов метод `newInstance()` возвращает тип `Object`, тебе приходится делать **явное нисходящее приведение типов (Downcasting)** к интерфейсу: `(Command)`.

// ---

// ### Вопросы на защите, к которым нужно быть готовым:

// 1. **Зачем нужно затирание типов?**
// * *Ответ:* Для обеспечения обратной совместимости со старыми версиями Java (до версии 1.5, где дженериков не было). Старый байт-код без дженериков и новый байт-код с затертыми дженериками могут работать вместе в одной JVM.


// 2. **Можно ли в рантайме (во время работы программы) узнать, что `commandArgs` был именно `List<String>`, а не `List<Integer>`?**
// * *Ответ:* Нет, нельзя. Из-за затирания типов внутри объекта списка этой информации больше нет. Для JVM это просто `List`.


// 3. **Почему `stack` объявлен как `Deque<Double>`, а не `Deque<double>`?**
// * *Ответ:* Дженерики в Java работают только со ссылочными типами данных (классами). Примитивы (типа `double`, `int`) нельзя передавать в generic-блоки. Поэтому используются классы-обертки (`Double`), а процесс перехода от `double` к `Double` и обратно берет на себя автоматическая упаковка/распаковка (Autoboxing/Unboxing).
