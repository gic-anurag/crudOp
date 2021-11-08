package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.SubjectDetailMaster;

public interface SubjectDetailMasterRepository extends JpaRepository<SubjectDetailMaster, Long> {

	List<SubjectDetailMaster> findBySubjectDetailMasterMongoId(String subjectDetailMasterMongoId);
}
