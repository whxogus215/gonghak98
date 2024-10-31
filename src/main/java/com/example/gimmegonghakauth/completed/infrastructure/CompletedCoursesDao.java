package com.example.gimmegonghakauth.completed.infrastructure;

import com.example.gimmegonghakauth.completed.domain.CompletedCoursesDomain;
import com.example.gimmegonghakauth.user.domain.UserDomain;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletedCoursesDao
    extends JpaRepository<CompletedCoursesDomain, Long>, CustomCompletedCoursesDao {
    
    List<CompletedCoursesDomain> findByUserDomain(UserDomain userDomain);
}
