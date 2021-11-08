package com.fadv.cspi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.CityMaster;

public interface CityMasterRepository extends JpaRepository<CityMaster, Long> {
	List<CityMaster> findByCityName(String cityName);
}
