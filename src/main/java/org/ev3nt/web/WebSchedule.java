package org.ev3nt.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.ev3nt.files.CacheManager;
import org.ev3nt.web.dto.LessonDTO;
import org.ev3nt.web.dto.ScheduleDTO;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WebSchedule {
    public enum ScheduleType {
        GROUP,
        TEACHER
    }

    static Integer obj2Int(Object object) {
        return Integer.parseInt(object.toString());
    }

    static Map<Integer, Map<Integer, List<LessonDTO>>> parseDisciplines(Map<?, ?> disciplines) {
        Map<Integer, Map<Integer, List<LessonDTO>>> schedule = new HashMap<>();

        boolean zaoch = disciplines.size() == 1;
        for (Map.Entry<?, ?> dayEntry : disciplines.entrySet()) {
            Integer day = obj2Int(dayEntry.getKey());
            Object dayObj = dayEntry.getValue();
            List<LessonDTO> lessons = extractLessons(dayObj);

            if (zaoch) {
                processZaochLessons(schedule, lessons);
            } else {
                processRegularLessons(schedule, day, lessons);
            }
        }

        return schedule;
    }

    static List<LessonDTO> extractLessons(Object dayObj) {
        List<?> legacyDayLessons = dayObj instanceof List
                ? (List<?>) dayObj
                :  new ArrayList<>(((Map<?, ?>) dayObj).values());

        return legacyDayLessons.stream()
                .flatMap(dayLessonsObj -> {
                    @SuppressWarnings("unchecked")
                    List<?> lessonsContainer = dayLessonsObj instanceof Map
                            ? ((Map<String, List<?>>) dayLessonsObj).values().stream()
                            .flatMap(List::stream).collect(Collectors.toList())
                            : (List<?>) dayLessonsObj;

                    return lessonsContainer.stream();
                })
                .map(lesson -> mapper.convertValue(lesson, LessonDTO.class))
                .collect(Collectors.toList());
    }

    static void processZaochLessons(Map<Integer, Map<Integer, List<LessonDTO>>> schedule, List<LessonDTO> lessons) {
        lessons.forEach(lesson -> {
            Integer week = Stream.of(lesson.getNumber_week(),
                            lesson.getUnder_group_1(),
                            lesson.getUnder_group_2())
                    .filter(s -> !s.isEmpty())
                    .findFirst()
                    .map(WebSchedule::obj2Int)
                    .orElse(null);

            Integer number = obj2Int(lesson.getNumber_para());

            schedule.computeIfAbsent(week, k -> new HashMap<>())
                    .computeIfAbsent(number, k -> new ArrayList<>())
                    .add(lesson);
        });
    }

    static void processRegularLessons(Map<Integer, Map<Integer, List<LessonDTO>>> schedule, int day,
                                      List<LessonDTO> lessons) {
        Map<Integer, List<LessonDTO>> dayLessons = new HashMap<>();

        lessons.forEach(lesson -> {
            Integer number = obj2Int(lesson.getNumber_para());

            dayLessons.computeIfAbsent(number, k -> new ArrayList<>())
                    .add(lesson);
        });

        schedule.put(day, dayLessons);
    }

    static void disciplinesActualizer(ScheduleDTO scheduleDTO, Map<?, ?> mappedJson) {
        Map<Integer, Map<Integer, List<LessonDTO>>> disciplines
                = parseDisciplines((Map<?, ?>) mappedJson.get("disciplines"));

        scheduleDTO.setDisciplines(disciplines);
    }

    static private ScheduleDTO fetchSchedule(String id, int semester, int year, ScheduleType scheduleType) {
        ScheduleDTO scheduleDTO = new ScheduleDTO();

        try {
            String url = "";
            switch (scheduleType) {
                case GROUP:
                    String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8.toString());
                    url = "https://scala.mivlgu.ru/core/frontend/index.php?r=schedulecash/group&group="
                            + encodedId + "&semester=" + semester + "&year=" + year + "&format=json";

                    break;
                case TEACHER:
                    url = "https://scala.mivlgu.ru/core/frontend/index.php?r=schedulecash/teacher&teacher_id="
                            + id + "&semester=" + semester + "&year=" + year + "&format=json";

                    break;
            }

            String json = WebHttp.request(url);

            Map<?, ?> mappedJson = mapper.readValue(json, Map.class);
            String status = (String) mappedJson.get("status");
            if (status.equals("ok")) {
                scheduleDTO = mapper.readValue(json, ScheduleDTO.class);

                disciplinesActualizer(scheduleDTO, mappedJson);
            } else {
                String message = mappedJson.get("message").toString();
                scheduleDTO.setStatus(status);
                scheduleDTO.setMessage(message);
            }
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
//            throw new RuntimeException(e);
        }

        return scheduleDTO;
    }

    static private String getCacheName(String id, int semester, int year) {
        return id + "_" + semester + "_" + year;
    }

    static private ScheduleDTO getCachedSchedule(String id, int semester, int year) throws JsonProcessingException {
        String cacheName = getCacheName(id, semester, year);
        String data = CacheManager.getCachedDataAsString(cacheName);

        ScheduleDTO scheduleDTO = mapper.readValue(data, ScheduleDTO.class);

        Map<?, ?> mappedJson = mapper.readValue(data, Map.class);
        if (mappedJson.get("status").equals("ok")) {
            disciplinesActualizer(scheduleDTO, mappedJson);
        }

        return scheduleDTO;
    }

    static private void cacheSchedule(String id, int semester, int year, ScheduleDTO schedule)
            throws JsonProcessingException {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String cacheName = getCacheName(id, semester, year);
        String data = mapper.writeValueAsString(schedule);
        CacheManager.saveDataAsCache(cacheName, data);
    }

    static public ScheduleDTO getSchedule(String id, int semester, int year, ScheduleType scheduleType) {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        ScheduleDTO scheduleDTO = fetchSchedule(id, semester, year, scheduleType);
        try {
            if (scheduleDTO.isEmpty()) {
                scheduleDTO = getCachedSchedule(id, semester, year);
            } else {
                cacheSchedule(id, semester, year, scheduleDTO);
            }
        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
        }

        return scheduleDTO;
    }

    static public ScheduleDTO getGroupSchedule(String name, int semester, int year) {
        return getSchedule(name, semester, year, ScheduleType.GROUP);
    }

    static public ScheduleDTO getTeacherSchedule(int id, int semester, int year) {
        return getSchedule(String.valueOf(id), semester, year, ScheduleType.TEACHER);
    }

    static ObjectMapper mapper = new ObjectMapper();
}
