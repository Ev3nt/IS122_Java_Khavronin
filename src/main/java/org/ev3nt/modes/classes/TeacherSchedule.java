package org.ev3nt.modes.classes;

import javafx.scene.control.ComboBox;
import org.ev3nt.gui.classes.PlaceholderTextField;
import org.ev3nt.gui.interfaces.ComboBoxItem;

import javax.swing.*;
import java.awt.*;

public class TeacherSchedule  implements ComboBoxItem {
    @Override
    public String getName() {
        return "Расписание преподавателя";
    }

    @Override
    public void showContent(JPanel contentPanel) {
        JPanel gridPanel = new JPanel(new GridLayout(2, 2));

        final Font f = gridPanel.getFont();

        fioField = new PlaceholderTextField(5);
        fioField.setPlaceholder("Иванов Иван Иванович");
        fioField.setFont(new Font(f.getName(), f.getStyle(), 18));
        JLabel label = new JLabel("ФИО преподавателя:");
        label.setFont(new Font(f.getName(), f.getStyle(), 18));
        gridPanel.add(label);
        gridPanel.add(fioField);

        label = new JLabel("Преподаватель:");
        label.setFont(new Font(f.getName(), f.getStyle(), 18));
        teacherComboBox = new JComboBox<>();
        gridPanel.add(label);
        gridPanel.add(teacherComboBox);

        contentPanel.add(gridPanel);

        JButton button = new JButton("Составить");
        contentPanel.add(button);
    }

    private PlaceholderTextField fioField;
    private JComboBox<String> teacherComboBox;
}
