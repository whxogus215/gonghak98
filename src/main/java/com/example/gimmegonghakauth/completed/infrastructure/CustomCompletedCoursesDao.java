package com.example.gimmegonghakauth.completed.infrastructure;

import com.example.gimmegonghakauth.completed.domain.CompletedCoursesDomain;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public interface CustomCompletedCoursesDao {

    @Transactional
    void saveAll(List<CompletedCoursesDomain> completedCourses);
}
