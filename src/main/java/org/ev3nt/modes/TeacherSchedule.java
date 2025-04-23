package org.ev3nt.modes;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ev3nt.files.FavouriteManager;
import org.ev3nt.gui.Window;
import org.ev3nt.web.WebTeachers;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class TeacherSchedule implements ScheduleMode{
    public TeacherSchedule() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int semester = month > 8 ? 1 : 2;
        int year = now.getYear() - (semester - 1);

        teachers = WebTeachers.getTeachers(semester, year);
    }

    @Override
    public String getName() {
        return "Преподаватель";
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
        groupPanel.add(new JLabel("Имя преподавателя"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        groupPanel.add(teacherName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        groupPanel.add(new JLabel("Преподаватель"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        groupPanel.add(teacherComboBox, gbc);

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

        DefaultListModel<TeacherItem> model = new DefaultListModel<>();
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

        teacherName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }

            private void onChanged() {
                teacherComboBox.removeAllItems();
                String name = teacherName.getText().toLowerCase();

                teachers.entrySet().stream()
                        .filter(entry -> entry.getValue().toLowerCase().contains(name))
                        .map(entry -> new TeacherItem(entry.getValue(), entry.getKey()))
                        .forEach(teacherComboBox::addItem);
            }
        });

        favourite.addActionListener(e -> {
            TeacherItem teacher = (TeacherItem) teacherComboBox.getSelectedItem();

            if (teacher != null) {
                DefaultListModel<TeacherItem> favouriteTeachers = (DefaultListModel<TeacherItem>)favouriteList.getModel();

                if (!favouriteTeachers.contains(teacher)) {
                    favouriteTeachers.addElement(teacher);

                    FavouriteManager.saveFavourites(favouriteKey, favouriteTeachers);

                    favouriteList.repaint();
                }
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Группа не выбрана!\nПожалуйста, выберите группу в выпадающем списке.",
                        "Не удалось добавить группу",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        unFavourite.addActionListener(e -> {
            List<TeacherItem> teachers = favouriteList.getSelectedValuesList();

            if (!teachers.isEmpty()) {
                DefaultListModel<TeacherItem> favouriteGroups = (DefaultListModel<TeacherItem>)favouriteList.getModel();

                for (TeacherItem teacher : teachers) {
                    favouriteGroups.removeElement(teacher);
                }

                FavouriteManager.saveFavourites(favouriteKey, favouriteGroups);

                favouriteList.repaint();
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Не выбрано ни одной группы для удаления!\nПожалуйста, выделите нужные группы в списке.",
                        "Не удалось удалить группы",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        InitFields();

        return panel;
    }

    private void InitFields() {
        List<TeacherItem> favouriteTeachers = FavouriteManager.loadFavourites(favouriteKey, TeacherItem.class);

        DefaultListModel<TeacherItem> model = (DefaultListModel<TeacherItem>)favouriteList.getModel();

        if (!favouriteTeachers.isEmpty()) {
            for (TeacherItem teacher : favouriteTeachers) {
                model.addElement(teacher);
            }

            favouriteList.repaint();
        }
    }

    static class TeacherItem {
        public TeacherItem(String name, Integer teacherId) {
            this.name = name;
            this.teacherId = teacherId;
        }

        public TeacherItem() {}

        @Override
        public String toString() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setTeacherId(Integer teacherId) {
            this.teacherId = teacherId;
        }

        public Integer getTeacherId() {
            return teacherId;
        }

        @JsonProperty
        String name;

        @JsonProperty
        Integer teacherId;
    }

    static JTextField teacherName = new JTextField();
    static JComboBox<TeacherItem> teacherComboBox = new JComboBox<>();
    static JList<TeacherItem> favouriteList;
    static Map<Integer, String> teachers = new HashMap<>();

    String favouriteKey = "Teachers";
}
