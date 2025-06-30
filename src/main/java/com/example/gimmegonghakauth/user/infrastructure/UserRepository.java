package com.example.gimmegonghakauth.user.infrastructure;

import com.example.gimmegonghakauth.user.domain.UserDomain;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserDomain,Long> {

    @EntityGraph(attributePaths = "majorsDomain")
    Optional<UserDomain> findByStudentId(Long studentId);

    boolean existsByStudentId(Long studentId);

    boolean existsByEmail(String email);

}
