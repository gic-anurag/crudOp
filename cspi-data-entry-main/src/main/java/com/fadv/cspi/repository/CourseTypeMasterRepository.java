package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.CourseTypeMaster;

public interface CourseTypeMasterRepository extends JpaRepository<CourseTypeMaster, Long> {
	List<CourseTypeMaster> findByCourseTypeMasterMongoId(String courseTypeMasterMongoId);
}
