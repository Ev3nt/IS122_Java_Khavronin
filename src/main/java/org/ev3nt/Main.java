package org.ev3nt;

import org.ev3nt.gui.UpdatingMessage;
import org.ev3nt.gui.Window;
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

        Window window = new Window("Расписание", 900, 600, 0.3, false);

        UpdatingMessage.wait(window.getWindow(), "Обновление данных...", () -> {
            window.add(GroupSchedule::new);
            window.add(TeacherSchedule::new);
            window.add(AudienceSchedule::new);
        });

        window.run();
    }
}