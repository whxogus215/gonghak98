package com.example.gimmegonghakauth.common.infrastructure;

import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MajorsDao  extends JpaRepository<MajorsDomain, Long> {
    MajorsDomain findByMajor(String Major);

    Optional<MajorsDomain> findById(Long id);
}
