package com.gic.fadv.cbvutvi4v.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gic.fadv.cbvutvi4v.model.CBVUTVI4VRuleConfig;

@Repository
public interface CBVUTVI4VRuleConfigRepository extends JpaRepository<CBVUTVI4VRuleConfig, Integer> {
	List<CBVUTVI4VRuleConfig> findByComponentNameAndSubComponentName(String componentName,String subComponentName);
}
