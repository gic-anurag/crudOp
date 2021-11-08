package com.gic.fadv.verification.mapping.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gic.fadv.verification.mapping.interfaces.MappedQuestionsInterface;
import com.gic.fadv.verification.mapping.interfaces.VendorQuestionnaireInterface;
import com.gic.fadv.verification.mapping.model.VendorQuestionaireTransaction;
import com.gic.fadv.verification.mapping.pojo.QuestionaireRequestPojo;
import com.gic.fadv.verification.mapping.pojo.VendorQuestionnairePOJO;
import com.gic.fadv.verification.mapping.repository.VendorQuestionaireTransactionRepository;

@Service
public class VendorQuestionnaireServiceImpl implements VendorQuestionnaireService {

	@Autowired
	private VendorQuestionaireTransactionRepository vendorQuestionaireTransactionRepository;

	@Override
	public List<VendorQuestionnaireInterface> fetchQuetionnaireMapping(
			VendorQuestionnairePOJO vendorQuestionnairePOJO) {
		String componentName = vendorQuestionnairePOJO.getComponentName() != null
				? vendorQuestionnairePOJO.getComponentName()
				: "";
		String productName = vendorQuestionnairePOJO.getProductName() != null ? vendorQuestionnairePOJO.getProductName()
				: "";
		String type = vendorQuestionnairePOJO.getType() != null ? vendorQuestionnairePOJO.getType() : "";

		if (StringUtils.isNotEmpty(componentName) && StringUtils.isNotEmpty(productName)) {
			if (StringUtils.equalsIgnoreCase(componentName, "Education") && StringUtils.isNotEmpty(type)) {
				return vendorQuestionaireTransactionRepository.getEducationQuestionnaires(componentName, productName,
						type);
			} else if (StringUtils.equalsIgnoreCase(componentName, "Address")) {
				return vendorQuestionaireTransactionRepository.getAddressQuestionnaires(componentName, productName);
			}
		}
		return new ArrayList<>();
	}

	@Override
	public List<MappedQuestionsInterface> getQuetionnaireMapping(QuestionaireRequestPojo questionaireRequestPojo) {
		String componentName = questionaireRequestPojo.getComponent() != null ? questionaireRequestPojo.getComponent()
				: "";
		String productName = questionaireRequestPojo.getProductName() != null ? questionaireRequestPojo.getProductName()
				: "";
		String type = questionaireRequestPojo.getType() != null ? questionaireRequestPojo.getType() : "";

		if (StringUtils.isNotEmpty(componentName) && StringUtils.isNotEmpty(productName)) {
			if (StringUtils.equalsIgnoreCase(componentName, "Education") && StringUtils.isNotEmpty(type)) {
				return vendorQuestionaireTransactionRepository.getMappedQuestionnaireDetailsByType(
						questionaireRequestPojo.getComponent(), questionaireRequestPojo.getProductName(),
						questionaireRequestPojo.getType());
			} else if (StringUtils.equalsIgnoreCase(componentName, "Address")) {
				return vendorQuestionaireTransactionRepository.getMappedQuestionnaireDetails(
						questionaireRequestPojo.getComponent(), questionaireRequestPojo.getProductName());
			}
		}
		return new ArrayList<>();
	}

	@Override
	public List<VendorQuestionnaireInterface> saveQuestionnaireMapping(
			List<VendorQuestionnairePOJO> vendorQuestionnairePOJOs) {
		List<VendorQuestionaireTransaction> vendorQuestionaireTransactions = vendorQuestionnairePOJOs.stream()
				.map(this::setQuestionnaireMapping).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(vendorQuestionaireTransactions)) {
			vendorQuestionaireTransactionRepository.saveAll(vendorQuestionaireTransactions);
		}
		return fetchQuetionnaireMapping(vendorQuestionnairePOJOs.get(0));
	}

	private VendorQuestionaireTransaction setQuestionnaireMapping(VendorQuestionnairePOJO vendorQuestionnairePOJO) {
		if (vendorQuestionnairePOJO.getQuestionnaireMappingId() != null
				&& vendorQuestionnairePOJO.getQuestionnaireMappingId() != 0) {
			VendorQuestionaireTransaction vendorQuestionaireTransaction = new VendorQuestionaireTransaction();

			if (vendorQuestionnairePOJO.getId() != null && vendorQuestionnairePOJO.getId() != 0) {
				Optional<VendorQuestionaireTransaction> vendorQuestionaireTransactionOpt = vendorQuestionaireTransactionRepository
						.findById(vendorQuestionnairePOJO.getId());

				if (vendorQuestionaireTransactionOpt.isPresent()) {
					VendorQuestionaireTransaction existingVndorQuestionaireTransaction = vendorQuestionaireTransactionOpt
							.get();
					vendorQuestionaireTransaction.setId(existingVndorQuestionaireTransaction.getId());
					vendorQuestionaireTransaction
							.setFieldMapping(existingVndorQuestionaireTransaction.getFieldMapping());
					vendorQuestionaireTransaction
							.setVerifiedData(existingVndorQuestionaireTransaction.getVerifiedData());
					vendorQuestionaireTransaction.setStatus(existingVndorQuestionaireTransaction.getStatus());
				}
			}

			vendorQuestionaireTransaction.setComponentName(vendorQuestionnairePOJO.getComponentName());
			vendorQuestionaireTransaction.setQuestionaireMappingId(vendorQuestionnairePOJO.getQuestionnaireMappingId());
			vendorQuestionaireTransaction.setType(vendorQuestionnairePOJO.getType());
			if (vendorQuestionnairePOJO.getReportComments() != null
					&& StringUtils.isNotEmpty(vendorQuestionnairePOJO.getReportComments())) {
				vendorQuestionaireTransaction.setFieldMapping(vendorQuestionnairePOJO.getReportComments());
			}
			if (vendorQuestionnairePOJO.getVerifiedData() != null
					&& StringUtils.isNotEmpty(vendorQuestionnairePOJO.getVerifiedData())) {
				vendorQuestionaireTransaction.setVerifiedData(vendorQuestionnairePOJO.getVerifiedData());
			}
			if (vendorQuestionnairePOJO.getStatus() != null
					&& StringUtils.isNotEmpty(vendorQuestionnairePOJO.getStatus())) {
				vendorQuestionaireTransaction.setStatus(vendorQuestionnairePOJO.getStatus());
			}

			return vendorQuestionaireTransaction;
		}
		return null;
	}
}
