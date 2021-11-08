package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.CaseClientDetails;
import com.fadv.cspi.entities.CaseDetails;

public interface CaseClientDetailsRepository extends JpaRepository<CaseClientDetails, Long> {

	List<CaseClientDetails> findByCaseDetails(CaseDetails caseDetails);
}
