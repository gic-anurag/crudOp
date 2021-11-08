package com.gic.fadv.verification.mapping.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gic.fadv.verification.mapping.model.QuestionaireTransaction;
import com.gic.fadv.verification.mapping.pojo.QuestionaireRequestPojo;
import com.gic.fadv.verification.mapping.pojo.QuestionaireTransactionPojo;
import com.gic.fadv.verification.mapping.pojo.QuestioneireMappingRes;
import com.gic.fadv.verification.mapping.pojo.QuestioneireMappingResponse;
import com.gic.fadv.verification.mapping.repository.QuestionaireMappingRepository;
import com.gic.fadv.verification.mapping.repository.QuestionaireTransactionRepository;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india/")
public class QuestionaireTransactionController {

	@Autowired
	private QuestionaireTransactionRepository questionaireTransactionRepository;

	@Autowired
	private QuestionaireMappingRepository questionaireMappingRepository;

	/**
	 * add new questioneire transaction
	 * 
	 * @return
	 */
	@ApiOperation(value = "add new QuestioneireTransaction")
	@PostMapping("/save-questioneire")
	public ResponseEntity<List<QuestionaireTransaction>> saveQuestionnereTransaction(
			@RequestBody List<QuestionaireTransactionPojo> questionaireTransactionPojoList) {

		List<QuestionaireTransaction> qtListres = new ArrayList<>();
		QuestionaireTransaction qt = null;
		for (QuestionaireTransactionPojo qtPojo : questionaireTransactionPojoList) {

			qt = questionaireTransactionRepository.getQuestionaireTransactionByQuestioneireMapping(qtPojo.getId());
			if (qt != null) {
				qt.setQuestionaireMappingId(qtPojo.getId());
				qt.setFieldMapping(qtPojo.getUpdatedtext());
				QuestionaireTransaction qtRes = questionaireTransactionRepository.save(qt);
				qtListres.add(qtRes);
			} else {
				qt = new QuestionaireTransaction();
				qt.setQuestionaireMappingId(qtPojo.getId());
				qt.setFieldMapping(qtPojo.getUpdatedtext());
				QuestionaireTransaction qtRes = questionaireTransactionRepository.save(qt);
				qtListres.add(qtRes);
			}
		}

		if (!qtListres.isEmpty())
			return new ResponseEntity<>(qtListres, HttpStatus.CREATED);

		return new ResponseEntity<>(qtListres, HttpStatus.EXPECTATION_FAILED);
	}

	/**
	 * get questionere transaction details with letf join with questionere mapping
	 * 
	 * @return
	 */

	@ApiOperation(value = "get questionere transaction details with letf join with questionere mapping")
	@GetMapping("/get-questionere")
	public ResponseEntity<List<QuestioneireMappingResponse>> getQuestionerWithfieldMapping() {

		return new ResponseEntity<>(questionaireMappingRepository.getQuestionaireMappingWsfwe(), HttpStatus.OK);
	}

	/**
	 * get questioneire transaction details with questionere maping by component and
	 * productName
	 * 
	 * @param questionaireRequestPojo
	 * @return
	 */
	@ApiOperation(value = "get questioneire transaction  details with questionere maping by component and productName")
	@PostMapping("/get-questionere-details")
	public ResponseEntity<List<QuestioneireMappingRes>> getQuestionerTansactionByComponentAndProductName(
			@RequestBody QuestionaireRequestPojo questionaireRequestPojo) {
		List<QuestioneireMappingRes> questionList=questionaireTransactionRepository.getQuestionaireTransactionByComponentAndProductName(questionaireRequestPojo.getComponent(), questionaireRequestPojo.getProductName());
		return new ResponseEntity<>(questionList,HttpStatus.OK);

	}

}
