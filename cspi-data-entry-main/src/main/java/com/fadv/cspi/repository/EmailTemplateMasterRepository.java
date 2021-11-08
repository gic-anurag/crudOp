package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.EmailTemplateMaster;

public interface EmailTemplateMasterRepository extends JpaRepository<EmailTemplateMaster, Long> {

	List<EmailTemplateMaster> findByEmailTemplateMasterMongoId(String emailTemplateMasterMongoId);
}
