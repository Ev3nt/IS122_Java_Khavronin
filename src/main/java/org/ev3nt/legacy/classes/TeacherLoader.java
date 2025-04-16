package org.ev3nt.legacy.classes;

import org.ev3nt.legacy.exceptions.ScheduleException;
import org.ev3nt.legacy.web.interfaces.WebSchedule;

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
