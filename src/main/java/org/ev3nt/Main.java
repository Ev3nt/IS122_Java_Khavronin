package org.ev3nt;

import gui.classes.Menu;
import modes.classes.AudienceSchedule;
import modes.classes.GroupSchedule;

public class Main {
    public static void main(String[] args) {
        Menu menu = new Menu("Test Window", 200, 200);
        menu.run();

        menu.add(new GroupSchedule());
        menu.add(new AudienceSchedule());
    }
}