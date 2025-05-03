package org.ev3nt.modes;

import org.ev3nt.web.dto.LessonDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeacherScheduleTest {
    // Здесь я тестирую только форматер, потому что он везде +- одинаковый
    @Test
    void teacherItem_ShouldReturnCorrectNameAndId() {
        TeacherSchedule.TeacherItem teacher = new TeacherSchedule.TeacherItem("Петров А.А.", 456);
        assertEquals("Петров А.А.", teacher.getName());
        assertEquals(456, teacher.getTeacherId());
    }

    @Test
    void preparePlainText_ShouldFormatLessonWithoutNulls() {
        TeacherSchedule schedule = new TeacherSchedule();
        LessonDTO lesson = new LessonDTO();
        lesson.setDiscipline("Физика");
        lesson.setAud("404");

        String result = schedule.preparePlainText(lesson);
        assertEquals("Физика 404", result);
    }

    @Test
    void preparePlainText_ShouldHandleEvenWeekType() {
        TeacherSchedule schedule = new TeacherSchedule();
        LessonDTO lesson = new LessonDTO();
        lesson.setDiscipline("Химия");
        lesson.setType_week("even");

        String result = schedule.preparePlainText(lesson);
        assertTrue(result.contains("чётная"));
    }

    @Test
    void updateTeacherList_ShouldNotThrow() {
        TeacherSchedule schedule = new TeacherSchedule();
        assertDoesNotThrow(schedule::updateTeacherList);
    }

    @Test
    void preparePlainText_ShouldHandleMinimumFields() {
        TeacherSchedule schedule = new TeacherSchedule();
        LessonDTO lesson = new LessonDTO();
        lesson.setDiscipline("Физика");
        lesson.setType("Практика");

        String result = schedule.preparePlainText(lesson);
        assertEquals("Физика Практика", result);
    }
}