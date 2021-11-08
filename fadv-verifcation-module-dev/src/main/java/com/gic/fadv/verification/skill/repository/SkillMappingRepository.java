package com.gic.fadv.verification.skill.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.skill.model.SkillMapping;

@Repository
public interface SkillMappingRepository extends JpaRepository<SkillMapping, Long> {

	List<SkillMapping> findByUserId(Long userId);

	@Query(value = "SELECT B.skill_name FROM {h-schema}skill_mapping A, {h-schema}skill_master B WHERE A.skill_id = B.skill_id "
			+ "AND A.user_id = :userId", nativeQuery = true)
	List<String> getSkillNamesByUserId(Long userId);
}
