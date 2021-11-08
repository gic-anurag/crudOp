package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.SbuMaster;

public interface SbuMasterRepository extends JpaRepository<SbuMaster, Long> {

	List<SbuMaster> findBySbuMasterMongoId(String sbuMasterMongoId);
}
