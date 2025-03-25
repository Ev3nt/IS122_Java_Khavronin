package org.ev3nt;

import org.ev3nt.gui.classes.Menu;
import org.ev3nt.modes.classes.GroupSchedule;
import org.ev3nt.modes.classes.TeacherSchedule;

import javax.swing.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

public class Main {
    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");

        try {
            Field charsetField = Charset.class.getDeclaredField("defaultCharset");
            charsetField.setAccessible(true);
            charsetField.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            JOptionPane.showMessageDialog(null, "Не удалось установить кодировку UTF-8, пожалуйста, установите её вручную!", "Сообщение", JOptionPane.WARNING_MESSAGE);
        }


        Menu menu = new Menu("Расписание МИВлГУ", 430, 300);
        menu.run();

        menu.add(GroupSchedule::new);
//        menu.add(AudienceSchedule::new);
        menu.add(TeacherSchedule::new);
    }
}