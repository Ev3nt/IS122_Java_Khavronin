package org.ev3nt.web.classes.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

public class ScheduleDTO {
    private String status;
    private String time;
    private TeacherDTO teacher;
    private GroupDTO group;
    private String semestr;
    private String year;
    private int maxLessons = 0;
    private int maxDays = 0;
    private Map<Integer, Map<Integer, List<LessonDTO>>> lessonsSchedule;

    @JsonIgnore
    private Map<Integer, Map<Integer, Map<String, LessonDTO[]>>> disciplines;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public GroupDTO getGroup() {
        return group;
    }

    public void setGroup(GroupDTO group) {
        this.group = group;
    }

    public String getSemestr() {
        return semestr;
    }

    public void setSemestr(String semestr) {
        this.semestr = semestr;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Map<Integer, Map<Integer, Map<String, LessonDTO[]>>> getDisciplines() {
        return disciplines;
    }

    public void setDisciplines(Map<Integer, Map<Integer, Map<String, LessonDTO[]>>> disciplines) {
        this.disciplines = disciplines;
        lessonsSchedule = null;
    }

    public TeacherDTO getTeacher() {
        return teacher;
    }

    @SuppressWarnings("unused")
    public void setTeacher(TeacherDTO teacher) {
        this.teacher = teacher;
    }

    @SuppressWarnings("unused")
    public int getMaxLessons() {
        if (maxLessons == 0) {
            calculateConstants();
        }

        return maxLessons;
    }

    public int getMaxDays() {
        if (maxDays == 0) {
            calculateConstants();
        }

        return maxDays;
    }

    public Map<Integer, Map<Integer, List<LessonDTO>>> getRawMappedDisciplines() {
        Map<Integer, Map<Integer, Map<String, LessonDTO[]>>> disciplines = getDisciplines();
        if (lessonsSchedule == null) {
            lessonsSchedule = new HashMap<>();

            int maxLessons = getMaxDays();

            for (int lessonNumber = 1; lessonNumber <= maxLessons; lessonNumber++) {
                Map<Integer, List<LessonDTO>> rowLessons = new HashMap<>();

                for (Map.Entry<Integer, Map<Integer, Map<String, LessonDTO[]>>> dayLessons : disciplines.entrySet()) {
                    Map<String, LessonDTO[]> lessons = dayLessons.getValue().get(lessonNumber);
                    if (lessons == null) {
                        continue;
                    }

                    List<LessonDTO> lessonsArray = new ArrayList<>();

                    for (Map.Entry<String, LessonDTO[]> lesson : lessons.entrySet()) {
                        Collections.addAll(lessonsArray, lesson.getValue());
                    }

                    rowLessons.put(dayLessons.getKey(), lessonsArray);
                }

                lessonsSchedule.put(lessonNumber, rowLessons);
            }
        }

        return lessonsSchedule;
    }

    private void calculateConstants() {
        for (Map.Entry<Integer, Map<Integer, Map<String, LessonDTO[]>>> dayEntry : disciplines.entrySet()) {
            for (Map.Entry<Integer, Map<String, LessonDTO[]>> lessonEntry : dayEntry.getValue().entrySet()) {
                maxLessons = Math.max(maxLessons, lessonEntry.getKey());
            }

            maxDays = Math.max(maxDays, dayEntry.getKey());
        }
    }
}