package org.ev3nt.modes.classes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.ev3nt.classes.*;
import org.ev3nt.gui.classes.PlaceholderTextField;
import org.ev3nt.gui.interfaces.ComboBoxItem;
import org.ev3nt.web.classes.HttpFaculty;
import org.ev3nt.web.classes.HttpSchedule;
import org.ev3nt.web.classes.dto.LessonDTO;
import org.ev3nt.web.classes.dto.ScheduleDTO;
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

public class AudienceSchedule implements ComboBoxItem {
    @Override
    public String getName() {
        return "Расписание аудитории";
    }

    @Override
    public void showContent(JPanel contentPanel) {
        JPanel gridPanel = new JPanel(new GridLayout(4, 2));

        final Font f = gridPanel.getFont();
        JLabel label;

        audienceField = new PlaceholderTextField(5);
        audienceField.setPlaceholder("Пример: 401/2");
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

    private List<ScheduleDTO> prepareGroups(List<String> groups, int year, int semester) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<ScheduleDTO> schedules = new ArrayList<>();

        for (String group : groups) {
            ScheduleLoader loader = new ScheduleLoader(HttpSchedule::new);
            String json = loader.getSchedule(group, semester, year);

            @SuppressWarnings("rawtypes") Map mappedJson = mapper.readValue(json, Map.class);

            if (!mappedJson.get("status").equals("ok")) {
                CacheManager.deleteLastCachedFile();

                continue;

//                throw new AudienceSchedule.GroupException(mappedJson.get("message").toString());
            }

            ScheduleDTO schedule = ScheduleParser.parse(json);

            schedules.add(schedule);
        }

        return schedules;
    }

    private void process() {
        List<String> groups = getGroups();

        String audience = audienceField.getText().toLowerCase();
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

            boolean showAllDays = showAllDaysCheck.isSelected();

            ResourceLoader.extract(scheduleTemplatePath.resolve("document.xml"));
            ResourceLoader.extract(templatesPath.resolve("Template.docx"));

            List<ScheduleDTO> schedules = prepareGroups(groups, year, semester);

            Map<Integer, Map<Integer, List<LessonDTO>>> lessonsSchedule = new HashMap<>();

            int maxDays = 0;
            String audienceFull = null;
            for (ScheduleDTO schedule : schedules) {
                Map<Integer, Map<Integer, List<LessonDTO>>> disciplines = schedule.getRawMappedDisciplines();
                String groupName = schedule.getGroup().getName();

                for (Map.Entry<Integer, Map<Integer, List<LessonDTO>>> rowLessonsEntry : disciplines.entrySet()) {
                    Map<Integer, List<LessonDTO>> rowLessons = rowLessonsEntry.getValue();

                    for (Map.Entry<Integer, List<LessonDTO>> lessonsEntry : rowLessons.entrySet()) {
                        List<LessonDTO> lessons = lessonsEntry.getValue();
                        for (LessonDTO lesson : lessons) {
                            if (lesson.getAud().toLowerCase().contains(audience) && !(groupName.contains("з") || lesson.isZaoch())) {
                                Map<Integer, List<LessonDTO>> combinedRowLessons = lessonsSchedule.computeIfAbsent(rowLessonsEntry.getKey(), k -> new HashMap<>());
                                List<LessonDTO> combinedLessons = combinedRowLessons.computeIfAbsent(lessonsEntry.getKey(), k -> new ArrayList<>());

                                lesson.setGroup_name(groupName);
                                maxDays = Math.max(Integer.parseInt(lesson.getId_day()), maxDays);

                                combinedLessons.add(lesson);

                                if (audienceFull == null) {
                                    audienceFull = lesson.getAud();
                                }
                            }
                        }
                    }
                }
            }

            if (audienceFull == null) {
                throw new RuntimeException("Аудитория не была найдена ни в одном расписании!");
            }

            audienceFull = audienceFull.substring(0, 1).toUpperCase() + audienceFull.substring(1);

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
            root.put("audience", audienceFull);
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

            audienceFull = audienceFull.replace("/", "-").replace("\\", "-");

            ZipCustomCopy zip = new ZipCustomCopy("schedules/" + audienceFull + ".docx", templatesPath.resolve("Template.docx").toString());

            zip.add("word/document.xml", writer.toString());

            zip.close();

            JOptionPane.showMessageDialog(null, "Расписание успешно составлено!", "Сообщение", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | RuntimeException | TemplateException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String> fetchGroups() {
        List<Integer> faculties = new ArrayList<>();
        Set<String> groups = new HashSet<>();

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

        return new ArrayList<>(groups);
    }

    private List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

//        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
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

    PlaceholderTextField audienceField;
    PlaceholderTextField semesterField;
    PlaceholderTextField yearField;
    JCheckBox showAllDaysCheck;

    Path templatesPath = Paths.get("templates");
    Path scheduleTemplatePath = templatesPath.resolve("AudienceSchedule");
}
