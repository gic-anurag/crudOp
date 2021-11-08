package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.CaseDetails;
import com.fadv.cspi.entities.CaseUploadedDocuments;

public interface CaseUploadedDocumentsRepository extends JpaRepository<CaseUploadedDocuments, Long> {

	List<CaseUploadedDocuments> findByfileNameAndUploadTypeAndCaseDetails(String fileName, String uploadType,
			CaseDetails caseDetails);

	List<CaseUploadedDocuments> findByUploadTypeAndCaseDetails(String uploadType, CaseDetails caseDetails);
}
