package com.smartlearn.course.enums;

public enum EnrollStatusEnum {

    ENROLLED("ENROLLED", "已选课"),
    DROPPED("DROPPED", "已退课"),
    COMPLETED("COMPLETED", "已完成");

    private final String code;
    private final String desc;

    EnrollStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
