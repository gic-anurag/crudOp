package com.gic.fadv.verification.attempts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.gic.fadv.verification.attempts.model.AttemptUploadDocument;

@Transactional
public interface AttemptUploadDocumentRepository extends JpaRepository<AttemptUploadDocument, Long> {

//	@Query(value = "SELECT * FROM {h-schema}attempt_upload_document WHERE checkid = :checkId", nativeQuery = true)
//	List<AttemptUploadDocument> findByCheckId(@Param("checkId") Long checkId);

	@Modifying
	@Query(value = "DELETE FROM {h-schema}attempt_upload_document WHERE checkid = :checkId", nativeQuery = true)
	void deleteByCheckId(@Param("checkId") String checkId);
	
	List<AttemptUploadDocument> findByCheckid(String checkId);
}
