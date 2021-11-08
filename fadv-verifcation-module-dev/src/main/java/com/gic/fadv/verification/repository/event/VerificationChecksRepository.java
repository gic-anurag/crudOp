package com.gic.fadv.verification.repository.event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.event.model.VerifiedChecks;
import com.gic.fadv.verification.pojo.VerifiedChecksInterface;


@Repository
public interface VerificationChecksRepository extends JpaRepository<VerifiedChecks, Long> {
	
	List<VerifiedChecks> findByCheckId(String checkId);
	
	@Query(value="SELECT A.verify_check_id AS verifyCheckId,  "
			+ "A.company_name AS clientName, C.candidate_name AS candidateName,  "
			+ "B.job_title AS jobTitle, "
			+ "B.email_address AS emailId, B.fax_number AS faxNo, "
			+ "B.number_provided AS contactNo, B.contact_date AS contactDate "
			+ "FROM {h-schema}verified_checks A "
			+ "INNER JOIN {h-schema}attempt_history B "
			+ "ON A.attemptid = B.attemptid "
			+ "INNER JOIN {h-schema}case_specific_info C "
			+ "ON A.case_specific_id = C.case_specific_id "
			+ "WHERE A.company_name = :companyName "
			+ "ORDER BY verify_check_id DESC LIMIT 5;", nativeQuery=true)
	List<VerifiedChecksInterface> filterByCompanyName(String companyName);

	@Query(value="SELECT A.verify_check_id AS verifyCheckId, A.aka_name AS clientName,  "
			+ "C.candidate_name AS candidateName,  "
			+ "B.job_title AS jobTitle, "
			+ "B.email_address AS emailId, B.fax_number AS faxNo, "
			+ "B.number_provided AS contactNo, B.contact_date AS contactDate "
			+ "FROM {h-schema}verified_checks A "
			+ "INNER JOIN {h-schema}attempt_history B "
			+ "ON A.attemptid = B.attemptid "
			+ "INNER JOIN {h-schema}case_specific_info C "
			+ "ON A.case_specific_id = C.case_specific_id "
			+ "WHERE A.aka_name = :akaName "
			+ "ORDER BY verify_check_id DESC LIMIT 5;", nativeQuery=true)
	List<VerifiedChecksInterface> filterByAkaName(String akaName);
}
