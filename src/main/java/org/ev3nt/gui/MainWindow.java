package org.ev3nt.gui;

import org.ev3nt.modes.ScheduleMode;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public class MainWindow {
    public MainWindow(String title, int width, int height, double coefficient, boolean resizable) {
        window = new TwoPanelsWindow(coefficient);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setSize(width, height);
        window.setResizable(resizable);
        window.setTitle(title);

        JPanel leftPanel = window.getLeftPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        yearComboBox = new JComboBox<>();
        yearComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        yearComboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, yearComboBox.getPreferredSize().height));

        semesterComboBox = new JComboBox<>();
        semesterComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        semesterComboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, semesterComboBox.getPreferredSize().height));

        leftPanel.add(new JLabel("Учебный год"));
        leftPanel.add(yearComboBox);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(new JLabel("Семестр"));
        leftPanel.add(semesterComboBox);

        leftPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,10));

        JPanel rightPanel = window.getRightPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        tabbedPane = new JTabbedPane();

        rightPanel.add(tabbedPane);

        rightPanel.setBorder(BorderFactory.createEmptyBorder(10,10,15,15));
    }

    public <T extends ScheduleMode> void add(Supplier<T> itemFactory) {
        T item = itemFactory.get();

        tabbedPane.addTab(item.getName(), item.getPanel());
    }

    public void run() {
        window.setVisible(true);
    }

    TwoPanelsWindow window;
    JComboBox<String> yearComboBox;
    JComboBox<String> semesterComboBox;
    JTabbedPane tabbedPane;

//    public JComboBox<String> getYearComboBox() {
//        return yearComboBox;
//    }
//
//    public JComboBox<String> getSemesterComboBox() {
//        return semesterComboBox;
//    }
//
//    public JTabbedPane getTabbedPane() {
//        return tabbedPane;
//    }
}
