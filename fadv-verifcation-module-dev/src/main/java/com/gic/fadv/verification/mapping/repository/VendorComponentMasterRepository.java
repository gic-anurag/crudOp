package com.gic.fadv.verification.mapping.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.mapping.model.VendorComponentMaster;

@Repository
public interface VendorComponentMasterRepository extends JpaRepository<VendorComponentMaster, Long> {

	
	
}
