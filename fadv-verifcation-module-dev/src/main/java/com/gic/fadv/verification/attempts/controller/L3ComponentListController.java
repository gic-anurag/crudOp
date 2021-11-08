package com.gic.fadv.verification.attempts.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.verification.attempts.model.AttemptQuestionnaire;
import com.gic.fadv.verification.attempts.model.ComponentQuestionDetail;
import com.gic.fadv.verification.attempts.model.L3ComponentDetail;
import com.gic.fadv.verification.attempts.model.QuestionnaireDetails;
import com.gic.fadv.verification.attempts.pojo.QuestionnairePOJO;
import com.gic.fadv.verification.attempts.repository.AttemptQuestionnaireRepository;
import com.gic.fadv.verification.attempts.repository.L3ComponentListRepository;
import com.gic.fadv.verification.attempts.service.L3APIService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/india")
public class L3ComponentListController {

	@Autowired
	private L3ComponentListRepository l3ComponentListRepository;

	@Autowired
	private AttemptQuestionnaireRepository attemptQuestionnaireRepository;
//	@Autowired
//	private CaseSpecificRecordDetailRepository caseSpecificRecordDetailRepository;

	@Autowired
	private L3APIService l3APIService;

//	@Autowired
//	private EntityManager entityManager;

	@Value("${component.list.L3.url}")
	private String componentListL3Url;
	@Value("${questionaire.list.l3.url}")
	private String questionaireListL3Url;
	@Value("${questionaire.l3.auth_token}")
	private String auth_token;
	@Value("${case.detail.l3.url}")
	private String caseDetailsURL;
	@Value("${data.entry.l3.url}")
	private String dataEntryURL;
	@Value("${mrl.l3.url}")
	private String mrlURL;
	@Value("${associate.docs.url}")
	private String associateDocsUrl;
	@Value("${doc.url}")
	private String uploadDocUrl;

	private static final Logger logger = LoggerFactory.getLogger(L3ComponentListController.class);

