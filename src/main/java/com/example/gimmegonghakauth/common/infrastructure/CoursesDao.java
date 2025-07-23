package com.example.gimmegonghakauth.common.infrastructure;

import com.example.gimmegonghakauth.common.domain.CourseDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursesDao extends JpaRepository<CourseDomain, Long> {

    CourseDomain findByCourseId(Long id);

    CourseDomain findByName(String name);

    // 띄어쓰기를 제외한 course.name 과 비교해서 반환하는 쿼리문
    @Query(value ="select * from course where REPLACE(name, ' ', '') = :name", nativeQuery = true)
    CourseDomain findByNameIgnoreSpaces(@Param("name") String name);
}
