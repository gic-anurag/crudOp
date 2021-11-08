package com.gic.fadv.verification.stellar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.stellar.model.StellarMisReport;

@Repository
public interface StellarMisReportRepository extends JpaRepository<StellarMisReport, Long> {

	@Query(value = "SELECT * FROM verification.stellar_mis_report smr WHERE CAST(DATE(smr.created_date) AS VARCHAR) "
			+ "BETWEEN SYMMETRIC :fromDate AND :toDate", nativeQuery = true)
	List<StellarMisReport> getStellarReportByDate(String fromDate, String toDate);
}