	// Should be used only one times
	@GetMapping("/component")
	public String getAllL3ComponentList() {
		String l3response = l3APIService.sendDataTogetComponent(componentListL3Url);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String response = null;
		try {
			JsonNode componentList = mapper.readTree(l3response);

			if (componentList.has("response")) {
				if (componentList.get("response").isArray()) {
					ArrayNode componentArr = (ArrayNode) componentList.get("response");
					List<L3ComponentDetail> lcomponentDetailList = new ArrayList<>();
					for (int i = 0; i < componentArr.size(); i++) {
						L3ComponentDetail l3ComponentDetail = new L3ComponentDetail();
						l3ComponentDetail.setComponentDesc(componentArr.get(i).get("componentDesc").asText());
						l3ComponentDetail.setComponentName(componentArr.get(i).get("componentName").asText());
						l3ComponentDetail.setId(componentArr.get(i).get("id").asText());
						l3ComponentDetail.setDataSource(componentArr.get(i).get("dataSource").asText());
						l3ComponentDetail
								.setIsDatabaseComponent(componentArr.get(i).get("isDatabaseComponent").asText());
						lcomponentDetailList.add(l3ComponentDetail);
					}
					if (!lcomponentDetailList.isEmpty()) {
						l3ComponentListRepository.saveAll(lcomponentDetailList);
					} else {
						logger.info("ComponentArr has no Data. No need to save this information");
					}
				} else {
					logger.info("Response is not a Json Array");
				}
				response = componentList.get("response").toString();
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return response;
	}

	@GetMapping(path = { "/question/{componentName}/{checkId}", "/question/{componentName}" })
	public List<AttemptQuestionnaire> getAllL3ComponentQuestionList(
			@PathVariable(name = "componentName", required = true) String componentName,
			@PathVariable(name = "checkId", required = false) String checkId) {

		List<AttemptQuestionnaire> attemptQuestionnaireList = new ArrayList<>();

		checkId = checkId != null ? checkId : "";

		if (StringUtils.isNotEmpty(checkId)) {

			attemptQuestionnaireList = attemptQuestionnaireRepository.findByCheckId(checkId);

			if (attemptQuestionnaireList.isEmpty()) {
				logger.info("List is empty");

				attemptQuestionnaireList = getQuestionList(componentName, attemptQuestionnaireList);

			} else {
				logger.info("List is not empty");
			}

			logger.info("Value of attemptQuestion List" + attemptQuestionnaireList);
			return attemptQuestionnaireList;
		} else {
			attemptQuestionnaireList = getQuestionList(componentName, attemptQuestionnaireList);
		}

		/*
		 * "questionName": "Additional Comments", "questionScope": "Verification Only",
		 * "sequenceId": "16", "priorityId": "1", "globalQuestionId": "801746",
		 */
		return attemptQuestionnaireList;
		// return componentQuestionDetail1;
		// return componentQuestionDetail;
	}

	private List<AttemptQuestionnaire> getQuestionList(String componentName,
			List<AttemptQuestionnaire> attemptQuestionnaireList) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		logger.info("Value of Component Name" + componentName);
		L3ComponentDetail l3ComponentDetail = new L3ComponentDetail();
		List<ComponentQuestionDetail> componentQuestionDetail = new ArrayList<ComponentQuestionDetail>();
		List<ComponentQuestionDetail> componentQuestionDetail1 = new ArrayList<ComponentQuestionDetail>();

		try {
			l3ComponentDetail = l3ComponentListRepository.findBycomponentName(componentName);
		} catch (Exception e) {
			logger.info("DB Exception" + e.getMessage());
			e.printStackTrace();
		}
		String componentId = l3ComponentDetail.getId();
		logger.info("Value of Id on the basis of l3ComponentDetail" + l3ComponentDetail.toString());
		logger.info("Value of Id on the basis of ComponentName" + componentId);
		/*
		 * Make Rest call to L3 API
		 */
		String response = null;
		if (componentId != null) {
			try {
				response = l3APIService.sendDataTogetComponentQuestion(l3ComponentDetail.getId());
			} catch (Exception e) {
				logger.info("Component Question List Rest Exception" + e.getMessage());
				e.printStackTrace();
			}
		}
		if (response != null) {
			JsonNode componentQuestionList = null;
			try {
				componentQuestionList = mapper.readTree(response);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (componentQuestionList != null) {
				if (componentQuestionList.has("response")) {
					if (componentQuestionList.get("response").isArray()) {
						ArrayNode componentArr = (ArrayNode) componentQuestionList.get("response");
						response = componentArr.toString();
						try {
							componentQuestionDetail = mapper.readValue(response,
									new TypeReference<List<ComponentQuestionDetail>>() {
									});
							/*
							 * Logic for taking only those Questions which has scope either Both or
							 * Verification Only
							 */
							for (int i = 0; i < componentQuestionDetail.size(); i++) {
								if (componentQuestionDetail.get(i).getQuestionScope().equalsIgnoreCase("Both")
										|| componentQuestionDetail.get(i).getQuestionScope()
												.equalsIgnoreCase("Verification Only")) {
									componentQuestionDetail1.add(componentQuestionDetail.get(i));
									AttemptQuestionnaire attemptQuestionnaire = new AttemptQuestionnaire();
									attemptQuestionnaire
											.setGlobalQuestionId(componentQuestionDetail.get(i).getGlobalQuestionId());
									attemptQuestionnaire
											.setQuestionName(componentQuestionDetail.get(i).getQuestionName());
									attemptQuestionnaireList.add(attemptQuestionnaire);
								}
							}

						} catch (JsonProcessingException e) {
							// TODO Auto-generated catch block
							logger.info("Json Mapping Error" + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}

		}
		return attemptQuestionnaireList;
	}

	/*
	 * New End Points for Populating Questionnaire
	 */
	@GetMapping("/questionnaire-list/{checkId}")
	public List<AttemptQuestionnaire> createQuestionnaireList(
			@PathVariable(name = "checkId", required = false) String checkId) {
		// Creating the ObjectMapper object
		List<QuestionnairePOJO> questionnairePOJOList = new ArrayList<>();
		List<AttemptQuestionnaire> questionList = new ArrayList<>();
		List<AttemptQuestionnaire> attemptQuestionnaireDB = attemptQuestionnaireRepository.findByCheckId(checkId);
		if (attemptQuestionnaireDB != null && CollectionUtils.isNotEmpty(attemptQuestionnaireDB)) {
			return attemptQuestionnaireDB;
		} else {
			String attemptQuestionnaireRes = l3APIService.sendDataTogetCaseId(questionaireListL3Url, checkId);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			ObjectNode attemptQuestionnaireNode = mapper.createObjectNode();
			try {
				attemptQuestionnaireNode = (ObjectNode) mapper.readTree(attemptQuestionnaireRes);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			if (attemptQuestionnaireNode != null && attemptQuestionnaireNode.has("response")) {
				JsonNode questionnaire = attemptQuestionnaireNode.get("response");
				try {
					questionnairePOJOList = mapper.readValue(questionnaire.toString(),
							new TypeReference<List<QuestionnairePOJO>>() {
							});
				} catch (JsonProcessingException e) {
					logger.error("Exception occured while mapping l3 questionnaire response : {}", e.getMessage());
				}
			}
			for (int i = 0; i < questionnairePOJOList.size(); i++) {
				QuestionnairePOJO quetionObj = questionnairePOJOList.get(i);
				AttemptQuestionnaire attemptQuestionnaire = new AttemptQuestionnaire();
				attemptQuestionnaire.setGlobalQuestionId(quetionObj.getGlobalQuestionId());
				attemptQuestionnaire.setQuestionName(quetionObj.getQuestionName());
				attemptQuestionnaire.setApplicationData(quetionObj.getAnswere());
				questionList.add(attemptQuestionnaire);
			}
			return questionList;
		}
	}

	private List<AttemptQuestionnaire> getQuestionaireList(List<AttemptQuestionnaire> attemptQuestionnaireList,
			ObjectMapper mapper, String attemptQuestionnaireStr) {
		List<QuestionnaireDetails> questionnaireDetailsList;
		String response = null;
		try {
			response = l3APIService.sendDataToRest(questionaireListL3Url, attemptQuestionnaireStr, null);
		} catch (Exception e) {
			logger.info("Component Question List Rest Exception" + e.getMessage());
			e.printStackTrace();
		}
		ObjectNode questionList = mapper.createObjectNode();
		try {
			questionList = (ObjectNode) mapper.readTree(response);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayNode questionArrNode = mapper.createArrayNode();
		if (questionList != null) {
			if (questionList != null && questionList.has("response")) {
				questionArrNode = (ArrayNode) questionList.get("response");
				try {
					questionnaireDetailsList = mapper.readValue(questionArrNode.toString(),
							new TypeReference<List<QuestionnaireDetails>>() {
							});
					for (int i = 0; i < questionnaireDetailsList.size(); i++) {
						AttemptQuestionnaire attemptQuestionnaire = new AttemptQuestionnaire();
						attemptQuestionnaire.setGlobalQuestionId(questionnaireDetailsList.get(i).getGlobalQuestionId());
						attemptQuestionnaire.setQuestionName(questionnaireDetailsList.get(i).getQuestionName());
						attemptQuestionnaireList.add(attemptQuestionnaire);
					}
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
		return attemptQuestionnaireList;
	}

	@GetMapping("/casedetails/{caseId}")
	public JsonNode getCaseDetails(@PathVariable(name = "caseId", required = true) String caseId) {
		String l3response = null;
		try {
			l3response = l3APIService.sendDataTogetCaseId(caseDetailsURL, caseId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String l3AssociateDocResponse = null;
		try {
			l3AssociateDocResponse = l3APIService.sendDataTogetCaseId(associateDocsUrl, caseId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String json = "{\"caseDetails\":" + l3response + ",\"fileDetails\":" + l3AssociateDocResponse + "}";
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JsonNode responseNode = null;
		try {
			// if(l3response!=null) {
			responseNode = mapper.readTree(json);
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseNode;
	}

	@GetMapping("/dataentry/{caseId}")
	public JsonNode getDataEntry(@PathVariable(name = "caseId", required = true) String caseId) {
		String l3response = null;
		try {
			l3response = l3APIService.sendDataTogetCaseId(dataEntryURL, caseId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JsonNode responseNode = null;
		try {
			if (l3response != null) {
				responseNode = mapper.readTree(l3response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseNode;
	}

	@PostMapping("/mrl")
	public JsonNode getMrl(@RequestBody String mrlReq) {
		String l3response = l3APIService.sendDataToRest(mrlURL, mrlReq, null);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JsonNode responseNode = null;
		try {
			responseNode = mapper.readTree(l3response);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseNode;
	}

	// Endpoint for Associate Documents on the basis of caseID/caseNo
	@GetMapping("/associatecasedetails/{caseId}")
	public JsonNode getAssociateCaseDetails(@PathVariable(name = "caseId", required = true) String caseId) {
		String l3AssociateDocResponse = null;
		try {
			l3AssociateDocResponse = l3APIService.sendDataTogetCaseId(associateDocsUrl, caseId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JsonNode responseNode = null;
		try {
			// if(l3response!=null) {
			responseNode = mapper.readTree(l3AssociateDocResponse);
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseNode;
	}

	// End point for Uploading the Documents
	@GetMapping("/uploaddocuments/{fileName}")
	public JsonNode getUploadDocuments(@PathVariable(name = "fileName", required = true) String fileName) {
		String l3AssociateDocResponse = null;
		try {
			l3AssociateDocResponse = l3APIService.sendDataTogetVerification(uploadDocUrl, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JsonNode responseNode = null;
		try {
			// if(l3response!=null) {
			responseNode = mapper.readTree(l3AssociateDocResponse);
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseNode;
	}

}
