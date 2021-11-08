package com.gic.fadv.verification.mapping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.mapping.model.OnlineQuestionaireTransaction;
import com.gic.fadv.verification.mapping.pojo.OnlineQuestioneireMappingRes;

@Repository
public interface OnlineQuestionaireTransactionRepository extends JpaRepository<OnlineQuestionaireTransaction, Long> {

	@Query("SELECT oqt FROM OnlineQuestionaireTransaction oqt WHERE oqt.dbQuestionaireMappingId=:id")
	public OnlineQuestionaireTransaction getOnlineQuestionaireTransactionByDbQuestioneire(Long id);

//	SELECT new com.gic.fadv.verification.mapping.pojo.OnlineQuestioneireMappingResponse(dqm.id,dqm.componentName,dqm.productName,dqm.globalQuestionId,dqm.globalQuestion,dqm.questioneType,dqm.reportLabel,dqm.pqPrecedence,dqm.entityName,dqm.questioneScope,dqm.status,dqm.createdDateTime,oqt.fieldMapping) FROM OnlineQuestionaireTransaction oqt right join DbQuestionaireMapping dqm on oqt.dbQuestionaireMappingId=dqm.id WHERE dqm.componentName=:component AND dqm.productName=:productName

//	SELECT DISTINCT ON(dqm.global_question_id) dqm.global_question_id AS globalQuestionId , dqm.id AS id,dqm.component AS component ,dqm.product_name AS productName,dqm.global_question AS globalQuestion,dqm.questione_type AS questioneType,dqm.form_label AS formLabel,dqm.report_label AS reportLabel,dqm.pq_precedence AS pqPrecedence,dqm.mandatory AS mandatory,dqm.input_type AS inputType,dqm.entity_name AS entityName,dqm.questione_scope AS questioneScope,dqm.status AS status,dqm.created_date_time AS createdDateTime,oqt.field_mapping AS fieldMapping FROM verification.questionaire_mapping qm  LEFT JOIN verification.vendor_questionaire_transaction qt on qt.questionaire_mapping_id=qm.id WHERE qm.component=:component AND qm.product_name=:productName AND  qm.questione_scope IN('Verification Only','Both') ORDER BY qm.global_question_id", nativeQuery = true

	@Query(value = "SELECT DISTINCT ON(dqm.global_question_id) dqm.global_question_id AS globalQuestionId , dqm.id AS id,dqm.component_name AS componentName ,dqm.product_name AS productName,dqm.global_question AS globalQuestion,dqm.questione_type AS questioneType,dqm.report_label AS reportLabel,dqm.pq_precedence AS pqPrecedence,dqm.entity_name AS entityName,dqm.questione_scope AS questioneScope,dqm.status AS status,dqm.created_date_time AS createdDateTime,oqt.field_mapping AS fieldMapping FROM  verification.online_questionaire_transaction oqt RIGHT JOIN verification.db_questionaire_mapping dqm ON oqt.db_questionaire_mapping_id=dqm.id WHERE dqm.component_name= :componentName AND dqm.product_name= :productName ORDER BY dqm.global_question_id", nativeQuery = true)
	public List<OnlineQuestioneireMappingRes> getQuestionaireTransactionByComponentAndProductName(
			@Param("componentName") String componentName, @Param("productName") String productName);

	
//	 @Query("SELECT new com.gic.fadv.verification.mapping.pojo.OnlineQuestioneireMappingResponse(dqm.id,dqm.componentName,dqm.productName,dqm.globalQuestionId,dqm.globalQuestion,dqm.questioneType,dqm.reportLabel,dqm.pqPrecedence,dqm.entityName,dqm.questioneScope,dqm.status,dqm.createdDateTime,oqt.fieldMapping) FROM OnlineQuestionaireTransaction oqt right join DbQuestionaireMapping dqm on oqt.dbQuestionaireMappingId=dqm.id WHERE dqm.componentName=:component AND dqm.productName=:productName"
//	  ) public List<OnlineQuestioneireMappingResponse>
//	  getQuestionaireTransactionByComponentAndProductName(@Param("component")
//	  String component, @Param("productName") String productName);
	 
}
