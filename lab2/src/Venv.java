package src;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class Venv extends JFrame {
    private final App app;
    private final JLabel machineOnFactoryNow;
    private final JLabel machineOnFactoryAll;
    public Venv(App app, Runnable onCloseAction){
        super("Интерфейс фабрики");
        super.setBounds(200, 100, 800, 800);
        //super.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                onCloseAction.run();
            }
        });

        Container container = super.getContentPane();
        container.setLayout(new GridLayout(3, 2, 2, 2));
        this.setVisible(true);

        this.app = app;

        machineOnFactoryNow = new JLabel("Машин на складе: 0", SwingConstants.CENTER);
        machineOnFactoryNow.setFont(new Font("Arial", Font.BOLD, 16));

        machineOnFactoryAll = new JLabel("Всего произведено:0", SwingConstants.CENTER);
        machineOnFactoryAll.setFont(new Font("Arial", Font.BOLD, 16));

        JSlider frequencySlider = new JSlider(JSlider.HORIZONTAL, 100, 50000, 1000);
        frequencySlider.setMajorTickSpacing(1000);
        frequencySlider.setMinorTickSpacing(100);
        frequencySlider.setPaintTicks(true);
        frequencySlider.setPaintLabels(true);
        frequencySlider.setBorder(BorderFactory.createTitledBorder("Задержка покупки машин в мс"));

        frequencySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e){
                int currentFrequency = frequencySlider.getValue();
                app.setDealerFrequency(currentFrequency);
            }
        });

        container.add(machineOnFactoryAll);
        container.add(machineOnFactoryNow);
        container.add(frequencySlider);

        Timer updateTimer = new Timer(200, e -> updateStats());
        updateTimer.start();

    }
    
    private void updateStats() {
        machineOnFactoryNow.setText("Машин на складе: " + app.sizeMachineFactory());
        machineOnFactoryAll.setText("Всего произведено: " + app.getTotatalWorkMachine());
    }
}