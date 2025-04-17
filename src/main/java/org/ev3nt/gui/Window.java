package org.ev3nt.gui;

import org.ev3nt.files.ResourceLoader;
import org.ev3nt.modes.ScheduleMode;

import javax.swing.*;
import java.awt.*;
import java.time.Year;
import java.util.Objects;
import java.util.function.Supplier;

public class Window {
    public Window(String title, int width, int height, double coefficient, boolean resizable) {
        window = new TwoPanelsWindow(coefficient);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(width, height);
        window.setLocationRelativeTo(null);
        window.setResizable(resizable);
        window.setTitle(title);

        Image icon = new ImageIcon(ResourceLoader.getResource("icon.png")).getImage();
        window.setIconImage(icon);

        JPanel leftPanel = window.getLeftPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        yearComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        yearComboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, yearComboBox.getPreferredSize().height));

        semesterComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        semesterComboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, semesterComboBox.getPreferredSize().height));

        weekTypeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        weekTypeComboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, weekTypeComboBox.getPreferredSize().height));

        leftPanel.add(new JLabel("Учебный год"));
        leftPanel.add(yearComboBox);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(new JLabel("Семестр"));
        leftPanel.add(semesterComboBox);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(new JLabel("Дни недели"));
        leftPanel.add(weekTypeComboBox);

        leftPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,10));

        JPanel rightPanel = window.getRightPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        rightPanel.add(tabbedPane);

        rightPanel.setBorder(BorderFactory.createEmptyBorder(10,10,15,15));

        InitFields();
    }

    private void InitFields() {
        for (int i = Year.now().getValue(); i > 2011; i--) {
            yearComboBox.addItem(new YearItem(i - 1 + " - " + i, i - 1));
        }

        semesterComboBox.addItem(new SemesterItem("Весенний", 2));
        semesterComboBox.addItem(new SemesterItem("Осенний", 1));

        weekTypeComboBox.addItem(new WeekTypeItem("Все дни", true));
        weekTypeComboBox.addItem(new WeekTypeItem("Только используемые", false));

    }

    public <T extends ScheduleMode> void add(Supplier<T> itemFactory) {
        T item = itemFactory.get();

        tabbedPane.addTab(item.getName(), item.getPanel(this));
    }

    public int getYear() {
        return ((YearItem) Objects.requireNonNull(yearComboBox.getSelectedItem())).year;
    }

    public int getSemester() {
        return ((SemesterItem) Objects.requireNonNull(semesterComboBox.getSelectedItem())).semester;
    }
    
    public boolean getWeekType() {
        return ((WeekTypeItem)Objects.requireNonNull(weekTypeComboBox.getSelectedItem())).fullWeek;
    }

    public void run() {
        window.setVisible(true);
    }

    static class YearItem {
        YearItem(String label, int year) {
            this.label = label;
            this.year = year;
        }

        @Override
        public String toString() {
            return label;
        }

        String label;
        int year;
    }

    static class SemesterItem {
        SemesterItem(String label, int semester) {
            this.label = label;
            this.semester = semester;
        }

        @Override
        public String toString() {
            return label;
        }

        String label;
        int semester;
    }

    static class WeekTypeItem {
        WeekTypeItem(String label, boolean fullWeek) {
            this.label = label;
            this.fullWeek = fullWeek;
        }

        @Override
        public String toString() {
            return label;
        }

        String label;
        boolean fullWeek;
    }

    TwoPanelsWindow window;
    JComboBox<YearItem> yearComboBox = new JComboBox<>();
    JComboBox<SemesterItem> semesterComboBox = new JComboBox<>();
    JComboBox<WeekTypeItem> weekTypeComboBox = new JComboBox<>();
    JTabbedPane tabbedPane = new JTabbedPane();
}
