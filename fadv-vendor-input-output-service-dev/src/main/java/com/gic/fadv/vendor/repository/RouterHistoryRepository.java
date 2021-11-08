package com.gic.fadv.vendor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.gic.fadv.vendor.model.RouterHistory;

@Transactional
public interface RouterHistoryRepository extends JpaRepository<RouterHistory, Long> {
	List<RouterHistory> findByEngineNameAndCurrentEngineStatus(String engineName, String currentEngineStatus);
}
