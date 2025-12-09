package com.smartlearn.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartlearn.course.entity.CourseChapter;
import com.smartlearn.course.entity.UserChapterProgress;
import com.smartlearn.course.enums.ChapterProgressStatusEnum;
import com.smartlearn.course.mapper.CourseChapterMapper;
import com.smartlearn.course.mapper.UserChapterProgressMapper;
import com.smartlearn.course.vo.ChapterProgressVO;
import com.smartlearn.course.service.ChapterProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChapterProgressServiceImpl implements ChapterProgressService {

    private final UserChapterProgressMapper progressMapper;
    private final CourseChapterMapper courseChapterMapper;

    @Override
    public void updateProgress(String userId, String chapterId, String statusCode) {
        // 合法性简单校验一下
        if (statusCode == null) {
            statusCode = ChapterProgressStatusEnum.FINISHED.getCode();
        }

        UserChapterProgress exist = progressMapper.selectOne(
                new LambdaQueryWrapper<UserChapterProgress>()
                        .eq(UserChapterProgress::getUserId, userId)
                        .eq(UserChapterProgress::getChapterId, chapterId)
        );

        LocalDateTime now = LocalDateTime.now();
        if (exist == null) {
            UserChapterProgress up = new UserChapterProgress();
            up.setUserId(userId);
            up.setChapterId(chapterId);
            up.setStatus(statusCode);
            up.setLastLearnTime(now);
            up.setCreatedTime(now);
            up.setUpdatedTime(now);
            progressMapper.insert(up);
        } else {
            exist.setStatus(statusCode);
            exist.setLastLearnTime(now);
            exist.setUpdatedTime(now);
            progressMapper.updateById(exist);
        }
    }

    @Override
    public List<ChapterProgressVO> getCourseProgress(String userId, String courseId) {
        // 1. 先查出该课程的所有章节 ID
        List<CourseChapter> chapters = courseChapterMapper.selectList(
                new LambdaQueryWrapper<CourseChapter>()
                        .eq(CourseChapter::getCourseId, courseId)
        );

        if (chapters == null || chapters.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> chapterIds = chapters.stream()
                .map(CourseChapter::getId)
                .toList();

        // 2. 查用户在这些章节上的进度
        List<UserChapterProgress> progresses = progressMapper.selectList(
                new LambdaQueryWrapper<UserChapterProgress>()
                        .eq(UserChapterProgress::getUserId, userId)
                        .in(UserChapterProgress::getChapterId, chapterIds)
        );

        // 3. 转成 VO
        return progresses.stream().map(p -> {
            ChapterProgressVO vo = new ChapterProgressVO();
            vo.setChapterId(p.getChapterId());
            vo.setStatus(p.getStatus());
            return vo;
        }).toList();
    }
}
