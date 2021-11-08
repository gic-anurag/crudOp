package com.gic.fadv.vendor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.gic.fadv.vendor.model.AttemptStatusData;

@Transactional
public interface AttemptStatusDataRepository extends JpaRepository<AttemptStatusData, Long> {
	
}
