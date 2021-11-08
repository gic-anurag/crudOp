package com.gic.fadv.verification.online.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.gic.fadv.verification.online.model.ManupatraOutput;
import com.gic.fadv.verification.online.pojo.ManupatraOutputPOJO;

@Transactional
public interface ManupatraOutputRepository extends JpaRepository<ManupatraOutput, Long> {
	@Query(value = "SELECT manupatra_output_id AS manupatraOutputId, check_id AS checkId, "
			+ "title FROM {h-schema}manupatra_output WHERE check_id = :checkId", nativeQuery = true)
	List<ManupatraOutputPOJO> getManupatraOutputByCheckId(String checkId);

	Optional<ManupatraOutput> findById(Long id);
}
