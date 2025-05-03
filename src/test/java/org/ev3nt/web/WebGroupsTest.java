package org.ev3nt.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebGroupsTest {
    @Test
    void cacheGroupsAndGetCachedGroups_ShouldWorkCorrectly() throws JsonProcessingException {
        Map<String, List<String>> testData = new HashMap<>();
        testData.put("Факультет 1", Arrays.asList("Группа 1", "Группа 2"));

        ObjectMapper originalMapper = WebGroups.mapper;
        String originalCacheName = WebGroups.cacheName;

        try {
            WebGroups.mapper = new ObjectMapper();
            WebGroups.cacheName = "test_groups_cache";

            WebGroups.cacheGroups(testData);

            Map<String, List<String>> result = WebGroups.getCachedGroups();

            assertEquals(testData, result);
            assertEquals(1, result.size());
            assertTrue(result.containsKey("Факультет 1"));
            assertEquals(2, result.get("Факультет 1").size());

        } finally {
            WebGroups.mapper = originalMapper;
            WebGroups.cacheName = originalCacheName;
        }
    }
}
