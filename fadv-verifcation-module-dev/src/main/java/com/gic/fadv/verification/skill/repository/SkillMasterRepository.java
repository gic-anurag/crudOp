package com.gic.fadv.verification.skill.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.skill.model.SkillMaster;

@Repository
public interface SkillMasterRepository extends JpaRepository<SkillMaster, Long> {

}
