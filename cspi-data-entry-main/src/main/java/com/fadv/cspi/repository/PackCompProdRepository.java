package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.PackCompProd;
import com.fadv.cspi.entities.PackageMaster;

public interface PackCompProdRepository extends JpaRepository<PackCompProd, Long> {

	List<PackCompProd> findByPackageMaster(PackageMaster packageMaster);
}
