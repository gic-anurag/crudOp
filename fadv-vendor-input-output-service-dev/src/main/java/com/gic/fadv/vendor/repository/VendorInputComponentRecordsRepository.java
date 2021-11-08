package com.gic.fadv.vendor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gic.fadv.vendor.model.VendorInputComponentRecords;
import com.gic.fadv.vendor.output.interfaces.CaseAndCheckDetailInterface;

public interface VendorInputComponentRecordsRepository extends JpaRepository<VendorInputComponentRecords, Long> {
	List <VendorInputComponentRecords> findByCheckId(String checkId);
	
	@Query(value = "SELECT A.instruction_check_id AS checkId, B.case_number AS caseNumber, "
			+ "A.case_specific_detail_id AS caseSpecificRecordId, B.case_specific_id AS caseSpecificId, "
			+ "A.component_name AS componentName, A.product AS product, "
			+ "CAST(B.case_reference AS VARCHAR) caseReference, B.sbu_name AS sbuName, B.package_name AS packageName "
			+ "FROM {h-schema}case_specific_record_detail A, {h-schema}case_specific_info B "
			+ "WHERE A.case_specific_id = B.case_specific_id AND A.instruction_check_id = :checkId LIMIT 1", nativeQuery = true)
	CaseAndCheckDetailInterface getDetailsUsingCheckId(String checkId);
}
