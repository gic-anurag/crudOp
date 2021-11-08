package com.gic.fadv.verification.attempts.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gic.fadv.verification.attempts.model.L3ComponentDetail;

public interface L3ComponentListRepository extends JpaRepository<L3ComponentDetail, Long> {
	L3ComponentDetail findBycomponentName(String componentName);
}
