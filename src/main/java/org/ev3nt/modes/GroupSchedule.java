package org.ev3nt.modes;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.ev3nt.files.FavouriteManager;
import org.ev3nt.files.ResourceLoader;
import org.ev3nt.files.ZipCustomCopy;
import org.ev3nt.gui.Window;
import org.ev3nt.web.WebGroups;
import org.ev3nt.web.WebSchedule;
import org.ev3nt.web.dto.LessonDTO;
import org.ev3nt.web.dto.ScheduleDTO;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;

public class GroupSchedule implements ScheduleMode{
    @Override
    public String getName() {
        return "Группа";
    }

    @Override
    public JPanel getPanel(Window parent) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel groupPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);


        JButton favourite = new JButton("+");
        Dimension buttonSize = new Dimension(favourite.getPreferredSize().width, favourite.getPreferredSize().height);
        favourite.setPreferredSize(buttonSize);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        groupPanel.add(new JLabel("Факультет"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        groupPanel.add(facultyComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        groupPanel.add(new JLabel("Группа"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        groupPanel.add(groupComboBox, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        groupPanel.add(favourite, gbc);

        groupPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, groupPanel.getPreferredSize().height));

        JPanel favouritePanel = new JPanel();
        favouritePanel.setLayout(new BoxLayout(favouritePanel, BoxLayout.Y_AXIS));

        JButton unFavourite = new JButton("–"); // "–" or "-"
        JPanel unFavouriteButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        unFavourite.setPreferredSize(buttonSize);
        unFavouriteButtonPanel.add(unFavourite);

        JPanel headerPanel = new JPanel(new BorderLayout());

        headerPanel.add(new JLabel("Избранные группы"), BorderLayout.WEST);
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

//        JButton singleSchedule = new JButton("Индивидуальное расписание");
//        JButton multiSchedule = new JButton("Общее расписание");
        JButton createSchedule = new JButton("Создать расписание");

//        buttonPanel.add(singleSchedule);
//        buttonPanel.add(multiSchedule);
        buttonPanel.add(createSchedule);

        panel.add(groupPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(favouritePanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(buttonPanel);

        facultyComboBox.addActionListener(e -> {
            groupComboBox.removeAllItems();

            List<String> groups = ((FacultyItem) Objects.requireNonNull(facultyComboBox.getSelectedItem())).groups;

            for (String group : groups) {
                groupComboBox.addItem(group);
            }
        });

        favourite.addActionListener(e -> {
            String group = (String)groupComboBox.getSelectedItem();

            if (group != null) {
                DefaultListModel<String> favouriteGroups = (DefaultListModel<String>)favouriteList.getModel();

                if (!favouriteGroups.contains(group)) {
                    favouriteGroups.addElement(group);

                    FavouriteManager.saveFavourites(favouriteKey, favouriteGroups);

                    favouriteList.repaint();
                }
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Группа не выбрана!",
                        "Не удалось добавить группу",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        unFavourite.addActionListener(e -> {
            List<String> groups = favouriteList.getSelectedValuesList();

            if (!groups.isEmpty()) {
                DefaultListModel<String> favouriteGroups = (DefaultListModel<String>)favouriteList.getModel();

                for (String group : groups) {
                    favouriteGroups.removeElement(group);
                }

                FavouriteManager.saveFavourites(favouriteKey, favouriteGroups);

                favouriteList.repaint();
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Не выбрано ни одной группы для удаления.\nПожалуйста, выделите нужные группы в списке.",
                        "Не удалось удалить группы",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        createSchedule.addActionListener(e -> {
            List<String> selectedGroups = favouriteList.getSelectedValuesList();

            if (selectedGroups.isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Ни одна группа не выбрана!",
                        "Не удалось создать расписание",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            try {
                process(selectedGroups, parent.getSemester(), parent.getYear());

                JOptionPane.showMessageDialog(
                        null,
                        "Расписание создано!",
                        "Успех",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException | TemplateException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        ex.getMessage(),
                        "Не удалось создать расписание",
                        JOptionPane.ERROR_MESSAGE
                );
//                throw new RuntimeException(ex);
            }
//            ScheduleDTO group = WebSchedule.getGroupSchedule("ИСз-124", parent.getSemester(), parent.getYear());
//            System.out.println(group.getDisciplines());
//
//            Map<String, List<String>> groups = WebGroups.getGroups();
//
//            for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
//                for (String groupName : entry.getValue()) {
//                    group = WebSchedule.getGroupSchedule(groupName, parent.getSemester(), parent.getYear());
//                }
//            }
//
//            group = WebSchedule.getTeacherSchedule(146, parent.getSemester(), parent.getYear());
//            System.out.println(group.getDisciplines());
        });

        InitFields();

        return panel;
    }

    private void InitFields() {
        Map<String, List<String>> groups = WebGroups.getGroups();
        List<String> favouriteGroups = FavouriteManager.loadFavourites(favouriteKey);

        for (String faculty : groups.keySet()) {
            facultyComboBox.addItem(new FacultyItem(faculty.substring(0, 1).toUpperCase() + faculty.substring(1), groups.get(faculty)));
        }

        DefaultListModel<String> model = (DefaultListModel<String>)favouriteList.getModel();

        if (!favouriteGroups.isEmpty()) {
            for (String group : favouriteGroups) {
                model.addElement(group);
            }

            favouriteList.repaint();
        }
    }

    static class FacultyItem {
        FacultyItem(String label, List<String> groups) {
            this.label = label;
            this.groups = groups;
        }

        @Override
        public String toString() {
            return label;
        }

        String label;
        List<String> groups;
    }

    private void appendIfNotNull(StringBuilder builder, String value) {
        if (value != null && !value.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" ");
            }

            builder.append(value.trim());
        }
    }

    String preparePlainText(LessonDTO lesson) {
        StringBuilder text = new StringBuilder();

        appendIfNotNull(text, lesson.getDiscipline());
        appendIfNotNull(text, lesson.getType());
        appendIfNotNull(text, lesson.getNumber_week() != null ? lesson.getNumber_week().replace("/", "-") : null);
        appendIfNotNull(text, lesson.getUnder_group());
        appendIfNotNull(text, lesson.getName());
        appendIfNotNull(text, lesson.getAud());

        if (lesson.getType_week() != null && !"all".equals(lesson.getType_week())) {
            text.append(" (").append("even".equals(lesson.getType_week()) ? "чётная" : "нечётная").append(")");
        }

        return text.toString();
    }

    void process(List<String> groups, int semester, int year) throws IOException, TemplateException {
        ResourceLoader.extract(templatesPath.resolve("document.xml").toString());
        ResourceLoader.extract(templatesPath.resolve("Template.docx").toString());

        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(templatesPath.toAbsolutePath().toFile());

        Template template = cfg.getTemplate("document.xml");

        Map<String, Object> root = new HashMap<>();
        List<Map<String, Object>> groupList = new ArrayList<>();
        for (String groupName : groups) {
            ScheduleDTO schedule = WebSchedule.getGroupSchedule(groupName, semester, year);

            if (!schedule.getStatus().equals("ok")) {
                throw new IOException(schedule.getMessage() + "\nГруппа: " + groupName);
            }

            Map<Integer, Map<Integer, List<LessonDTO>>> disciplines = schedule.getDisciplines();
            schedule.getDisciplines().values().stream()
                    .flatMap(semesterMap -> semesterMap.values().stream())
                    .flatMap(List::stream)
                    .forEach(lesson -> lesson.setPlainText(
                            preparePlainText(lesson)
                    ));

            List<String> rowNames;
            if (groupName.contains("з-")) {
                boolean isSpring = semester == 2;
                int month = isSpring ? 2 : 9;
                int day = isSpring ? 8 : 1;
                LocalDate saturday = LocalDate.of(isSpring ? year + 1 : year, month, day)
                        .with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

                rowNames = new ArrayList<>();
                for (int i = 0; i < 18; i++) {
                    rowNames.add(saturday.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + " (неделя " + (i + 1) + ")");

                    saturday = saturday.plusWeeks(1);
                }
            } else {
                rowNames = Arrays.asList("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС");
            }

            Map<String, Object> group = new HashMap<>();
            group.put("title", schedule.getGroup().getName());
            group.put("first_lesson_number", schedule.getMinLessonNumber());
            group.put("last_lesson_number", schedule.getMaxLessonNumber());
            group.put("schedule", disciplines);
            group.put("row_names", rowNames);

            groupList.add(group);
        }

        root.put("schedules", groupList);

        StringWriter writer = new StringWriter();
        template.process(root, writer);

        try {
            Files.createDirectory(Paths.get("schedules"));
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }

        ZipCustomCopy zip = new ZipCustomCopy(
                "schedules/" + String.join(", ", groups) + ".docx",
                templatesPath.resolve("Template.docx").toString());

        zip.add("word/document.xml", writer.toString());

        zip.close();
    }

    JComboBox<FacultyItem> facultyComboBox = new JComboBox<>();
    JComboBox<String> groupComboBox = new JComboBox<>();
    JList<String> favouriteList;

    String favouriteKey = "Groups";

    Path templatesPath = Paths.get("templates");
}
