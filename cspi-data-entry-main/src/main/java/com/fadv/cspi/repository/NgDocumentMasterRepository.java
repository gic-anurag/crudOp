package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.NgDocumentMaster;

public interface NgDocumentMasterRepository extends JpaRepository<NgDocumentMaster, Long> {

	List<NgDocumentMaster> findByNgDocumentMasterMongoId(String ngDocumentMasterMongoId);
}
