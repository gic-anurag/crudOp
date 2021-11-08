package com.gic.fadv.verification.mapping.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gic.fadv.verification.mapping.interfaces.MappedQuestionsInterface;
import com.gic.fadv.verification.mapping.interfaces.VendorQuestionnaireInterface;
import com.gic.fadv.verification.mapping.model.VendorQuestionaireTransaction;
import com.gic.fadv.verification.mapping.pojo.QuestionaireRequestPojo;
import com.gic.fadv.verification.mapping.pojo.VendorQuestionnairePOJO;

@Service
public interface VendorQuestionnaireService {

	List<VendorQuestionnaireInterface> fetchQuetionnaireMapping(VendorQuestionnairePOJO vendorQuestionnairePOJO);

	List<VendorQuestionnaireInterface> saveQuestionnaireMapping(List<VendorQuestionnairePOJO> vendorQuestionnairePOJOs);

	List<MappedQuestionsInterface> getQuetionnaireMapping(QuestionaireRequestPojo questionaireRequestPojo);

}
