package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.ProductMaster;

public interface ProductMasterRepository extends JpaRepository<ProductMaster, Long> {

	List<ProductMaster> findByProductMasterMongoId(String productMasterMongoId);
}
