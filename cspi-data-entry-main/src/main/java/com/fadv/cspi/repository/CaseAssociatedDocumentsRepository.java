package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.CaseAssociatedDocuments;
import com.fadv.cspi.entities.CaseDetails;

public interface CaseAssociatedDocumentsRepository extends JpaRepository<CaseAssociatedDocuments, Long> {

	List<CaseAssociatedDocuments> findByCaseDetails(CaseDetails caseDetails);
}
