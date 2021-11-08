package com.gic.fadv.vendor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.gic.fadv.vendor.model.AttemptQuestionnaire;

@Transactional
public interface AttemptQuestionnaireRepository extends JpaRepository<AttemptQuestionnaire, Long> {
	List<AttemptQuestionnaire> findByCheckIdAndGlobalQuestionIdAndComponentNameAndProductNameAndType(String checkId,
			String globalQuestionId, String componentName, String productName, String type);
	
	List<AttemptQuestionnaire> findByCheckIdAndGlobalQuestionIdAndComponentNameAndProductName(String checkId,
			String globalQuestionId, String componentName, String productName);
}
