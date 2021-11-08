package com.gic.fadv.verification.mapping.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gic.fadv.verification.mapping.repository.DbQuestionaireMappingRepository;

@Service
public class DbQuestionaireMappingService {

	@Value("${csv.baseDir}")
	private String baseDir;

	@Autowired
	private DbQuestionaireMappingRepository dbQuestionaireMappingRepository;

	public String saveDbQuestinneireCsv() {
		try {

			// List<DbQuestionaireMapping> list =
			// Utility.readDbQuestionaireMappingCSV(baseDir + "/DbQuestionneire.csv");

			/*
			 * for (DbQuestionaireMapping dbeuestionaireMapping : list) {
			 * dbeuestionaireMapping.setCreatedDateTime(new Date());
			 * dbQuestionaireMappingRepository.save(dbeuestionaireMapping); }
			 */

			return "Record save successfully !!";

		} catch (Exception e) {
			e.printStackTrace();
		}

		return "Failed to save  questionaire  !!";
	}

	public List<String> getDistictComponentNames() {

		return dbQuestionaireMappingRepository.getDistictConponenetNames();
	}

	public List<String> getDistictproductNames() {

		return dbQuestionaireMappingRepository.getDistictProductNames();
	}

}
