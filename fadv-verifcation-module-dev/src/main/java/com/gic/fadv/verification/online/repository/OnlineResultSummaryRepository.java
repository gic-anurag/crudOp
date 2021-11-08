package com.gic.fadv.verification.online.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gic.fadv.verification.online.model.OnlineResultSummary;

@Transactional
public interface OnlineResultSummaryRepository extends JpaRepository<OnlineResultSummary, Long> {
	List<OnlineResultSummary> findByCheckId(String checkId);
}
