package org.ev3nt.legacy.classes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ev3nt.legacy.web.classes.dto.LessonDTO;
import org.ev3nt.legacy.web.classes.dto.ScheduleDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleParser {
    static public ScheduleDTO parse(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<?, ?> mappedJson = mapper.readValue(json, Map.class);

        ScheduleDTO schedule = mapper.readValue(json, ScheduleDTO.class);

        Map<Integer, Map<Integer, Map<String, LessonDTO[]>>> scheduleDisciplines = new HashMap<>();

        Map<?, ?> mappedDisciplines = (Map<?, ?>)mappedJson.get("disciplines");
        mappedDisciplines.forEach((day, daySchedule) -> {
            Map<Integer, Map<String, LessonDTO[]>> dayScheduleMap = new HashMap<>();

            if (daySchedule instanceof Map) {
                ((Map<?, ?>) daySchedule).forEach((key, value) -> {
                    int lessonNumber = Integer.parseInt(key.toString());
                    Map<String, LessonDTO[]> lessonsMap = parseLessons(mapper, value);

                    dayScheduleMap.put(lessonNumber, lessonsMap);
                });
            } else if (daySchedule instanceof List) {
                int index = 0;
                for (Object value : ((List<?>) daySchedule)) {
                    int lessonNumber = ++index;
                    Map<String, LessonDTO[]> lessonsMap = parseLessons(mapper, value);

                    dayScheduleMap.put(lessonNumber, lessonsMap);
                }
            }

            scheduleDisciplines.put(Integer.valueOf((String)day), dayScheduleMap);
        });

        schedule.setDisciplines(scheduleDisciplines);

        return schedule;
    }

    static private Map<String, LessonDTO[]> parseLessons(ObjectMapper mapper, Object lessons) {
        Map<String, LessonDTO[]> lessonsMap = new HashMap<>();

        if (lessons instanceof Map) {
            ((Map<?, ?>) lessons).forEach((subjectObj, lessonsObj) -> {
                String subject = subjectObj.toString();
                LessonDTO[] lessonsArray = ((List<?>)lessonsObj).stream()
                        .map(lessonMap -> mapper.convertValue(lessonMap, LessonDTO.class))
                        .toArray(LessonDTO[]::new);
                lessonsMap.put(subject, lessonsArray);
            });
        }

        return lessonsMap;
    }
}
