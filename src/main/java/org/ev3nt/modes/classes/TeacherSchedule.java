package org.ev3nt.modes.classes;

import org.ev3nt.classes.TeacherLoader;
import org.ev3nt.gui.classes.PlaceholderTextField;
import org.ev3nt.gui.interfaces.ComboBoxItem;
import org.ev3nt.web.classes.HttpTeacher;
import org.ev3nt.web.classes.dto.TeacherDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class TeacherSchedule  implements ComboBoxItem {
    @Override
    public String getName() {
        return "Расписание преподавателя";
    }

    @Override
    public void showContent(JPanel contentPanel) {
        JPanel gridPanel = new JPanel(new GridLayout(4, 2));

        final Font f = gridPanel.getFont();

        fioField = new PlaceholderTextField(5);
        fioField.setPlaceholder("Иванов Иван Иванович");
        fioField.setFont(new Font(f.getName(), f.getStyle(), 18));
        JLabel label = new JLabel("ФИО преподавателя:");
        label.setFont(new Font(f.getName(), f.getStyle(), 18));
        gridPanel.add(label);
        gridPanel.add(fioField);

        semesterField = new PlaceholderTextField(5);
        semesterField.setPlaceholder("Пример: 2");
        semesterField.setFont(new Font(f.getName(), f.getStyle(), 18));
        label = new JLabel("Семестр:");
        label.setFont(new Font(f.getName(), f.getStyle(), 18));
        gridPanel.add(label);
        gridPanel.add(semesterField);

        yearField = new PlaceholderTextField(5);
        yearField.setPlaceholder("Пример: 2024");
        yearField.setFont(new Font(f.getName(), f.getStyle(), 18));
        label = new JLabel("Год:");
        label.setFont(new Font(f.getName(), f.getStyle(), 18));
        gridPanel.add(label);
        gridPanel.add(yearField);

        label = new JLabel("Преподаватель:");
        label.setFont(new Font(f.getName(), f.getStyle(), 18));
        teacherComboBox = new JComboBox<>();
        gridPanel.add(label);
        gridPanel.add(teacherComboBox);

        contentPanel.add(gridPanel);

        JPanel gridPanelButtons = new JPanel();

        JButton buttonFindTeachers = new JButton("Найти преподавателя");
        JButton button = new JButton("Составить");

        buttonFindTeachers.addActionListener(new ButtonListener(this::fetchTeachers));

        gridPanelButtons.add(buttonFindTeachers);
        gridPanelButtons.add(button);

        contentPanel.add(gridPanelButtons);
    }

    private void fetchTeachers() {
        String teacherFIO = fioField.getText();
        int semester = 0;
        int year = 0;
        String message = null;

        if (teacherFIO.isEmpty()) {
            message = "Вы не ввели ФИО преподавателя!";
        } else if (semesterField.getText().isEmpty()) {
            message = "Вы не уазали семестр!";
        } else if (yearField.getText().isEmpty()) {
            message = "Вы не указали год!";
        } else {
            try {
                semester = Integer.parseInt(semesterField.getText());
                year = Integer.parseInt(yearField.getText());
            } catch (NumberFormatException e) {
                message = "Неверный формат числа!";
            }
        }

        try {
            if (message != null) {
                throw new IOException(message);
            }

            TeacherLoader loader = new TeacherLoader(HttpTeacher::new);
            String xml = loader.getSchedule(teacherFIO, semester, year);

            Document doc = Jsoup.parse(xml);
            Elements teacherElements = doc.select("div.sch-teacher a");

            teachers.clear();
            teacherComboBox.removeAllItems();

            for (Element element : teacherElements) {
                String name = element.attr("data-teacher-name");

                TeacherDTO teacher = new TeacherDTO();
                teacher.setId(element.attr("data-teacher-id"));
                teacher.setName(name);
                teachers.add(teacher);

                teacherComboBox.addItem(name);
            }

            if (teachers.isEmpty()) {
                throw new IOException("Преподаватели не обнаружены!");
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    @FunctionalInterface
    interface Execute {
        void execute();
    }

    private static class ButtonListener implements ActionListener {

        public ButtonListener(Execute callback) {
            this.callback = callback;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.callback.execute();
        }

        private final Execute callback;
    }

    PlaceholderTextField fioField;
    PlaceholderTextField semesterField;
    PlaceholderTextField yearField;
    JComboBox<String> teacherComboBox;

    ArrayList<TeacherDTO> teachers = new ArrayList<>();
}
