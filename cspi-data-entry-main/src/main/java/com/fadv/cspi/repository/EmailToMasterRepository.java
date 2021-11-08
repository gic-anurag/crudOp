package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.EmailToMaster;

public interface EmailToMasterRepository extends JpaRepository<EmailToMaster, Long> {
	List<EmailToMaster> findByEmailToMasterMongoId(String emailToMasterMongoId);
}
