package com.gic.fadv.verification.mapping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gic.fadv.verification.mapping.interfaces.MappedQuestionsInterface;
import com.gic.fadv.verification.mapping.interfaces.VendorQuestionnaireInterface;
import com.gic.fadv.verification.mapping.model.VendorQuestionaireTransaction;
import com.gic.fadv.verification.mapping.pojo.QuestioneireMappingRes;

@Repository
public interface VendorQuestionaireTransactionRepository extends JpaRepository<VendorQuestionaireTransaction, Long> {

	@Query("SELECT vqt FROM VendorQuestionaireTransaction vqt WHERE vqt.questionaireMappingId=:id")
	public VendorQuestionaireTransaction getQuestionaireTransactionByQuestioneireMapping(Long id);

	/*
	 * @Query("SELECT new com.gic.fadv.verification.mapping.pojo.QuestioneireMappingResponse(qm.id,qm.component,qm.productName,qm.globalQuestionId,qm.globalQuestion,qm.questioneType,qm.formLabel,qm.reportLabel,qm.pqPrecedence,qm.mandatory,qm.inputType,qm.entityName,qm.questioneScope,qm.status,qm.createdDateTime,qt.fieldMapping) FROM   QuestionaireMapping qm  LEFT JOIN VendorQuestionaireTransaction qt on qt.questionaireMappingId=qm.id WHERE qm.component=:component AND qm.productName=:productName AND  qm.questioneScope in ('Verification Only','Both')"
	 * ) public List<QuestioneireMappingResponse>
	 * getQuestionaireTransactionByComponentAndProductName(@Param("component")
	 * String component, @Param("productName") String productName);
	 */

//	SELECT DISTINCT ON(qm.global_question_id) qm.global_question_id AS globalQuestionId , qm.id AS id,qm.component AS component ,qm.product_name AS productName,qm.global_question AS globalQuestion,qm.questione_type AS questionType,qm.form_label AS formLabel,qm.report_label AS reportLabel,qm.pq_precedence AS pqPrecedence,qm.mandatory AS mandatory,qm.input_type AS inputType,qm.entity_name AS entityName,qm.questione_scope AS questionScope,qm.status AS status,qm.created_date_time AS createdDateTime,qt.field_mapping AS fieldMapping FROM verification.questionaire_mapping qm  LEFT JOIN verification.questionaire_transaction qt ON qt.questionaire_mapping_id=qm.id WHERE qm.component=?1 AND qm.product_name=?2 AND qm.questione_scope IN ('Verification Only','Both') ORDER BY qm.global_question_id", nativeQuery = true

	@Query(value = "SELECT DISTINCT ON(qm.global_question_id) qm.global_question_id AS globalQuestionId , qm.id AS id,qm.component AS component ,qm.product_name AS productName,qm.global_question AS globalQuestion,qm.questione_type AS questioneType,qm.form_label AS formLabel,qm.report_label AS reportLabel,qm.pq_precedence AS pqPrecedence,qm.mandatory AS mandatory,qm.input_type AS inputType,qm.entity_name AS entityName,qm.questione_scope AS questioneScope,qm.created_date_time AS createdDateTime,qt.field_mapping AS fieldMapping  ,qt.status AS status,qt.verified_data AS verifiedData FROM verification.questionaire_mapping qm  LEFT JOIN verification.vendor_questionaire_transaction qt on qt.questionaire_mapping_id=qm.id WHERE qm.component=:component AND qm.product_name=:productName AND  qm.questione_scope IN('Verification Only','Both') ORDER BY qm.global_question_id", nativeQuery = true)
	public List<QuestioneireMappingRes> getQuestionaireTransactionByComponentAndProductName(
			@Param("component") String component, @Param("productName") String productName);

	@Query(value = "SELECT A.global_question_id AS globalQuestionId, A.form_label AS globalQuestion, "
			+ "B.status, B.verified_data AS verifiedData, B.field_mapping AS fieldMapping "
			+ "FROM {h-schema}questionaire_mapping A, {h-schema}vendor_questionaire_transaction B "
			+ "WHERE A.id = B.questionaire_mapping_id AND A.component = :componentName "
			+ "AND A.product_name = :productName", nativeQuery = true)
	List<MappedQuestionsInterface> getMappedQuestionnaireDetails(String componentName, String productName);
	
	@Query(value = "SELECT A.global_question_id AS globalQuestionId, A.form_label AS globalQuestion, "
			+ "B.status, B.verified_data AS verifiedData, B.field_mapping AS fieldMapping "
			+ "FROM {h-schema}questionaire_mapping A, {h-schema}vendor_questionaire_transaction B "
			+ "WHERE A.id = B.questionaire_mapping_id AND A.component = :componentName AND "
			+ "A.product_name = :productName and A.type = :type", nativeQuery = true)
	List<MappedQuestionsInterface> getMappedQuestionnaireDetailsByType(String componentName, String productName, String type);

	@Query(value = "SELECT DISTINCT ON(A.global_question_id) A.id AS questionnaireMappingId, "
			+ "A.component AS componentName, A.product_name AS productName, A.global_question_id AS globalQuestionId, "
			+ "A.global_question AS globalQuestion, A.questione_scope AS questionScope, "
			+ "B.field_mapping AS reportComments, B.status, B.id, "
			+ "B.verified_data AS verifiedData, A.type FROM verification.questionaire_mapping A "
			+ "LEFT JOIN verification.vendor_questionaire_transaction B ON A.id = B.questionaire_mapping_id "
			+ "WHERE A.component = :componentName AND A.product_name = :productName AND A.questione_scope IN('Verification Only','Both') "
			+ "AND A.type = :type", nativeQuery = true)
	List<VendorQuestionnaireInterface> getEducationQuestionnaires(String componentName, String productName,
			String type);

	@Query(value = "SELECT DISTINCT ON(A.global_question_id) A.id AS questionnaireMappingId, "
			+ "A.component AS componentName, A.product_name AS productName, A.global_question_id AS globalQuestionId, "
			+ "A.global_question AS globalQuestion, A.questione_scope AS questionScope, "
			+ "B.field_mapping AS reportComments, B.status, B.id, "
			+ "B.verified_data AS verifiedData, A.type FROM verification.questionaire_mapping A "
			+ "LEFT JOIN verification.vendor_questionaire_transaction B ON A.id = B.questionaire_mapping_id "
			+ "WHERE A.component = :componentName AND A.product_name = :productName "
			+ "AND A.questione_scope IN('Verification Only','Both') ", nativeQuery = true)
	List<VendorQuestionnaireInterface> getAddressQuestionnaires(String componentName, String productName);

	@Query(value = "SELECT id FROM verification.vendor_questionaire_transaction WHERE questionaire_mapping_id = :questionnaireMappingId LIMIT 1", nativeQuery = true)
	Long getVendorQuestionnaireId(Long questionnaireMappingId);
	
	List<VendorQuestionaireTransaction> findByquestionaireMappingId(Long questionaireMappingId);
}
