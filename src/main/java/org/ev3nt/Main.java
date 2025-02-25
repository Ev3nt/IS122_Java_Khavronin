package org.ev3nt;

import org.ev3nt.gui.classes.Menu;
import org.ev3nt.modes.classes.AudienceSchedule;
import org.ev3nt.modes.classes.GroupSchedule;

public class Main {
    public static void main(String[] args) {
        Menu menu = new Menu("Test Window", 200, 200);
        menu.run();

        menu.add(GroupSchedule::new);
        menu.add(AudienceSchedule::new);
    }
}