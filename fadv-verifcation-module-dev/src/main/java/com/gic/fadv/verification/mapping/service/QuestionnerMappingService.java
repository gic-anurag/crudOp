package com.gic.fadv.verification.mapping.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gic.fadv.verification.mapping.model.QuestionaireMapping;
import com.gic.fadv.verification.mapping.pojo.QuestionaireMappingRequest;
import com.gic.fadv.verification.mapping.repository.QuestionaireMappingRepository;
import com.gic.fadv.verification.utility.Utility;

@Transactional
@Service
public class QuestionnerMappingService {

	@Value("${csv.baseDir}")
	private String baseDir;

	@Autowired
	private QuestionaireMappingRepository questionaireMappingRepository;

	public String saveQuestinnerCsv() {
		try {

			List<QuestionaireMapping> list = Utility.readFromCSV(baseDir + "/QuestioneireMapping.csv");

			for (QuestionaireMapping euestionaireMapping : list) {
				euestionaireMapping.setStatus("A");
				euestionaireMapping.setCreatedDateTime(new Date());
				questionaireMappingRepository.save(euestionaireMapping);
			}

			return "Record save successfully !!";

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "Failed to save  questionaire  !!";
	}

	public List<QuestionaireMapping> getQuestionaireByComponentAndProductName(
			QuestionaireMappingRequest questionaireMappingRequest) {
		try {
			return questionaireMappingRepository.getQuestionaireMappingByComponentAndProductName(
					questionaireMappingRequest.getComponent(), questionaireMappingRequest.getProductName());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ArrayList<>();
	}

}
