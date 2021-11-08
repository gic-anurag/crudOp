package com.gic.fadv.verification.spoc.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.spoc.model.SPOCBulk;

@Repository
@Transactional
public interface SPOCBulkRepository extends JpaRepository<SPOCBulk, Long> {
	List<SPOCBulk> findByAkaNameAndFlag(String akaName, String flag);

	@Query(value = "Select aka_name from {h-schema}spoc_bulk where check_id = :checkId LIMIT 1", nativeQuery = true)
	String getAkaNameByCheckId(String checkId);
	
	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE {h-schema}spoc_bulk A SET flag = :flag WHERE check_id = :checkId", nativeQuery = true)
	void updateFlagByCheckId(String checkId, String flag);
	
	List<SPOCBulk> findByCheckId(String checkId);
}
