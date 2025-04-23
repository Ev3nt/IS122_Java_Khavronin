package org.ev3nt.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ScheduleDTO {
    private String status;
    private String time;
    private TeacherDTO teacher;
    private GroupDTO group;
    private String semestr;
    private String year;
    private String message;

    @JsonIgnore
    private Map<Integer, Map<Integer, List<LessonDTO>>> disciplines;

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

    @JsonProperty("disciplines")
    public Map<Integer, Map<Integer, List<LessonDTO>>> getDisciplines() {
        return disciplines;
    }

    @JsonIgnore
    public void setDisciplines(Map<Integer, Map<Integer, List<LessonDTO>>> disciplines) {
        this.disciplines = disciplines;
    }

    @SuppressWarnings("unused")
    public TeacherDTO getTeacher() {
        return teacher;
    }

    @SuppressWarnings("unused")
    public void setTeacher(TeacherDTO teacher) {
        this.teacher = teacher;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void prepareDisciplines(Function<LessonDTO, String> contentGenerator) {
        disciplines.values().stream()
                .flatMap(semesterMap -> semesterMap.values().stream())
                .flatMap(List::stream)
                .forEach(lesson -> lesson.setPlainText(
                        contentGenerator.apply(lesson)
                ));
    }

    public List<Integer> getPairNumbers() {
        List<Integer> pairNumbers = disciplines.values().stream()
                .flatMap(innerMap -> innerMap.keySet().stream())
                .distinct()
                .collect(Collectors.toList());

        int min = pairNumbers.stream().min(Integer::compareTo).orElse(0);
        int max = pairNumbers.stream().max(Integer::compareTo).orElse(0);

        return IntStream.rangeClosed(min, max)
                .boxed()
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean isEmpty() {
        return (status == null || status.isEmpty()) &&
                (time == null || time.isEmpty()) &&
                teacher == null &&
                group == null &&
                (semestr == null || semestr.isEmpty()) &&
                (year == null || year.isEmpty()) &&
                (disciplines == null || disciplines.isEmpty());
    }
}