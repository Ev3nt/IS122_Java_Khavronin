package org.ev3nt.classes;

import org.ev3nt.exceptions.ScheduleException;
import org.ev3nt.web.interfaces.WebSchedule;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class ScheduleLoader {
    public <T extends WebSchedule> ScheduleLoader(Supplier<T> factory) {
        webSchedule = factory.get();
    }

    public String getSchedule(String group, Integer semester, Integer year) {
        return getSchedule(group, semester, year, null);
    }

    public String getSchedule(String group, Integer semester, Integer year, String name) throws ScheduleException {
        String json = webSchedule.getSchedule(group, semester, year);
        Path cacheName = Paths.get(String.format("%s_%d_%d", name == null ? group : name, semester, year));

        if (json.isEmpty()) {
            json = CacheManager.getCachedDataAsString(cacheName);

            if (json.isEmpty()) {
                throw new ScheduleException("Не удалось получить расписание!");
            }
        } else {
            CacheManager.saveDataAsCache(cacheName, json);
        }

        return json;
    }

    private final WebSchedule webSchedule;
}
