package com.gic.fadv.verification.mapping.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.mapping.model.QuestionaireMapping;
import com.gic.fadv.verification.mapping.pojo.QuestioneireMappingResponse;

@Repository
public interface QuestionaireMappingRepository extends JpaRepository<QuestionaireMapping, Long> {
	
	@Query("SELECT qm FROM QuestionaireMapping qm  WHERE qm.component=:component AND qm.productName=:productName AND (qm.questioneScope='Both' OR qm.questioneScope='Verification Only')")
    public List<QuestionaireMapping>getQuestionaireMappingByComponentAndProductName(@Param("component") String component, @Param("productName") String productName );
		
	@Query("SELECT new com.gic.fadv.verification.docs.pojo.QuestioneireMappingResponse(qm.id,qm.component,qm.productName,qm.globalQuestionId,qm.globalQuestion,qm.questioneType,qm.formLabel,qm.reportLabel,qm.pqPrecedence,qm.mandatory,qm.inputType,qm.entityName,qm.questioneScope,qm.status,qm.createdDateTime,qt.fieldMapping) FROM QuestionaireTransaction qt left join QuestionaireMapping qm on qt.questionaireMappingId=qm.id")
	public List<QuestioneireMappingResponse>getQuestionaireMappingWsfwe();
	
	
}
