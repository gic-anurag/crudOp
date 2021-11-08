package com.gic.fadv.verification.mapping.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gic.fadv.verification.mapping.model.OnlineQuestionaireTransaction;
import com.gic.fadv.verification.mapping.pojo.OnlineDbQuestionaireMappingRequestPojo;
import com.gic.fadv.verification.mapping.pojo.OnlineQuestionaireTransactionPojo;
import com.gic.fadv.verification.mapping.pojo.OnlineQuestioneireMappingRes;
import com.gic.fadv.verification.mapping.pojo.OnlineQuestioneireMappingResponse;
import com.gic.fadv.verification.mapping.repository.OnlineQuestionaireTransactionRepository;

import io.swagger.annotations.ApiOperation;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india/")
public class OnlineQuestionaireMappingTransactionController {

	@Autowired
	private OnlineQuestionaireTransactionRepository onlineQuestionaireTransactionRepository;

	/**
	 * add new verification questioneire mapping transaction
	 * 
	 * @return
	 */
	@ApiOperation(value = "add new QuestioneireTransaction")
	@PostMapping("/save-online-questioneire")
	public ResponseEntity<List<OnlineQuestionaireTransaction>> saveQuestionnereTransaction(
			@RequestBody List<OnlineQuestionaireTransactionPojo> onlineQuestionaireTransactionPojoList) {

		List<OnlineQuestionaireTransaction> qtListres = new ArrayList<>();
		OnlineQuestionaireTransaction qt = null;

		for (OnlineQuestionaireTransactionPojo qtPojo : onlineQuestionaireTransactionPojoList) {

			qt = onlineQuestionaireTransactionRepository
					.getOnlineQuestionaireTransactionByDbQuestioneire(qtPojo.getId());
			if (qt != null) {
				qt.setDbQuestionaireMappingId(qtPojo.getId());
				qt.setFieldMapping(qtPojo.getUpdatedtext());
				OnlineQuestionaireTransaction qtRes = onlineQuestionaireTransactionRepository.save(qt);
				qtListres.add(qtRes);
			} else {
				qt = new OnlineQuestionaireTransaction();
				qt.setDbQuestionaireMappingId(qtPojo.getId());
				qt.setFieldMapping(qtPojo.getUpdatedtext());
				OnlineQuestionaireTransaction qtRes = onlineQuestionaireTransactionRepository.save(qt);
				qtListres.add(qtRes);
			}
		}
		if (!qtListres.isEmpty())
			return new ResponseEntity<>(qtListres, HttpStatus.CREATED);

		return new ResponseEntity<>(qtListres, HttpStatus.EXPECTATION_FAILED);
	}

	/**
	 * get dbquestioneire transaction details with questionere maping by component
	 * and productName
	 * 
	 * @param questionaireRequestPojo
	 * @return
	 */
	@ApiOperation(value = "get questioneire transaction  details with questionere maping by component and productName")
	@PostMapping("/get-online-questionere-details")
	public ResponseEntity<List<OnlineQuestioneireMappingRes>> getQuestionerTansactionByComponentAndProductName(
			@RequestBody OnlineDbQuestionaireMappingRequestPojo onlineDbQuestionaireMappingRequestPojo) {
		return new ResponseEntity<>(
				onlineQuestionaireTransactionRepository.getQuestionaireTransactionByComponentAndProductName(
						onlineDbQuestionaireMappingRequestPojo.getComponentName(),
						onlineDbQuestionaireMappingRequestPojo.getProductName()),
				HttpStatus.OK);
	}

//	/**
//	 * get dbquestioneire transaction details with questionere maping by component and
//	 * productName
//	 * 
//	 * @param questionaireRequestPojo
//	 * @return
//	 */
//	@ApiOperation(value = "get questioneire transaction  details with questionere maping by component and productName")
//	@PostMapping("/get-online-questionere-details")
//	public ResponseEntity<List<OnlineQuestioneireMappingResponse>> getQuestionerTansactionByComponentAndProductName(
//			@RequestBody OnlineDbQuestionaireMappingRequestPojo onlineDbQuestionaireMappingRequestPojo) {
//		return new ResponseEntity<>(
//				onlineQuestionaireTransactionRepository.getQuestionaireTransactionByComponentAndProductName(
//						onlineDbQuestionaireMappingRequestPojo.getComponentName(), onlineDbQuestionaireMappingRequestPojo.getProductName()),
//				HttpStatus.OK);
//	}

}
