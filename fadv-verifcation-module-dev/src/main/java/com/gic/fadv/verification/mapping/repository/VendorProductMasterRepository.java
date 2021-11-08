package com.gic.fadv.verification.mapping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.mapping.model.VendorProductMaster;

@Repository
public interface VendorProductMasterRepository extends JpaRepository<VendorProductMaster, Long> {

	@Query("SELECT vpm FROM VendorProductMaster vpm WHERE vpm.componentidFk=:componentId ")
	List<VendorProductMaster> getAllProductsByComponentId(Long componentId);

}
