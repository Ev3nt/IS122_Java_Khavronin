package org.ev3nt.modes.classes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.ev3nt.classes.*;
import org.ev3nt.exceptions.ScheduleException;
import org.ev3nt.gui.classes.PlaceholderTextField;
import org.ev3nt.gui.interfaces.ComboBoxItem;
import org.ev3nt.web.classes.HttpSchedule;
import org.ev3nt.web.classes.dto.LessonDTO;
import org.ev3nt.web.classes.dto.ScheduleDTO;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class GroupSchedule implements ComboBoxItem {
    @Override
    public String getName() {
        return "Расписание группы";
    }

    @Override
    public void showContent(JPanel contentPanel) {
        JPanel gridPanel = new JPanel(new GridLayout(4, 2));

        final Font f = gridPanel.getFont();
        JLabel label;

        groupField = new PlaceholderTextField(5);
        groupField.setPlaceholder("Пример: ИС-122");
        groupField.setFont(new Font(f.getName(), f.getStyle(), 18));
        label = new JLabel("Группа:");
        label.setFont(new Font(f.getName(), f.getStyle(), 18));
        gridPanel.add(label);
        gridPanel.add(groupField);

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

        showAllDaysCheck = new JCheckBox();
        label = new JLabel("Показать все дни:");
        label.setFont(new Font(f.getName(), f.getStyle(), 18));
        gridPanel.add(label);
        gridPanel.add(showAllDaysCheck);

        contentPanel.add(gridPanel);

        JButton button = new JButton("Составить");
        button.setFont(new Font(f.getName(), f.getStyle(), 18));
        contentPanel.add(button);

        button.addActionListener(new ButtonListener(this::fetch_group));
    }

    private void process(String json) throws IOException, TemplateException, GroupException {
        boolean showAllDays = showAllDaysCheck.isSelected();

        ResourceLoader.extract(scheduleTemplatePath.resolve("document.xml"));
        ResourceLoader.extract(templatesPath.resolve("Template.docx"));

        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("rawtypes") Map mappedJson = mapper.readValue(json, Map.class);

        if (!mappedJson.get("status").equals("ok")) {
            throw new GroupException(mappedJson.get("message").toString());
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
        root.put("group", schedule.getGroup());
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

        ZipCustomCopy zip = new ZipCustomCopy("schedules/" + schedule.getGroup().getName() + ".docx", templatesPath.resolve("Template.docx").toString());

        zip.add("word/document.xml", writer.toString());

        zip.close();
    }

    private static class GroupException extends Exception {
        public GroupException(String message) {
                super(message);
            }
    }

    private void fetch_group() {
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
        } catch (IOException | TemplateException | GroupException | ScheduleException e) {
            if (e.getClass() == GroupException.class) {
                CacheManager.deleteLastCachedFile();
            }

            JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    PlaceholderTextField groupField;
    PlaceholderTextField semesterField;
    PlaceholderTextField yearField;
    JCheckBox showAllDaysCheck;

    Path templatesPath = Paths.get("templates");
    Path scheduleTemplatePath = templatesPath.resolve("GroupSchedule");
}
