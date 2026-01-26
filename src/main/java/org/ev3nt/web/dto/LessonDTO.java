package org.ev3nt.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LessonDTO {
    String id_day;
    String number_para;
    String discipline;
    String type;
    String type_week;
    String aud;
    String number_week;
    String comment;
    boolean zaoch;
    String name;
    String under_group;
    String under_group_1;
    String under_group_2;
    String group_name;
    boolean date;

    @JsonIgnore
    String plain_text;

    public String getId_day() {
        return id_day;
    }

    public void setId_day(String id_day) {
        this.id_day = id_day;
    }

    public String getNumber_para() {
        return number_para;
    }

    public void setNumber_para(String number_para) {
        this.number_para = number_para;
    }

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType_week() {
        return type_week;
    }

    public void setType_week(String type_week) {
        this.type_week = type_week;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public String getNumber_week() {
        return number_week;
    }

    public void setNumber_week(String number_week) {
        this.number_week = number_week;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isZaoch() {
        return zaoch;
    }

    public void setZaoch(boolean zaoch) {
        this.zaoch = zaoch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnder_group() {
        return under_group;
    }

    public void setUnder_group(String under_group) {
        this.under_group = under_group;
    }

    public String getUnder_group_1() {
        return under_group_1;
    }

    public void setUnder_group_1(String under_group_1) {
        this.under_group_1 = under_group_1;
    }

    public String getUnder_group_2() {
        return under_group_2;
    }

    public void setUnder_group_2(String under_group_2) {
        this.under_group_2 = under_group_2;
    }

    @SuppressWarnings("unused")
    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public void setPlainText(String plain_text) {
        this.plain_text = plain_text;
    }

    // Используется в шаблонах
    @SuppressWarnings("unused")
    public String getPlainText() {
        return plain_text;
    }
}