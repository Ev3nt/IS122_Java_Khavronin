package modes.classes;

import gui.interfaces.ComboBoxItem;

import javax.swing.*;

public class GroupSchedule implements ComboBoxItem {

    @Override
    public String getName() {
        return "Расписание группы";
    }

    @Override
    public void showContent(JPanel contentPanel) {
        contentPanel.add(new JLabel("Test field"));
    }
}
