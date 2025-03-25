package org.ev3nt.web.classes.dto;

import java.util.Map;

public class ScheduleDTO {
    private String status;
    private String time;
    private TeacherDTO teacher;
    private GroupDTO group;
    private String semestr;
    private String year;
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
    }

    public TeacherDTO getTeacher() {
        return teacher;
    }

    public void setTeacher(TeacherDTO teacher) {
        this.teacher = teacher;
    }
}