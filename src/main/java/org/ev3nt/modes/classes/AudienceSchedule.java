package org.ev3nt.modes.classes;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.TemplateException;
import org.ev3nt.classes.ButtonListener;
import org.ev3nt.classes.CacheManager;
import org.ev3nt.classes.ScheduleLoader;
import org.ev3nt.exceptions.ScheduleException;
import org.ev3nt.gui.classes.PlaceholderTextField;
import org.ev3nt.gui.interfaces.ComboBoxItem;
import org.ev3nt.web.classes.HttpFaculty;
import org.ev3nt.web.classes.HttpSchedule;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class AudienceSchedule implements ComboBoxItem {
    @Override
    public String getName() {
        return "Расписание аудитории";
    }

    @Override
    public void showContent(JPanel contentPanel) {
        List<String> groups = getGroups();

        JPanel gridPanel = new JPanel(new GridLayout(4, 2));

        final Font f = gridPanel.getFont();
        JLabel label;

        audienceField = new PlaceholderTextField(5);
        audienceField.setPlaceholder("Пример: 401");
        audienceField.setFont(new Font(f.getName(), f.getStyle(), 18));
        label = new JLabel("Аудитория:");
        label.setFont(new Font(f.getName(), f.getStyle(), 18));

        gridPanel.add(label);
        gridPanel.add(audienceField);

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

        button.addActionListener(new ButtonListener(this::process));
    }

    private List<Map<Integer, Object>> prepareGroups(List<String> groups, int year, int semester)  {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<Integer, Object>> schedules = new ArrayList<>();

        for (String group : groups) {
            ScheduleLoader loader = new ScheduleLoader(HttpSchedule::new);
            String json = loader.getSchedule(group, semester, year);

            @SuppressWarnings("rawtypes") Map mappedJson = null;
            try {
                mappedJson = mapper.readValue(json, Map.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if (!mappedJson.get("status").equals("ok")) {
                CacheManager.deleteLastCachedFile();

                continue;

//                throw new AudienceSchedule.GroupException(mappedJson.get("message").toString());
            }

            ScheduleDTO schedule = null;
            try {
                schedule = mapper.readValue(json, ScheduleDTO.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
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
                    Map<String, LessonDTO[]> lessons = dayLessons.getValue().get(lessonNumber);
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

            schedules.add(lessonsSchedule);
        }

        return schedules;
    }

    private void process() {
        List<String> groups = getGroups();

        String audience = audienceField.getText();
        int semester = 0;
        int year = 0;
        String message = null;

        if (audience.isEmpty()) {
            message = "Вы не выбрали аудиторию!";
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

            List<Map<Integer, Object>> schedules = prepareGroups(groups, year, semester);

            JOptionPane.showMessageDialog(null, "Расписание успешно составлено!", "Сообщение", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | RuntimeException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String> fetchGroups() {
        List<Integer> faculties = new ArrayList<>();
        List<String> groups = new ArrayList<>();

        try {
            {
                String xml = HttpFaculty.getFaculties();

                Document doc = Jsoup.parse(xml);
                Element select = doc.selectFirst("select#searchschedule-faculty");

                if (select == null) {
                    throw new RuntimeException("Не удалось получить список факультетов!");
                }

                Elements options = select.select("option");

                for (Element option : options) {
                    String value = option.attr("value");
                    if (value.isEmpty()) {
                        continue;
                    }

                    faculties.add(Integer.valueOf(value));
                }
            }

            for (int faculty : faculties) {
                String xml = HttpFaculty.getGroups(faculty);

                Document doc = Jsoup.parse(xml);

                Elements checkboxDivs = doc.select("div.custom-control.custom-checkbox");

                for (Element div : checkboxDivs) {
//                    Element input = div.selectFirst("input.custom-control-input");
//                    if (input == null) {
//                        continue;
//                    }
//
//                    String value = input.attr("value");

                    Element label = div.selectFirst("label.custom-control-label");
                    if (label == null) {
                        continue;
                    }

                    String groupName = label.text();
                    groups.add(groupName);
                }
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return groups;
    }

    private List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String cacheFileName = "groups"; // formatter.format(new Date());
        Path cachePath = Paths.get(cacheFileName);
        String cachedData = CacheManager.getCachedDataAsString(cachePath);

        if (cachedData.isEmpty()) {
            groups = fetchGroups();

            try {
                cachedData = mapper.writeValueAsString(groups);

                CacheManager.saveDataAsCache(cachePath, cachedData);
            } catch (JsonProcessingException ignored) {

            }
        } else {
            try {
                groups = mapper.readValue(cachedData, new TypeReference<List<String>>() {});
            } catch (JsonProcessingException ignored) {

            }
        }

        return groups;
    }

    private static class GroupException extends Exception {
        public GroupException(String message) {
            super(message);
        }
    }

    PlaceholderTextField audienceField;
    PlaceholderTextField semesterField;
    PlaceholderTextField yearField;
    JCheckBox showAllDaysCheck;

    Path templatesPath = Paths.get("templates");
    Path scheduleTemplatePath = templatesPath.resolve("GroupSchedule");
}
