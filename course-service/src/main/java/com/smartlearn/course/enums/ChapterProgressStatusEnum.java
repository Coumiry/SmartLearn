package com.smartlearn.course.enums;

public enum ChapterProgressStatusEnum {

    NOT_STARTED("NOT_STARTED", "未开始"),
    LEARNING("LEARNING", "学习中"),
    FINISHED("FINISHED", "已完成");

    private final String code;
    private final String desc;

    ChapterProgressStatusEnum(String code, String desc) {
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
