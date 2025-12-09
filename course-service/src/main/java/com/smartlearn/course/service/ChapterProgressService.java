package com.smartlearn.course.service;

import com.smartlearn.course.vo.ChapterProgressVO;

import java.util.List;

public interface ChapterProgressService {

    /**
     * 更新章节进度
     */
    void updateProgress(String userId, String chapterId, String statusCode);

    /**
     * 查询当前用户在某课程下所有章节进度
     */
    List<ChapterProgressVO> getCourseProgress(String userId, String courseId);
}
