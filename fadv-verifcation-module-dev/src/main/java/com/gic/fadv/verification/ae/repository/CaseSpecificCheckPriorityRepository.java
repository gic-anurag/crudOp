package com.gic.fadv.verification.ae.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.ae.model.CaseSpecificCheckPriority;

@Repository
public interface CaseSpecificCheckPriorityRepository extends JpaRepository<CaseSpecificCheckPriority, Long>
{

    CaseSpecificCheckPriority findTopByIsAllocatedOrderByPriorityAscPriorityDateAsc(boolean isAllocated);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update CaseSpecificCheckPriority checkRecord set checkRecord.isAllocated = true where checkRecord.checkId = :checkId")
    void updateCheckPriorityAllocated(@Param("checkId") String checkId);

}
