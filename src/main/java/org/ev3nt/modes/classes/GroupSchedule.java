package org.ev3nt.modes.classes;

import org.ev3nt.gui.interfaces.ComboBoxItem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GroupSchedule implements ComboBoxItem {
    @Override
    public String getName() {
        return "Расписание группы";
    }

    @Override
    public void showContent(JPanel contentPanel) {
        groupField = new JTextField(5);
        contentPanel.add(new JLabel("Группа:"));
        contentPanel.add(groupField);

        JButton button = new JButton("Проверить");
        contentPanel.add(button);

        button.addActionListener(new ButtonListener());
    }

    private class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String group = groupField.getText();
            String text;

            if (!group.isEmpty()) {
                text = "Введёная группа: " + group;
            } else {
                text = "Вы не ввели группу";
            }

            JOptionPane.showMessageDialog(null, text, "Сообщение", JOptionPane.INFORMATION_MESSAGE);

            // TODO: Request schedule, store, parse and format (first 3 points are ready and only require adaptation).
        }
    }

    JTextField groupField;
}
