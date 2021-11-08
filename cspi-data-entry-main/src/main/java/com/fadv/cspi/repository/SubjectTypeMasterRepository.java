package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.fadv.cspi.entities.SubjectTypeMaster;

public interface SubjectTypeMasterRepository extends JpaRepository<SubjectTypeMaster, Long> {

	List<SubjectTypeMaster> findBySubjectTypeMasterMongoId(String subjectTypeMasterMongoId);
}
