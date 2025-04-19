package org.ev3nt.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class ScheduleDTO {
    private String status;
    private String time;
    private TeacherDTO teacher;
    private GroupDTO group;
    private String semestr;
    private String year;

    @JsonIgnore
    private Map<Integer, Map<Integer, List<LessonDTO>>> disciplines;

    @JsonIgnore
    private String message;

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

    public TeacherDTO getTeacher() {
        return teacher;
    }

    public void setTeacher(TeacherDTO teacher) {
        this.teacher = teacher;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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