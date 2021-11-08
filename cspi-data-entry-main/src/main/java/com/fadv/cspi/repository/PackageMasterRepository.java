package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.PackageMaster;

public interface PackageMasterRepository extends JpaRepository<PackageMaster, Long> {

	List<PackageMaster> findByPackageMasterMongoId(String packageMasterMongoId);
}
