package src;

import java.io.IOException;
import src.inputdata.InputData;

public class Main {
    public static void main(String[] args) {
        try {
            InputData config = new InputData("src/config.properties");
            App app = new App(config);
            
            app.start();

            new Venv(app, app::stop);

        } catch (IOException e) {
            System.err.println("Ошибка чтения конфигурации: " + e.getMessage());
        }
    }
}