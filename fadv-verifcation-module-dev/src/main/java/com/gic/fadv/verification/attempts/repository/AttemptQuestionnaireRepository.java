package com.gic.fadv.verification.attempts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.gic.fadv.verification.attempts.model.AttemptQuestionnaire;
import com.gic.fadv.verification.pojo.AttemptQuestionnairePOJO;

@Transactional
public interface AttemptQuestionnaireRepository extends JpaRepository<AttemptQuestionnaire, Long> {
	List<AttemptQuestionnaire> findByCheckId(String checkId);

	/*
	 * <<<<<<< HEAD
	 * 
	 * @Query(value =
	 * "SELECT A.followup_id, A.checkid, B.followup_description, B.followup_status, C.*, D.case_specific_detail_id, D.case_specific_id, "
	 * +
	 * "D.check_created_date, D.check_due_date, D.check_status, D.check_tat, D.component_name,  "
	 * +
	 * "D.entity_location, D.functional_entity_name, D.instruction_check_id, D.product "
	 * =======
	 */
	@Query(value = "SELECT A.followup_id as followupId, A.checkid as checkid, "
			+ "B.followup_description as followupDescription, B.followup_status as followUpStatus, "
			+ "C.attempt_questionnaire_id as attemptQuestionnaireId, C.question_name as questionName, "
			+ "C.global_question_id as globalQuestionId, C.application_data as applicationData, "
			+ "C.report_comments as reportComments, C.create_date as createDate, C.status as status, "
			+ "C.userid as userid, C.verified_data as verifiedData, C.adjudication as adjudication, "
			+ "D.case_specific_detail_id as caseSpecificDetailId, D.case_specific_id as caseSpecificId, "
			+ "D.check_created_date as checkCreatedDate, D.check_due_date as checkDueDate, "
			+ "D.check_status as checkStatus, D.check_tat as checkTat, D.component_name as componentName, "
			+ "CAST(D.component_record_field as varchar) componentRecordField, "
			+ "D.entity_location as entityLocation, D.functional_entity_name as functionalEntityName, "
			+ "D.instruction_check_id as instructionCheckId, D.product "
			+ "FROM {h-schema}attempt_history AS A " + "INNER JOIN {h-schema}attempt_followup_master AS B "
			+ "ON A.followup_id = B.followup_id LEFT JOIN "
			+ "{h-schema}attempt_questionnaire AS C ON A.checkid=C.check_id INNER JOIN {h-schema}case_specific_record_detail as D "
			+ "ON A.checkid=d.instruction_check_id where A.checkid=:checkId", nativeQuery = true)
	List<AttemptQuestionnairePOJO> findAllByJoin(String checkId);
}
