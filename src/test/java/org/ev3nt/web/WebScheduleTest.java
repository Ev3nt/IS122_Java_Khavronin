package org.ev3nt.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ev3nt.web.dto.LessonDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WebScheduleTest {
    @BeforeEach
    void setUp() {
        originalMapper = WebSchedule.mapper;
        WebSchedule.mapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        WebSchedule.mapper = originalMapper;
    }

    @Test
    void parseDisciplines_ShouldHandleRegularLessons() {
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> day1 = new HashMap<>();
        List<Map<String, Object>> lessons = new ArrayList<>();

        Map<String, Object> lesson1 = new HashMap<>();
        lesson1.put("number_para", "1");
        lesson1.put("discipline", "Математика");
        lessons.add(lesson1);

        day1.put("1", lessons);
        input.put("1", day1);

        Map<Integer, Map<Integer, List<LessonDTO>>> result = WebSchedule.parseDisciplines(input);

        assertEquals(1, result.size());
        assertEquals(1, result.get(1).size());
        assertEquals("Математика", result.get(1).get(1).get(0).getDiscipline());
    }

    @Test
    void parseDisciplines_ShouldHandleZaochLessons() {
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> day1 = new HashMap<>();
        List<Map<String, Object>> lessons = new ArrayList<>();

        Map<String, Object> lesson1 = new HashMap<>();
        lesson1.put("number_para", "1");
        lesson1.put("discipline", "Физика");
        lesson1.put("number_week", "1");
        lessons.add(lesson1);

        day1.put("1", lessons);
        input.put("1", day1);

        Map<Integer, Map<Integer, List<LessonDTO>>> result = WebSchedule.parseDisciplines(input);

        assertEquals(1, result.size());
        assertEquals(1, result.get(1).size());
        assertEquals("Физика", result.get(1).get(1).get(0).getDiscipline());
    }

     ObjectMapper originalMapper;
}
