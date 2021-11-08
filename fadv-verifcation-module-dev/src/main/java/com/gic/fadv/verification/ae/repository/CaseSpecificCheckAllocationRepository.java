package com.gic.fadv.verification.ae.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.ae.model.CaseSpecificCheckAllocation;

@Repository
public interface CaseSpecificCheckAllocationRepository extends JpaRepository<CaseSpecificCheckAllocation, Long>
{

}