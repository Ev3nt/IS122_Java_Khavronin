package org.ev3nt.modes;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.ev3nt.files.FavouriteManager;
import org.ev3nt.files.ResourceLoader;
import org.ev3nt.files.ZipCustomCopy;
import org.ev3nt.gui.ModalMessage;
import org.ev3nt.gui.Window;
import org.ev3nt.utils.StringUtils;
import org.ev3nt.web.WebGroups;
import org.ev3nt.web.WebSchedule;
import org.ev3nt.web.dto.LessonDTO;
import org.ev3nt.web.dto.ScheduleDTO;

import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.ev3nt.utils.StringUtils.appendIfNotNull;

public class AudienceSchedule implements ScheduleMode{
    @Override
    public void setParent(Window parent) {
        this.parent = parent;
    }

    @Override
    public String getName() {
        return "Аудитория";
    }

    @Override
    public JPanel getPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel audiencePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JButton favourite = new JButton("+");
        Dimension buttonSize = new Dimension(favourite.getPreferredSize().width, favourite.getPreferredSize().height);
        favourite.setPreferredSize(buttonSize);

        Dimension fieldSize = new Dimension(audienceField.getPreferredSize().width, buttonSize.height);
        audienceField.setPreferredSize(fieldSize);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        audiencePanel.add(new JLabel("Аудитория"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        audiencePanel.add(audienceField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        audiencePanel.add(favourite, gbc);

        audiencePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, audiencePanel.getPreferredSize().height));

        JPanel favouritePanel = new JPanel();
        favouritePanel.setLayout(new BoxLayout(favouritePanel, BoxLayout.Y_AXIS));

        JButton unFavourite = new JButton("–"); // "–" or "-"
        JPanel unFavouriteButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        unFavourite.setPreferredSize(buttonSize);
        unFavouriteButtonPanel.add(unFavourite);

        JPanel headerPanel = new JPanel(new BorderLayout());

        headerPanel.add(new JLabel("Избранные аудитории"), BorderLayout.WEST);
        headerPanel.add(unFavouriteButtonPanel, BorderLayout.EAST);

        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, headerPanel.getPreferredSize().height));

        DefaultListModel<String> model = new DefaultListModel<>();
        favouriteList = new JList<>(model);
        favouriteList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(favouriteList);

        favouritePanel.add(headerPanel);
        favouritePanel.add(scrollPane);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonPanel.getPreferredSize().height));

        JButton createSchedule = new JButton("Создать расписание");
        JButton flatSchedule = new JButton("Создать общее расписание");

        buttonPanel.add(createSchedule);
        buttonPanel.add(flatSchedule);

        panel.add(audiencePanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(favouritePanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(buttonPanel);

        favourite.addActionListener(e -> {
            String audience = audienceField.getText();

            if (audience != null && !audience.isEmpty()) {
                DefaultListModel<String> favouriteAudiences = (DefaultListModel<String>)favouriteList.getModel();

                if (!favouriteAudiences.contains(audience)) {
                    favouriteAudiences.addElement(audience);

                    FavouriteManager.saveFavourites(favouriteKey, favouriteAudiences);

                    favouriteList.repaint();
                }
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Аудитория не выбран!\nПожалуйста, введите номер аудитории.",
                        "Не удалось добавить аудиторию",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        unFavourite.addActionListener(e -> {
            List<String> audiences = favouriteList.getSelectedValuesList();

            if (!audiences.isEmpty()) {
                DefaultListModel<String> favouriteAudiences = (DefaultListModel<String>)favouriteList.getModel();

                for (String audience : audiences) {
                    favouriteAudiences.removeElement(audience);
                }

                FavouriteManager.saveFavourites(favouriteKey, favouriteAudiences);

                favouriteList.repaint();
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Не выбрано ни одной аудитории для удаления!\n" +
                                "Пожалуйста, выделите нужные аудитории в списке.",
                        "Не удалось удалить аудиторию",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        createSchedule.addActionListener(e -> createSchedule(false));

        flatSchedule.addActionListener(e -> createSchedule(true));

        InitFields();

        return panel;
    }

    void InitFields() {
        List<String> favouriteAudiences = FavouriteManager.loadFavourites(favouriteKey, String.class);

        DefaultListModel<String> model = (DefaultListModel<String>)favouriteList.getModel();

        if (!favouriteAudiences.isEmpty()) {
            for (String audience : favouriteAudiences) {
                model.addElement(audience);
            }

            favouriteList.repaint();
        }
    }

    void createSchedule(boolean isFlat) {
        ModalMessage.wait(parent.getWindow(), "Создание расписания", "Расписание создаётся...", () -> {
            List<String> selectedAudiences = favouriteList.getSelectedValuesList()
                    .stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            if (selectedAudiences.isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Ни одна аудитория не выбрана!\n" +
                                "Пожалуйста, выделите нужные аудитории в списке.",
                        "Не удалось создать расписание",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            try {
                process(selectedAudiences, parent.getSemester(), parent.getYear(), parent.getScheduleFormat(), isFlat);

                JOptionPane.showMessageDialog(
                        null,
                        "Расписание создано!",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException | TemplateException | RuntimeException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        ex.getMessage(),
                        "Не удалось создать расписание",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    String preparePlainText(LessonDTO lesson) {
        StringBuilder text = new StringBuilder();

        appendIfNotNull(text, lesson.getDiscipline());
        appendIfNotNull(text, lesson.getType());
        appendIfNotNull(text, lesson.getNumber_week() != null ? lesson.getNumber_week().replace("/", "-") : null);
        appendIfNotNull(text, lesson.getUnder_group());
        appendIfNotNull(text, lesson.getName());
        appendIfNotNull(text, lesson.getGroup_name());

        if (lesson.getType_week() != null && !"all".equals(lesson.getType_week())) {
            text.append(" (").append("even".equals(lesson.getType_week()) ? "чётная" : "нечётная").append(")");
        }

        return text.toString();
    }

    boolean isNotZaoch(LessonDTO lesson, String audience, String groupName) {
        return lesson.getAud().toLowerCase().contains(audience) && !(groupName.contains("з-") || lesson.isZaoch());
    }

    void collectAudienceLessons(ScheduleDTO schedule, String audience, Map<Integer, Map<Integer, List<LessonDTO>>> lessonsSchedule) {
        schedule.getDisciplines().forEach((rowNum, rowLessons) ->
                rowLessons.forEach((lessonNum, lessons) ->
                        lessons.stream()
                                .filter(lesson -> isNotZaoch(lesson, audience, schedule.getGroup().getName()))
                                .forEach(lesson -> {
                                    lesson.setGroup_name(schedule.getGroup().getName());
                                    lessonsSchedule
                                            .computeIfAbsent(rowNum, k -> new HashMap<>())
                                            .computeIfAbsent(lessonNum, k -> new ArrayList<>())
                                            .add(lesson);
                                })
                )
        );
    }

    String extractAudienceName(ScheduleDTO schedule, String audience) {
        return schedule.getDisciplines().values().stream()
                .flatMap(row -> row.values().stream())
                .flatMap(List::stream)
                .filter(lesson -> lesson.getAud().toLowerCase().contains(audience))
                .findFirst()
                .map(LessonDTO::getAud)
                .orElse("");
    }

    void createSchedule(Template template, List<String> audiences, int semester, int year, boolean isFlat)
            throws IOException, TemplateException {

        List<String> groups = WebGroups.getGroups().values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        List<ScheduleDTO> schedules = groups.stream()
                .map(group -> WebSchedule.getGroupSchedule(group, semester, year))
                .filter(schedule -> {
                    String status = schedule.getStatus();
                    if (status.equals("connection_error")) {
                        throw new RuntimeException(schedule.getMessage());
                    }
                    return status.equals("ok");
                })
                .collect(Collectors.toList());

        List<String> rowNames = Arrays.asList("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС");

        Map<String, Object> root = new HashMap<>();
        List<String> audiencesNames = new ArrayList<>();
        List<Map<String, Object>> audienceList = new ArrayList<>();
        List<Integer> days = new ArrayList<>();
        Map<Integer, List<Integer>> lessonsCounts = new HashMap<>();
        for (String audience : audiences) {
            Map<Integer, Map<Integer, List<LessonDTO>>> disciplines = new HashMap<>();
            String audienceName = "";

            for (ScheduleDTO schedule : schedules) {
                collectAudienceLessons(schedule, audience, disciplines);

                if (audienceName.isEmpty()) {
                    audienceName = extractAudienceName(schedule, audience);
                }
            }

            if (disciplines.isEmpty() || audienceName.isEmpty()) {
                throw new RuntimeException("Аудитория не была найдена ни в одном расписании!\nАудитория: " + audience);
            }

            audienceName = audienceName.substring(0, 1).toUpperCase()
                         + audienceName.substring(1);

            ScheduleDTO schedule = new ScheduleDTO();
            schedule.setDisciplines(disciplines);

            schedule.prepareDisciplines(this::preparePlainText);

            days.addAll(new ArrayList<>(disciplines.keySet()));
            disciplines.forEach((k, v) -> lessonsCounts
                    .computeIfAbsent(k, e -> new ArrayList<>())
                    .addAll(v.keySet()));

            Map<String, Object> audienceMap = new HashMap<>();
            audienceMap.put("title", audienceName);
            audienceMap.put("columns", schedule.getPairNumbers());
            audienceMap.put("schedule", schedule.getDisciplines());
            audienceMap.put("row_names", rowNames);

            audiencesNames.add(
                    audienceName
                    .replace("\\", "-")
                    .replace("/", "-"));

            audienceList.add(audienceMap);
        }

        days = days.stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        lessonsCounts.replaceAll((k, v) -> v.stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList()));

        root.put("days", days);
        root.put("lessonsCounts", lessonsCounts);
        root.put("rowNames", rowNames);
        root.put("schedules", audienceList);

        StringWriter writer = new StringWriter();
        template.process(root, writer);

        try {
            Files.createDirectory(Paths.get("schedules"));
        } catch (IOException ignored) { }

        if (!isFlat) {
            ZipCustomCopy zip = new ZipCustomCopy(
                    StringUtils.getFileNameByList("schedules", audiencesNames, ".docx"),
                    templatesPath.resolve("Template.docx").toString());

            zip.add("word/document.xml", writer.toString());

            zip.close();
        } else {
            FileOutputStream file = new FileOutputStream(StringUtils.getFileNameByList("schedules", audiencesNames, ".html"));
            file.write(writer.toString().getBytes());
            file.close();
        }
    }

    void process(List<String> audiences, int semester, int year, boolean combined, boolean isFlat)
            throws IOException, TemplateException, RuntimeException {

        if (!isFlat) {
            ResourceLoader.extract(templatesPath.resolve("document.xml").toString());
            ResourceLoader.extract(templatesPath.resolve("Template.docx").toString());
        } else {
            ResourceLoader.extract(templatesPath.resolve("template.html").toString());
        }

        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(templatesPath.toAbsolutePath().toFile());

        Template template = !isFlat ? cfg.getTemplate("document.xml") : cfg.getTemplate("template.html");

        if (!isFlat) {
            if (combined) {
                createSchedule(template, audiences, semester, year, false);
            } else {
                audiences.forEach(teacher -> {
                    try {
                        createSchedule(template, Collections.singletonList(teacher), semester, year, false);
                    } catch (IOException | TemplateException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } else {
            createSchedule(template, audiences, semester, year, true);
        }
    }

    JTextField audienceField = new JTextField();
    JList<String> favouriteList;

    String favouriteKey = "Audiences";

    Path templatesPath = Paths.get("templates");

    Window parent;
}
