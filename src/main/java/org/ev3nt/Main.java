package org.ev3nt;

import org.ev3nt.gui.MainWindow;
import org.ev3nt.modes.AudienceSchedule;
import org.ev3nt.modes.GroupSchedule;
import org.ev3nt.modes.TeacherSchedule;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        Font defaultFont = new Font("Arial", Font.PLAIN, 18);
        UIManager.put("Button.font", defaultFont);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("ComboBox.font", defaultFont);
        UIManager.put("TabbedPane.font", defaultFont);
        UIManager.put("List.font", defaultFont);

        MainWindow window = new MainWindow("Расписание", 800, 600, 0.3, true);
        window.run();

        window.add(GroupSchedule::new);
        window.add(TeacherSchedule::new);
        window.add(AudienceSchedule::new);
    }
}