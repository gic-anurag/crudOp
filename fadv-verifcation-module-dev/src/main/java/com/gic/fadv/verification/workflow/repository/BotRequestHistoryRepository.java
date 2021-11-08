package com.gic.fadv.verification.workflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.gic.fadv.verification.workflow.model.BotRequestHistory;

@Transactional
public interface BotRequestHistoryRepository extends JpaRepository<BotRequestHistory, Long> {

	@Query(value = "select * from verification.bot_request_history brh where "
			+ "CAST(DATE(brh.created_date) AS VARCHAR) BETWEEN SYMMETRIC :fromDate AND :toDate AND "
			+ "CASE WHEN :requestStatus !=  '' THEN  lower(brh.request_status) = lower(:requestStatus) ELSE TRUE END AND "
			+ "CASE WHEN :caseNumber !=  '' THEN brh.case_number = :caseNumber ELSE TRUE END "
			+ "order by brh.case_number", nativeQuery = true)
	List<BotRequestHistory> getFilteredBotHistory(String fromDate, String toDate, String caseNumber,
			String requestStatus);

	@Query(value = "select distinct request_status from verification.bot_request_history "
			+ "where request_status is not null and request_status <> ''", nativeQuery = true)
	List<String> getBotRequestStatusList();
}
