package com.gic.fadv.verification.mapping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.mapping.model.QuestionaireTransaction;
import com.gic.fadv.verification.mapping.pojo.QuestioneireMappingRes;

@Repository
public interface QuestionaireTransactionRepository extends JpaRepository<QuestionaireTransaction,Long> {

	@Query("SELECT qt FROM QuestionaireTransaction qt WHERE qt.questionaireMappingId=:questionaireMappingId")
	public QuestionaireTransaction getQuestionaireTransactionByQuestioneireMapping(@Param("questionaireMappingId") Long questionaireMappingId); 
	
	
//	@Query(value="SELECT DISTINCT ON(qm.global_question_id) qm.global_question_id, qm.id,qm.component,qm.product_name,qm.global_question,qm.questione_type,qm.form_label,qm.report_label,qm.pq_precedence,qm.mandatory,qm.input_type,qm.entity_name,qm.questione_scope,qm.status,qm.created_date_time,qt.field_mapping FROM verification.questionaire_mapping qm  LEFT JOIN verification.questionaire_transaction qt ON qt.questionaire_mapping_id=qm.id WHERE qm.component=?1 AND qm.product_name=?2 AND qm.questione_scope IN ('Verification Only','Both') ORDER BY qm.global_question_id",nativeQuery=true)
//	public List<QuestioneireMappingResponse>getQuestionaireTransactionByComponentAndProductName(String component,  String productName);
	
	@Query(value = "SELECT DISTINCT ON(qm.global_question_id) qm.global_question_id AS globalQuestionId , qm.id AS id,qm.component AS component ,qm.product_name AS productName,qm.global_question AS globalQuestion,qm.questione_type AS questioneType,qm.form_label AS formLabel,qm.report_label AS reportLabel,qm.pq_precedence AS pqPrecedence,qm.mandatory AS mandatory,qm.input_type AS inputType,qm.entity_name AS entityName,qm.questione_scope AS questioneScope,qm.status AS status,qm.created_date_time AS createdDateTime,qt.field_mapping AS fieldMapping FROM verification.questionaire_mapping qm  LEFT JOIN verification.questionaire_transaction qt ON qt.questionaire_mapping_id=qm.id WHERE qm.component= :component AND qm.product_name= :productName AND qm.questione_scope IN ('Verification Only','Both') ORDER BY qm.global_question_id", nativeQuery = true)
	public List<QuestioneireMappingRes> getQuestionaireTransactionByComponentAndProductName(@Param("component")String component,
			@Param("productName")String productName);
	
//	@Query("SELECT new com.gic.fadv.verification.mapping.pojo.QuestioneireMappingResponse(qm.id,qm.component,qm.productName,qm.globalQuestionId,qm.globalQuestion,qm.questioneType,qm.formLabel,qm.reportLabel,qm.pqPrecedence,qm.mandatory,qm.inputType,qm.entityName,qm.questioneScope,qm.status,qm.createdDateTime,qt.fieldMapping) FROM QuestionaireMapping qm  right join QuestionaireTransaction qt on qt.questionaireMappingId=qm.id WHERE qm.component=:component AND qm.productName=:productName and qm.questioneScope in ('Verification Only','Both')")
//	public List<QuestioneireMappingResponse>getQuestionaireTransactionByComponentAndProductName(@Param("component") String component, @Param("productName") String productName);
}
