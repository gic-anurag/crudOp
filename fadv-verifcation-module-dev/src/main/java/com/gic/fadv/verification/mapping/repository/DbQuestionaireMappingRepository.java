package com.gic.fadv.verification.mapping.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.mapping.model.DbQuestionaireMapping;

@Repository
public interface DbQuestionaireMappingRepository extends JpaRepository<DbQuestionaireMapping, Long> {


	
	@Query(value  ="SELECT Distinct(component_name) FROM verification.db_questionaire_mapping",nativeQuery=true)
	public List<String>getDistictConponenetNames();
	
	@Query(value  ="SELECT Distinct(product_name) FROM verification.db_questionaire_mapping",nativeQuery=true)
	public List<String>getDistictProductNames();
}
