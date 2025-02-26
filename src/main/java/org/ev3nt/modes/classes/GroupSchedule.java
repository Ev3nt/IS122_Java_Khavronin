package org.ev3nt.modes.classes;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.ev3nt.classes.CacheManager;
import org.ev3nt.classes.ResourceLoader;
import org.ev3nt.classes.ScheduleLoader;
import org.ev3nt.classes.ZipCustomCopy;
import org.ev3nt.gui.interfaces.ComboBoxItem;
import org.ev3nt.web.classes.HttpSchedule;
import org.ev3nt.web.classes.dto.LessonDTO;
import org.ev3nt.web.classes.dto.ScheduleDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class GroupSchedule implements ComboBoxItem {
    @Override
    public String getName() {
        return "Расписание группы";
    }

    @Override
    public void showContent(JPanel contentPanel) {
        JPanel gridPanel = new JPanel(new GridLayout(4, 2));

        groupField = new JTextField(5);
        gridPanel.add(new JLabel("Группа:"));
        gridPanel.add(groupField);

        semesterField = new JTextField(5);
        gridPanel.add(new JLabel("Семестр:"));
        gridPanel.add(semesterField);

        yearField = new JTextField(5);
        gridPanel.add(new JLabel("Год:"));
        gridPanel.add(yearField);

        showAllDaysCheck = new JCheckBox();
        gridPanel.add(new JLabel("Показать все дни:"));
        gridPanel.add(showAllDaysCheck);

        contentPanel.add(gridPanel);

        JButton button = new JButton("Составить");
        contentPanel.add(button);

        button.addActionListener(new ButtonListener());
    }

    private void process(String json) throws IOException, TemplateException, GroupException {
        boolean showAllDays = showAllDaysCheck.isSelected();

        ResourceLoader.extract(templatesPath.resolve("document.xml"));
        ResourceLoader.extract(templatesPath.resolve("Template.docx"));

        ObjectMapper mapper = new ObjectMapper();
        var mappedJson = mapper.readValue(json, Map.class);

        if (!mappedJson.get("status").equals("ok")) {
            throw new GroupException(mappedJson.get("message").toString());
        }

        ScheduleDTO schedule = mapper.readValue(json, ScheduleDTO.class);
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

        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(templatesPath.toFile());
        cfg.setAPIBuiltinEnabled(true);
        Template template = cfg.getTemplate("document.xml");

        List<String> daysOfWeek = List.of("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС");
        if (!showAllDays) {
            daysOfWeek = daysOfWeek.subList(0, maxDays);
        }

        Map<String, Object> root = new HashMap<>();
        root.put("group", schedule.getGroup());
        root.put("days", daysOfWeek);
        root.put("maxDays", maxDays);
        root.put("lessonsTable", lessonsSchedule);

        StringWriter writer = new StringWriter();
        template.process(root, writer);

        try {
            Files.createDirectory(Path.of("schedules"));
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }

        ZipCustomCopy zip = new ZipCustomCopy("schedules\\" + schedule.getGroup().getName() + ".docx", templatesPath.resolve("Template.docx").toString());

        zip.add("word\\document.xml", writer.toString());

        zip.close();
    }

    private static class GroupException extends Exception {
        public GroupException(String message) {
                super(message);
            }
    }

    private class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            String group = groupField.getText();
            int semester = 0;
            int year = 0;
            String message = null;

            if (group.isEmpty()) {
                message = "Вы не выбрали группу!";
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

                ScheduleLoader loader = new ScheduleLoader(HttpSchedule::new);
                String json = loader.getSchedule(group, semester, year);
                process(json);

                JOptionPane.showMessageDialog(null, "Расписание успешно составлено!", "Сообщение", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | TemplateException | GroupException e) {
                if (e.getClass() == GroupException.class) {
                    CacheManager.deleteLastCachedFile();
                }

                JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    JTextField groupField;
    JTextField semesterField;
    JTextField yearField;
    JCheckBox showAllDaysCheck;

    Path templatesPath = Path.of("templates\\GroupSchedule");
}
