package src;

import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Venv extends JFrame {
    public Venv(Runnable onCloseAction){
        super("Интерфейс фабрики");
        super.setBounds(200, 100, 800, 800);
        super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                onCloseAction.run();
            }
        });

        Container container = super.getContentPane();
        container.setLayout(new GridLayout(3, 2, 2, 2));
        this.setVisible(true);
    }
}