package org.ev3nt.classes;

import org.ev3nt.exceptions.ScheduleException;
import org.ev3nt.web.interfaces.WebSchedule;

import java.util.function.Supplier;

public class TeacherLoader {
    public <T extends WebSchedule> TeacherLoader(Supplier<T> factory) {
        webSchedule = factory.get();
    }

    public String getSchedule(String teacherFIO, Integer semester, Integer year) throws ScheduleException {
        return webSchedule.getSchedule(teacherFIO, semester, year);
    }

    private final WebSchedule webSchedule;
}
