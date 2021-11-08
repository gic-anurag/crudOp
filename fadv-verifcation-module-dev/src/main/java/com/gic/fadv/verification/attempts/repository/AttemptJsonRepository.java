package com.gic.fadv.verification.attempts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gic.fadv.verification.attempts.model.AttemptJson;

public interface AttemptJsonRepository extends JpaRepository<AttemptJson, Long> {

}
