package org.ev3nt.gui;

import org.ev3nt.files.ResourceLoader;
import org.ev3nt.modes.ScheduleMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.List;
import java.util.stream.Stream;

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

        ScheduleFormatComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        ScheduleFormatComboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, ScheduleFormatComboBox.getPreferredSize().height));

        updateData.setText("Обновить данные");
        updateData.setMaximumSize(new Dimension(Short.MAX_VALUE, updateData.getPreferredSize().height));

        leftPanel.add(new JLabel("Учебный год"));
        leftPanel.add(yearComboBox);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(new JLabel("Семестр"));
        leftPanel.add(semesterComboBox);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(new JLabel("Формат расписания"));
        leftPanel.add(ScheduleFormatComboBox);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(updateData);

        leftPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,10));

        JPanel rightPanel = window.getRightPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        rightPanel.add(tabbedPane);

        rightPanel.setBorder(BorderFactory.createEmptyBorder(10,10,15,15));

        InitFields();
    }

    void InitFields() {
        for (int i = Year.now().getValue(); i > 2011; i--) {
            yearComboBox.addItem(new YearItem(i - 1 + " - " + i, i - 1));
        }

        semesterComboBox.addItem(new SemesterItem("Весенний", 2));
        semesterComboBox.addItem(new SemesterItem("Осенний", 1));

        ScheduleFormatComboBox.addItem(new ScheduleFormat("Один файл", true));
        ScheduleFormatComboBox.addItem(new ScheduleFormat("Отдельные файлы", false));

        updateData.addActionListener(e -> {
            try (Stream<Path> pathStream = Files.walk(Paths.get("cache"))) {
                pathStream
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException ex) {
                                System.err.println("Ошибка при удалении: " + path);
                            }
                        });
            } catch (IOException ex) {
                System.err.println("Ошибка при обходе директории: " + ex.getMessage());
            }

            for (ActionListener listener : updateCallbacks) {
                listener.actionPerformed(e);
            }
        });
    }

    public <T extends ScheduleMode> void add(Supplier<T> itemFactory) {
        T item = itemFactory.get();

        item.setParent(this);

        tabbedPane.addTab(item.getName(), item.getPanel());
    }

    public int getYear() {
        return ((YearItem) Objects.requireNonNull(yearComboBox.getSelectedItem())).year;
    }

    public int getSemester() {
        return ((SemesterItem) Objects.requireNonNull(semesterComboBox.getSelectedItem())).semester;
    }
    
    public boolean getScheduleFormat() {
        return ((ScheduleFormat)Objects.requireNonNull(ScheduleFormatComboBox.getSelectedItem())).combined;
    }

    public void addUpdateDataCallback(ActionListener listener) {
        if (!updateCallbacks.contains(listener)) {
            updateCallbacks.add(listener);
        }
    }

    public void run() {
        window.setVisible(true);
    }

    public JFrame getWindow() {
        return window;
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

    static class ScheduleFormat {
        ScheduleFormat(String label, boolean combined) {
            this.label = label;
            this.combined = combined;
        }

        @Override
        public String toString() {
            return label;
        }

        String label;
        boolean combined;
    }

    TwoPanelsWindow window;
    JComboBox<YearItem> yearComboBox = new JComboBox<>();
    JComboBox<SemesterItem> semesterComboBox = new JComboBox<>();
    JComboBox<ScheduleFormat> ScheduleFormatComboBox = new JComboBox<>();
    JButton updateData = new JButton();
    JTabbedPane tabbedPane = new JTabbedPane();

    List<ActionListener> updateCallbacks = new ArrayList<>();
}
