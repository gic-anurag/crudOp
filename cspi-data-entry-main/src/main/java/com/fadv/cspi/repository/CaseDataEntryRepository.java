package com.fadv.cspi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fadv.cspi.entities.CaseDataEntry;

public interface CaseDataEntryRepository extends JpaRepository<CaseDataEntry, Long> {
}
