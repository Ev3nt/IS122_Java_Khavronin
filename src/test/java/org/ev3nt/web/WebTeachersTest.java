package org.ev3nt.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WebTeachersTest {
    @BeforeEach
    void setUp() {
        originalMapper = WebTeachers.mapper;
        originalCacheName = WebTeachers.cacheName;
    }

    @AfterEach
    void tearDown() {
        WebTeachers.mapper = originalMapper;
        WebTeachers.cacheName = originalCacheName;
    }

    @Test
    void cacheTeachersAndGetCachedTeachers_ShouldWorkCorrectly() throws JsonProcessingException {
        WebTeachers.mapper = new ObjectMapper();
        WebTeachers.cacheName = "test_teachers_cache";

        Map<Integer, String> testData = new HashMap<>();
        testData.put(1, "Иванов И.И.");
        testData.put(2, "Петров П.П.");

        WebTeachers.cacheTeachers(testData);

        Map<Integer, String> result = WebTeachers.getCachedTeachers();

        assertEquals(2, result.size());
        assertEquals("Иванов И.И.", result.get(1));
        assertEquals("Петров П.П.", result.get(2));
    }

    ObjectMapper originalMapper;
    String originalCacheName;
}
