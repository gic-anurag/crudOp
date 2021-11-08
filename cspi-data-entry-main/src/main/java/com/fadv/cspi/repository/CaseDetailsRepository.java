package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fadv.cspi.controller.interfaces.CaseDetailsResponseInterface;
import com.fadv.cspi.entities.CaseDetails;

public interface CaseDetailsRepository extends JpaRepository<CaseDetails, Long> {

	@Query(value = "select cd.crn as crn, cd.case_no as caseNo, cd.case_creation_status as caseCreationStatus, "
			+ "cd.created_date as createdDate, cm.client_name as clientName "
			+ "from {h-schema}case_details cd, {h-schema}client_master cm, {h-schema}case_client_details ccd where "
			+ "cd.case_details_id = ccd.case_details_id and ccd.client_master_id = cm.client_master_id "
			+ "and CAST(DATE(cd.created_date) AS VARCHAR) BETWEEN SYMMETRIC :fromDate AND :toDate "
			+ "and case when :clientName != '' then lower(cm.client_name) like lower(:clientName) else true end "
			+ "and case when :crnNo != '' then cd.crn = :crnNo else true end "
			+ "and case when :caseCreationStatus != '' then lower(cd.case_creation_status) like lower(:caseCreationStatus) else true end", nativeQuery = true)
	List<CaseDetailsResponseInterface> getCaseDetailsByFilter(String fromDate, String toDate, String clientName,
			String crnNo, String caseCreationStatus);

	@Query(value = "select cd.crn as crn, cd.case_no as caseNo, cd.case_creation_status as caseCreationStatus, "
			+ "cd.created_date as createdDate, cm.client_name as clientName "
			+ "from {h-schema}case_details cd, {h-schema}client_master cm, {h-schema}case_client_details ccd where "
			+ "cd.case_details_id = ccd.case_details_id and ccd.client_master_id = cm.client_master_id "
			+ "and cd.case_no = :caseNo", nativeQuery = true)
	List<CaseDetailsResponseInterface> getCaseDetailsByCaseNo(String caseNo);
}
