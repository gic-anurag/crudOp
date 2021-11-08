package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.CaseAdditionalDetails;
import com.fadv.cspi.entities.CaseDetails;

public interface CaseAdditionalDetailsRepository extends JpaRepository<CaseAdditionalDetails, Long> {

	List<CaseAdditionalDetails> findByCaseDetails(CaseDetails caseDetails);
}
