package org.ev3nt.modes.classes;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.ev3nt.classes.*;
import org.ev3nt.gui.classes.PlaceholderTextField;
import org.ev3nt.gui.interfaces.ComboBoxItem;
import org.ev3nt.web.classes.HttpTeacher;
import org.ev3nt.web.classes.HttpTeacherSchedule;
import org.ev3nt.web.classes.dto.LessonDTO;
import org.ev3nt.web.classes.dto.ScheduleDTO;
import org.ev3nt.web.classes.dto.TeacherDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class TeacherSchedule  implements ComboBoxItem {
    @Override
    public String getName() {
        return "Расписание преподавателя";
    }

    @Override
    public void showContent(JPanel contentPanel) {
        JPanel gridPanel = new JPanel(new GridLayout(5, 2));

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

        showAllDaysCheck = new JCheckBox();
        label = new JLabel("Показать все дни:");
        label.setFont(new Font(f.getName(), f.getStyle(), 18));
        gridPanel.add(label);
        gridPanel.add(showAllDaysCheck);

        contentPanel.add(gridPanel);

        JPanel gridPanelButtons = new JPanel();

        JButton buttonFindTeachers = new JButton("Найти преподавателя");
        JButton button = new JButton("Составить");
        buttonFindTeachers.setFont(new Font(f.getName(), f.getStyle(), 18));
        button.setFont(new Font(f.getName(), f.getStyle(), 18));

        buttonFindTeachers.addActionListener(new ButtonListener(this::fetchTeachers));
        button.addActionListener(new ButtonListener(this::process));

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

    private static class TeacherException extends Exception {
        public TeacherException(String message) {
            super(message);
        }
    }

    private void process() {
        try {
            int semester = 0;
            int year = 0;
            String message = null;

            if (teachers.isEmpty()) {
                message = "Вы не выбрали преподавателя!";
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

            if (message != null) {
                throw new IOException(message);
            }

            boolean showAllDays = showAllDaysCheck.isSelected();

            ResourceLoader.extract(scheduleTemplatePath.resolve("document.xml"));
            ResourceLoader.extract(templatesPath.resolve("Template.docx"));

            TeacherDTO teacher = teachers.get(teacherComboBox.getSelectedIndex());
            String teacher_id = teacher.getId();

            ScheduleLoader loader = new ScheduleLoader(HttpTeacherSchedule::new);
            String json = loader.getSchedule(teacher_id, semester, year);

            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("rawtypes") Map mappedJson = mapper.readValue(json, Map.class);

            if (!mappedJson.get("status").equals("ok")) {
                throw new TeacherException(mappedJson.get("message").toString());
            }

            ScheduleDTO schedule = ScheduleParser.parse(json);
            Map<Integer, Map<Integer, Map<String, LessonDTO[]>>> disciplines = schedule.getDisciplines();

            int maxDays = 0;
            int maxLessons = 0;
            for (Map.Entry<Integer, Map<Integer, Map<String, LessonDTO[]>>> dayEntry : disciplines.entrySet()) {
                for (Map.Entry<Integer, Map<String, LessonDTO[]>> lessonEntry : dayEntry.getValue().entrySet()) {
                    maxLessons = Math.max(maxLessons, lessonEntry.getKey());
                }

                maxDays = Math.max(maxDays, dayEntry.getKey());
            }

            Map<Integer, Object> lessonsSchedule = new HashMap<>();

            for (int lessonNumber = 1; lessonNumber <= maxLessons; lessonNumber++) {
                Map<Integer, Object> rowLessons = new HashMap<>();

                for (Map.Entry<Integer, Map<Integer, Map<String, LessonDTO[]>>> dayLessons : disciplines.entrySet()) {
                    Map<String, LessonDTO[]> lessons =  dayLessons.getValue().get(lessonNumber);
                    if (lessons == null) {
                        continue;
                    }

                    ArrayList<LessonDTO> lessonsArray = new ArrayList<>();

                    for (Map.Entry<String, LessonDTO[]> lesson : lessons.entrySet()) {
                        Collections.addAll(lessonsArray, lesson.getValue());
                    }

                    rowLessons.put(dayLessons.getKey(), lessonsArray);
                }

                lessonsSchedule.put(lessonNumber, rowLessons);
            }

            //noinspection deprecation
            Configuration cfg = new Configuration();
            cfg.setDirectoryForTemplateLoading(scheduleTemplatePath.toAbsolutePath().toFile());
            cfg.setAPIBuiltinEnabled(true);
            Template template = cfg.getTemplate("document.xml");

            List<String> daysOfWeek = Arrays.asList("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС");
            if (!showAllDays) {
                daysOfWeek = daysOfWeek.subList(0, maxDays);
            }

            Map<String, Object> root = new HashMap<>();
            root.put("teacher", schedule.getTeacher());
            root.put("days", daysOfWeek);
            root.put("maxDays", maxDays);
            root.put("lessonsTable", lessonsSchedule);

            StringWriter writer = new StringWriter();
            template.process(root, writer);

            try {
                Files.createDirectory(Paths.get("schedules"));
            } catch (IOException e) {
//            throw new RuntimeException(e);
            }

            ZipCustomCopy zip = new ZipCustomCopy("schedules/" + schedule.getTeacher().getName() + ".docx", templatesPath.resolve("Template.docx").toString());

            zip.add("word/document.xml", writer.toString());

            zip.close();

            JOptionPane.showMessageDialog(null, "Расписание успешно составлено!", "Сообщение", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | TemplateException | TeacherException e) {
            if (e.getClass() == TeacherException.class) {
                CacheManager.deleteLastCachedFile();
            }

            JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    PlaceholderTextField fioField;
    PlaceholderTextField semesterField;
    PlaceholderTextField yearField;
    JComboBox<String> teacherComboBox;
    JCheckBox showAllDaysCheck;

    Path templatesPath = Paths.get("templates");
    Path scheduleTemplatePath = templatesPath.resolve("TeacherSchedule");

    ArrayList<TeacherDTO> teachers = new ArrayList<>();
}
