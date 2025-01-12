package com.example.gimmegonghakauth.status.infrastructure;

import com.example.gimmegonghakauth.common.domain.MajorsDomain;
import com.example.gimmegonghakauth.status.domain.AbeekDomain;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbeekRepository extends JpaRepository<AbeekDomain, Long> {
    AbeekDomain save(AbeekDomain abeekDomain);
    List<AbeekDomain> findAllByYearAndMajorsDomain(int year, MajorsDomain majorsDomain);

}
