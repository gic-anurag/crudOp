package com.gic.fadv.online.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.online.model.AttemptHistory;
import com.gic.fadv.online.model.AttemptStatusData;
import com.gic.fadv.online.model.Component;
import com.gic.fadv.online.model.ComponentScoping;
import com.gic.fadv.online.model.Datum;
import com.gic.fadv.online.model.OnlineManualVerification;
import com.gic.fadv.online.model.OnlineReq;
import com.gic.fadv.online.model.OnlineVerificationChecks;
import com.gic.fadv.online.pojo.ApiServiceResultPOJO;
import com.gic.fadv.online.pojo.CaseReferencePOJO;
import com.gic.fadv.online.pojo.CaseSpecificInfoPOJO;
import com.gic.fadv.online.pojo.CaseSpecificRecordDetailPOJO;
import com.gic.fadv.online.pojo.CheckVerificationPOJO;
import com.gic.fadv.online.pojo.FileUploadPOJO;
import com.gic.fadv.online.pojo.TaskSpecsPOJO;
import com.gic.fadv.online.pojo.VerificationEventStatusPOJO;
import com.gic.fadv.online.utility.Utility;
import com.google.gson.JsonParser;

@Service
public class OnlineServiceImpl implements OnlineService {

	@Value("${data.entry.l3.url}")
	private String l3DataEntryURL;
	@Autowired
	private OnlineApiService onlineApiService;

	@Autowired
	private ApiService apiService;

	@Autowired
	private Environment env;

	@Autowired
	private OnlineFinalService onlineFinalService;

	@Value("${online.router.mca.rest.url}")
	private String mcaRestUrl;
	@Value("${online.router.loan.rest.url}")
	private String loanRestUrl;
	@Value("${online.router.manupatra.rest.url}")
	private String manupatraRestUrl;
	@Value("${online.router.adversemedia.rest.url}")
	private String adversemediaRestUrl;
	@Value("${online.router.worldcheck.rest.url}")
	private String worldcheckRestUrl;
	@Value("${online.router.watchout.rest.url}")
	private String watchoutRestUrl;
	@Value("${casespecificinfo.rest.url}")
	private String caseSpecificInfoUrl;
	@Value("${componentwisemapping.rest.url}")
	private String componentWiseApiUrl;
	@Value("${online.verifictioneventstatus.rest.url}")
	private String verifictioneventstatusRestUrl;
	@Value("${spocattemptstatusdata.rest.url}")
	private String spocAttemptStatusDataRestUrl;
	@Value("${spocattempthistory.rest.url}")
	private String spocAttemptHistoryRestUrl;
	@Value("${verification.online.manual.UI.url}")
	private String verificationOnlineManualUIUrl;
	@Value("${verification.url.checkid.l3}")
	private String verificationStatusUrlL3;
	@Value("${updateverifychecks.rest.url}")
	private String updateVerifyChecksUrl;
	@Value("${online.personal.verify.rest.url}")
	private String verifyIdApiUrl;
	@Value("${verification.status.url.l3}")
	private String verificationStatusL3Url;
	@Value("${onlineverifychecks.rest.url}")
	private String onlineverifychecksRestUrl;

	private static final Logger logger = LoggerFactory.getLogger(OnlineServiceImpl.class);

	@Override
	public Map<String, String> getDetailsFromDataEntry(OnlineReq onlineReq) {

		Map<String, String> resultMapList = new HashMap<>();
		String caseNumber = onlineReq.getMetadata().getTxLabel() != null ? onlineReq.getMetadata().getTxLabel() : "";

		if (!StringUtils.isEmpty(caseNumber)) {
			String dataEntryURL = l3DataEntryURL + caseNumber;
			String dataEntryResponse = onlineApiService.getRequestFromL3(dataEntryURL);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonNode objectNode = mapper.createObjectNode();

			if (dataEntryResponse != null) {
				try {
					objectNode = mapper.readTree(dataEntryResponse);
				} catch (JsonProcessingException e) {
					logger.error("Error : {}", e.getMessage());
					e.printStackTrace();
				}
			}

			JsonNode responseNode = objectNode.has("response") ? objectNode.get("response") : mapper.createObjectNode();
			JsonNode deDataNode = responseNode.has("deData") ? responseNode.get("deData") : mapper.createObjectNode();
			JsonNode personalDetailsNode = deDataNode.has("personaldetailsasperbvf")
					? deDataNode.get("personaldetailsasperbvf")
					: mapper.createObjectNode();
			JsonNode passportNode = deDataNode.has("passportasperdocument") ? deDataNode.get("passportasperdocument")
					: mapper.createObjectNode();
			JsonNode panCardNode = deDataNode.has("pancardasperdocument") ? deDataNode.get("pancardasperdocument")
					: mapper.createObjectNode();
			JsonNode dlNode = deDataNode.has("drivinglicenseasperdocument")
					? deDataNode.get("drivinglicenseasperdocument")
					: mapper.createObjectNode();
			JsonNode voterIdNode = deDataNode.has("voteridasperdocument") ? deDataNode.get("voteridasperdocument")
					: mapper.createObjectNode();

			String primaryName = personalDetailsNode.has("candidatename")
					? personalDetailsNode.get("candidatename").asText()
					: "";
			if (StringUtils.isEmpty(primaryName)) {
				if (personalDetailsNode.has("middlename")
						&& StringUtils.isNotEmpty(personalDetailsNode.get("middlename").asText())) {
					primaryName = personalDetailsNode.get("firstname").asText()
							+ personalDetailsNode.get("middlename").asText()
							+ personalDetailsNode.get("lastname").asText();
				} else {
					primaryName = personalDetailsNode.get("firstname").asText()
							+ personalDetailsNode.get("lastname").asText();
				}
			}
			primaryName = primaryName.replaceAll("  ", " ");
			String fathersName = personalDetailsNode.has("fathersname")
					? personalDetailsNode.get("fathersname").asText()
					: "";
			String dob = personalDetailsNode.has("dob") ? personalDetailsNode.get("dob").asText() : "";
			String passportName = passportNode.has("candidatename") ? passportNode.get("candidatename").asText() : "";
			String panName = panCardNode.has("nameasperpancard") ? panCardNode.get("nameasperpancard").asText() : "";
			String dlName = dlNode.has("candidatename") ? dlNode.get("candidatename").asText() : "";
			String voterIdName = voterIdNode.has("candidatename") ? voterIdNode.get("candidatename").asText() : "";

			String secondaryName = getSecondaryName(primaryName, passportName, panName, dlName, voterIdName);
			String address = "";

			resultMapList.put("primaryName", primaryName);
			resultMapList.put("secondaryName", secondaryName);
			resultMapList.put("dob", dob);
			resultMapList.put("fathersName", fathersName);
			resultMapList.put("address", address);
		}

		return resultMapList;
	}

	@Override
	public String getSecondaryName(String primaryName, String passportName, String panName, String dlName,
			String voterIdName) {

		String secondaryName = "";
		String educationName = "";

		String[] primaryNameList = primaryName.split(" ");
		boolean passportflag = true;
		boolean panflag = true;
		boolean dlflag = true;
		boolean voterflag = true;
		boolean educationflag = true;
		for (int idx = 0; idx < primaryNameList.length; idx++) {
			if (!passportName.contains(primaryNameList[idx])) {
				passportflag = false;
			}
			if (!panName.contains(primaryNameList[idx])) {
				panflag = false;
			}
			if (!dlName.contains(primaryNameList[idx])) {
				dlflag = false;
			}
			if (!voterIdName.contains(primaryNameList[idx])) {
				voterflag = false;
			}
			if (!educationName.contains(primaryNameList[idx])) {
				educationflag = false;
			}
		}
		int passportLength = passportflag ? passportName.trim().length() : 0;
		int panLength = panflag ? panName.trim().length() : 0;
		int dlLength = dlflag ? dlName.trim().length() : 0;
		int voterIdLength = voterflag ? voterIdName.trim().length() : 0;
		int educationLength = educationflag ? educationName.trim().length() : 0;

		int maxVal = Stream.of(passportLength, panLength, dlLength, voterIdLength, educationLength)
				.max(Integer::compareTo).get();

		if (passportLength == maxVal) {
			secondaryName = passportName;
		} else if (panLength == maxVal) {
			secondaryName = panName;
		} else if (dlLength == maxVal) {
			secondaryName = dlName;
		} else if (voterIdLength == maxVal) {
			secondaryName = voterIdName;
		} else if (educationLength == maxVal) {
			secondaryName = educationName;
		}
		return secondaryName;
	}

	@Override
	public ApiServiceResultPOJO getApiServiceNames(ObjectMapper mapper, OnlineReq onlineReq)
			throws JsonProcessingException {

		List<String> serviceNameList = new ArrayList<>();
		Map<String, String> resultServiceMap = new HashMap<>();

		ApiServiceResultPOJO apiServiceResultPOJO = new ApiServiceResultPOJO();
		apiServiceResultPOJO.setCheckIdList(new ArrayList<>());
		apiServiceResultPOJO.setServiceNameList(new ArrayList<>());
		apiServiceResultPOJO.setResultServiceMap(new HashMap<>());
		apiServiceResultPOJO.setServiceResponseMap(new HashMap<>());
		apiServiceResultPOJO.setDataEntryMap(new HashMap<>());
		apiServiceResultPOJO.setOnlineVerificationChecksList(new ArrayList<>());

		for (Datum data : onlineReq.getData()) {
			List<ComponentScoping> componentScopingList = data.getTaskSpecs().getComponentScoping();
			for (ComponentScoping componentScoping : componentScopingList) {

//				try {
//					apiServiceResultPOJO = saveCaseSpecificInfo(mapper, componentScoping, apiServiceResultPOJO);
//
//					if (apiServiceResultPOJO.getCaseSpecificId() == 0) {
//						return apiServiceResultPOJO;
//					}
//				} catch (JsonProcessingException e) {
//					logger.error("Exception : {}", e.getMessage());
//					return apiServiceResultPOJO;
//				}

				for (Component component : componentScoping.getComponents()) {
					if (StringUtils.equalsIgnoreCase(component.getComponentname(), "Database")) {
						String productName = component.getPRODUCT() != null ? component.getPRODUCT() : "";
						String componentName = component.getComponentname();

						apiServiceResultPOJO.setComponentName(componentName);
						apiServiceResultPOJO.setProductName(productName);

						ObjectNode objectNode = mapper.createObjectNode();
						objectNode.put("productName", productName);
						objectNode.put("componentName", componentName);
						String objectNodeStr = mapper.writeValueAsString(objectNode);

						String serviceName = onlineApiService.sendDataToLocalRest(componentWiseApiUrl, objectNodeStr);

						processRecordsForMI(componentScoping, component);

						if (serviceName != null && !StringUtils.isEmpty(serviceName)) {
							resultServiceMap.put(productName, serviceName);
							if (serviceName.contains(",")) {
								serviceNameList.addAll(Arrays.asList(serviceName.split(",")));
							} else {
								serviceNameList.add(serviceName);
							}
						}

					}
				}
			}
		}

		serviceNameList = new ArrayList<>(new HashSet<>(serviceNameList));
		apiServiceResultPOJO.setServiceNameList(serviceNameList);
		apiServiceResultPOJO.setResultServiceMap(resultServiceMap);

		return apiServiceResultPOJO;
	}

	private void processRecordsForMI(ComponentScoping componentScoping, Component component) {
		for (JsonNode recordNode : component.getRecords()) {
			String miValue = recordNode.has("MI") ? recordNode.get("MI").asText() : "";

			if (StringUtils.equalsIgnoreCase(miValue, "Yes")) {
				try {
					sendDataToL3Mi(recordNode, componentScoping, component);
				} catch (JsonProcessingException e) {
					logger.error("Error : {}", e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public ApiServiceResultPOJO processApiService(ObjectMapper mapper, String serviceName,
			Map<String, String> resultMap, ApiServiceResultPOJO apiServiceResultPOJO) {

		Map<String, String> serviceResponseMap = apiServiceResultPOJO.getServiceResponseMap() != null
				? apiServiceResultPOJO.getServiceResponseMap()
				: new HashMap<>();
		String dinStr = "";
		if (StringUtils.equalsIgnoreCase(serviceName, "Worldcheck")
				|| StringUtils.equalsIgnoreCase(serviceName, "Watchout")) {
			String mcaResponse = serviceResponseMap.get("MCA");
			mcaResponse = mcaResponse != null ? mcaResponse : "{}";

			try {
				JsonNode responseNode = mapper.readTree(mcaResponse);
				JsonNode mcaResponseNode = responseNode.has("MCA") ? responseNode.get("MCA")
						: mapper.createObjectNode();
				dinStr = mcaResponseNode.has("din") ? mcaResponseNode.get("din").asText() : "";
			} catch (JsonProcessingException e) {
				logger.error("Exception : {}", e.getMessage());
				e.printStackTrace();
			}

		}

		if (StringUtils.equalsIgnoreCase(serviceName, "MCA")) {
			String mcaResponse = processMCAAPIRecordWise(mapper, resultMap);
			serviceResponseMap.put("MCA", mcaResponse);
		}
		if (StringUtils.equalsIgnoreCase(serviceName, "Worldcheck")) {
			serviceResponseMap.put("Worldcheck", processWorldCheckAPIRecordWise(mapper, resultMap, dinStr));
		}
		if (StringUtils.equalsIgnoreCase(serviceName, "Adverse Media")
				|| StringUtils.equalsIgnoreCase(serviceName, "Google")) {
			serviceResponseMap.put("Adverse Media", processAdverseAPIRecordWise(mapper, resultMap));
		}
		if (StringUtils.equalsIgnoreCase(serviceName, "Manupatra")) {
			try {
				serviceResponseMap.put("Manupatra",
						processManupatraAPIRecordWise(mapper, resultMap, apiServiceResultPOJO));
			} catch (JsonProcessingException e) {
				logger.error("Exception : {}", e.getMessage());
				e.printStackTrace();
			}
		}
		if (StringUtils.equalsIgnoreCase(serviceName, "Loan Defaulter (Cibil)")
				|| StringUtils.equalsIgnoreCase(serviceName, "Loan Defaulter")) {
			serviceResponseMap.put("Loan Defaulter (Cibil)", processLoanAPIRecordWise(mapper, resultMap));
		}
		if (StringUtils.equalsIgnoreCase(serviceName, "Watchout")) {
			serviceResponseMap.put("Watchout", processWatchoutAPIRecordWise(mapper, resultMap, dinStr));
		}
		apiServiceResultPOJO.setServiceResponseMap(serviceResponseMap);

		return apiServiceResultPOJO;
	}

	private String processMCAAPIRecordWise(ObjectMapper mapper, Map<String, String> resultMap) {

		ObjectNode personalInfo = mapper.createObjectNode();
		personalInfo.put("name", resultMap.get("primaryName"));
		personalInfo.put("dob", resultMap.get("dob"));
		personalInfo.put("father_name", resultMap.get("fathersName"));

		String response = "";

		try {
			response = onlineApiService.sendDataToRestUrl(mcaRestUrl, personalInfo.toString());
		} catch (Exception e) {
			logger.info("Exception : {}", e.getMessage());
		}

		return response;
	}

	private String processLoanAPIRecordWise(ObjectMapper mapper, Map<String, String> resultMap) {

		ObjectNode personalInfo = mapper.createObjectNode();
		personalInfo.put("name", resultMap.get("primaryName"));
		personalInfo.put("dob", resultMap.get("dob"));
		personalInfo.put("father_name", resultMap.get("fathersName"));

		String response = "";

		try {
			response = onlineApiService.sendDataToRestUrl(loanRestUrl, personalInfo.toString());
		} catch (Exception e) {
			logger.info("Exception : {}", e.getMessage());
		}

		return response;
	}

	private String processAdverseAPIRecordWise(ObjectMapper mapper, Map<String, String> resultMap) {

		ObjectNode personalInfo = mapper.createObjectNode();
		personalInfo.put("name", resultMap.get("primaryName"));
		personalInfo.put("dob", resultMap.get("dob"));
		personalInfo.put("father_name", resultMap.get("fathersName"));

		String response = "";

		try {
			response = onlineApiService.sendDataToRestUrl(adversemediaRestUrl, personalInfo.toString());
		} catch (Exception e) {
			logger.info("Exception : {}", e.getMessage());
		}

		return response;
	}

	private String processManupatraAPIRecordWise(ObjectMapper mapper, Map<String, String> resultMap,
			ApiServiceResultPOJO apiServiceResultPOJO) throws JsonProcessingException {

		ObjectNode personalInfo = mapper.createObjectNode();
		String contexts = "Accused,Rape,Arrested,Fake,Fraud,Convicted,Assualt,"
				+ "Molested,Assassinated,FleshTrade,Burglary,SexRacket,Hawala,"
				+ "Betting,Smuggling,Pimp,Abeyance,Sabotage,Abetted,Racket,Criminal"
				+ ",Crime,Detained,Robbery,Forgery,Mortgage,Terrorist,Duped,Ban,"
				+ "Debarred,Wanted,Bankrupt,Summon,Bribery,Corruption,Trafficking,"
				+ "Hijack,Arson,Counterfeit,Piracy,CiberCrime,Hacking,Phishing,"
				+ "Extortion,Moneylaundering,Slavery,Manipulation,Stole,Snatch,"
				+ "Theft,Kidnapping,Hostage,Ransacke,Sack,SexualExploitation,"
				+ "WarCrimes,DistributionofFakeMedicine,Sentenced,AviationCrime,"
				+ "Mugshot,Stab,Harrasment,Fight,Quarrel,Disqalified,Foundguilty,"
				+ "HeldResponsible,Dispute,Alleged,Disqualifieddirector,Duping,"
				+ "Attacked,ChainSnatchers,Custody,Chitfundscam,illegal,"
				+ "legalproceedings,Narcotics ,nabbed,apprehend,gruesome,destroy,"
				+ "rapist,abduct,shoplifting,kingpin,contemnor,bootlegger,ambush,"
				+ "Cheat,cold-heartedmurder,Corpse,brutal,grive,abrasion,regret,"
				+ "unfortunateevent,gash,AircraftHijack,ArmsTrafficking,Cybercrime,"
				+ "Embezzlement,TaxFraud,TaxEvasion,IllicitTraffickinginstolengoods,"
				+ "InsiderTrading&MarketManipulation,MigrantSmuggling," + "IndecentAssault,Narcotics Trafficking,"
				+ "Pharmaceuticalproducttrafficking,Banned/FakeMedicines,"
				+ "IllegalProduction&Distribution(Narcotics/pharma/armsetc),"
				+ "SecuritiesFraud,Piracy(Sea),IllegalCartelInformation,PriceFixing,"
				+ "Antitrustviolations,LawViolations,Racketeering,Terrorism,"
				+ "TerrorFinancing,WantsandWarrants,parole,Probation,offender,"
				+ "Sexoffender,Committingacrime,jailed,inprison,behindthebars,"
				+ "firearm,drugs,alcohol/liquor,passjudgement,imprisonment,"
				+ "drugspeddler,impostor,Swindle,dacoit,deceive,manhandle,booked,"
				+ "Blacklisted ,trick,auto-lifter,unlawful,Illegaltrader,"
				+ "misappropriation,stealing,thief,larceny,anti-corruption,"
				+ "anti-moneylaundering,prosecute,charged,investigation,suspected,"
				+ "offense,fines,penalty,compensation,damages,sue,insolvent,"
				+ "borrower,impersonate,prostitution,brothel,infringement,"
				+ "iniquitous,breach,debauchery,dishonest,illegality,Battery,"
				+ "Homicide,manslaughter,misdemeanor,misfeasance,Felony,"
				+ "infractions,whitecollarcrime,vandalism,shoplifting,violence,"
				+ "treason,publicintoxication,trespass,disorderlyconduct,"
				+ "recklessdriving,drink&drive,hit&run,parkingornoiseviolations,"
				+ "buildingcodeviolations,littering,habitualtrafficoffender,"
				+ "Publicdrunkenness,workingunlicensed,drug-dealing,"
				+ "Succomedtoinjury,Lure,implead,hire,Posing,demanding,anonymus," + "steroid,cannabies,weed";
		personalInfo.put("name", resultMap.get("primaryName"));
		personalInfo.put("dob", resultMap.get("dob"));
		personalInfo.put("father_name", resultMap.get("fathersName"));
		personalInfo.put("address", resultMap.get("address"));
		personalInfo.put("contexts", contexts);

		String primaryResponse = "";
		String secondaryResponse = "";

		ObjectNode objectNode = mapper.createObjectNode();
		try {
			primaryResponse = onlineApiService.sendDataToRestUrl(manupatraRestUrl, personalInfo.toString());
			objectNode.put("Primary result", primaryResponse);
		} catch (Exception e) {
			logger.info("Exception : {}", e.getMessage());
		}

		if (!StringUtils.isEmpty(resultMap.get("secondaryName"))) {
			personalInfo.put("name", resultMap.get("secondaryName"));
			try {
				secondaryResponse = onlineApiService.sendDataToRestUrl(manupatraRestUrl, personalInfo.toString());
				objectNode.put("Secondary result", secondaryResponse);
			} catch (Exception e) {
				logger.info("Exception : {}", e.getMessage());
			}
		} else {
			objectNode.put("Secondary result", secondaryResponse);
		}
		apiServiceResultPOJO.setManuPatraResponsePrimary(primaryResponse);
		apiServiceResultPOJO.setManuPatraResponseSecondary(secondaryResponse);

		return mapper.writeValueAsString(objectNode);
	}

	private String processWatchoutAPIRecordWise(ObjectMapper mapper, Map<String, String> resultMap, String dinStr) {

		ObjectNode personalInfo = mapper.createObjectNode();
		personalInfo.put("name", resultMap.get("primaryName"));
		personalInfo.put("dob", resultMap.get("dob"));
		personalInfo.put("father_name", resultMap.get("fathersName"));
		if (dinStr != null && !StringUtils.isEmpty(dinStr)) {
			personalInfo.put("din", dinStr);
		}

		String response = "";

		try {
			response = onlineApiService.sendDataToRestUrl(watchoutRestUrl, personalInfo.toString());
		} catch (Exception e) {
			logger.info("Exception : {}", e.getMessage());
		}

		return response;
	}

	private String processWorldCheckAPIRecordWise(ObjectMapper mapper, Map<String, String> resultMap, String dinStr) {

		ObjectNode personalInfo = mapper.createObjectNode();
		personalInfo.put("name", resultMap.get("primaryName"));
		String formattedDob=Utility.formatDateUtil(resultMap.get("dob"));
		//personalInfo.put("dob", resultMap.get("dob"));
		personalInfo.put("dob", formattedDob);
		personalInfo.put("father_name", resultMap.get("fathersName"));
		personalInfo.put("address", resultMap.get("address"));
		if (dinStr != null && !StringUtils.isEmpty(dinStr)) {
			personalInfo.put("din", dinStr);
		}

		String response = "";

		try {
			response = onlineApiService.sendDataToRestUrl(worldcheckRestUrl, personalInfo.toString());
		} catch (Exception e) {
			logger.info("Exception : {}", e.getMessage());
		}

		return response;
	}

	@Override
	public JsonNode writeRecordResultMap(ObjectMapper mapper, OnlineReq onlineReq,
			ApiServiceResultPOJO apiServiceResultPOJO) {

		Map<String, String> resultServiceMap = apiServiceResultPOJO.getResultServiceMap() != null
				? apiServiceResultPOJO.getResultServiceMap()
				: new HashMap<>();
		Map<String, String> serviceResponseMap = apiServiceResultPOJO.getServiceResponseMap() != null
				? apiServiceResultPOJO.getServiceResponseMap()
				: new HashMap<>();
		List<Datum> dataList = new ArrayList<>();

		ComponentScoping componentScopingTmp = new ComponentScoping();
		for (Datum data : onlineReq.getData()) {
			List<ComponentScoping> componentScopingList = new ArrayList<>();
			for (ComponentScoping componentScoping : data.getTaskSpecs().getComponentScoping()) {
				componentScopingTmp = componentScoping;
				List<Component> componentList = new ArrayList<>();
				for (Component component : componentScoping.getComponents()) {
					if (StringUtils.equalsIgnoreCase(component.getComponentname(), "Database")) {
						for (Entry<String, String> entrySet : resultServiceMap.entrySet()) {
							if (StringUtils.equalsIgnoreCase(entrySet.getKey(), component.getPRODUCT())) {
								List<JsonNode> recordNodes = component.getRecords();
								List<JsonNode> newRecordNodes = new ArrayList<>();
								for (JsonNode recordNode : recordNodes) {
									String checkId = recordNode.get("checkId") != null
											? recordNode.get("checkId").asText()
											: "";
									apiServiceResultPOJO.setProductName(component.getPRODUCT());
									apiServiceResultPOJO.setComponentName(component.getComponentname());
//									try {
//										saveIntiatedAttempt(mapper, apiServiceResultPOJO, checkId);
//									} catch (JsonProcessingException e) {
//										logger.error("Exception : {}", e.getMessage());
//									}

									ArrayNode ruleResult = recordNode.has("ruleResult")
											? (ArrayNode) recordNode.get("ruleResult")
											: mapper.createArrayNode();
									ObjectNode ruleResultObject = mapper.createObjectNode();
									ruleResultObject.put("engine", "onlineResult");
									ruleResultObject.put("success", true);
									ruleResultObject.put("message", "SUCCEEDED");
									ruleResultObject.put("status", 200);
									ObjectNode finalResult = mapper.createObjectNode();

									if (entrySet.getValue().contains(",")) {
										String[] entrySetApis = entrySet.getValue().split(",");
										for (String entryValue : entrySetApis) {
											finalResult.put(entryValue.trim(),
													serviceResponseMap.get(entryValue.trim()));

											try {
												if (StringUtils.equalsIgnoreCase(entryValue.trim(), "Manupatra")) {
													setManupatraResponse(mapper, checkId, apiServiceResultPOJO);
												} else {
													setApiServiceResponse(mapper, apiServiceResultPOJO,
															serviceResponseMap.get(entryValue.trim()), checkId,
															entryValue.trim());
												}
											} catch (JsonProcessingException e) {
												logger.error("Exception : {}", e.getMessage());
											}
										}
									} else {
										finalResult.put(entrySet.getValue().trim(),
												serviceResponseMap.get(entrySet.getValue().trim()));
										try {
											if (StringUtils.equalsIgnoreCase(entrySet.getValue().trim(), "Manupatra")) {
												setManupatraResponse(mapper, checkId, apiServiceResultPOJO);
											} else {
												setApiServiceResponse(mapper, apiServiceResultPOJO,
														serviceResponseMap.get(entrySet.getValue().trim()), checkId,
														entrySet.getValue().trim());
											}
										} catch (JsonProcessingException e) {
											logger.error("Exception : {}", e.getMessage());
										}
									}

									ruleResultObject.set("result", finalResult);
									ruleResult.add(ruleResultObject);

									((ObjectNode) recordNode).set("ruleResult", ruleResult);
									newRecordNodes.add(recordNode);
								}
								component.setRecords(newRecordNodes);
							}
						}
					}
					componentList.add(component);
				}
				componentScoping.setComponents(componentList);
				componentScopingList.add(componentScoping);
			}
			data.getTaskSpecs().setComponentScoping(componentScopingList);
			dataList.add(data);
		}

		onlineReq.setData(dataList);
//		saveOnlineVerifyCheck(mapper, componentScopingTmp, apiServiceResultPOJO);

		List<String> checkIdList = apiServiceResultPOJO.getCheckIdList();
		checkIdList = new ArrayList<>(new HashSet<>(checkIdList));

		onlineApiService.sendDataToL3ByCheckId(verificationStatusUrlL3, checkIdList);
		return mapper.convertValue(onlineReq, JsonNode.class);
	}

	private ApiServiceResultPOJO saveCaseSpecificInfo(ObjectMapper mapper, ComponentScoping componentScoping,
			ApiServiceResultPOJO apiServiceResultPOJO) throws JsonProcessingException {
		CaseSpecificInfoPOJO caseSpecificInfo = new CaseSpecificInfoPOJO();
		caseSpecificInfo.setCandidateName(componentScoping.getCandidate_Name());
		caseSpecificInfo.setCaseDetails(componentScoping.getCaseDetails().toString());
		caseSpecificInfo.setCaseMoreInfo(componentScoping.getCaseMoreInfo().toString());
		caseSpecificInfo.setCaseReference(componentScoping.getCaseReference().toString());
		caseSpecificInfo.setCaseRefNumber(componentScoping.getCASE_REF_NUMBER());
		caseSpecificInfo.setCaseNumber(componentScoping.getCASE_NUMBER());
		caseSpecificInfo.setClientCode(componentScoping.getCLIENT_CODE());
		caseSpecificInfo.setClientName(componentScoping.getCLIENT_NAME());
		caseSpecificInfo.setSbuName(componentScoping.getSBU_NAME());
		caseSpecificInfo.setPackageName(componentScoping.getPackageName());
		caseSpecificInfo.setClientSpecificFields(componentScoping.getClientSpecificFields().toString());

		JsonNode caseSpecificInfoNode = mapper.convertValue(caseSpecificInfo, JsonNode.class);
		String caseSpecificInfoStr = mapper.writeValueAsString(caseSpecificInfoNode);

		String caseSpecificResponse = onlineApiService.sendDataToAttempt(caseSpecificInfoUrl, caseSpecificInfoStr);
		CaseSpecificInfoPOJO caseSpecificInfoPojo = new CaseSpecificInfoPOJO();
		if (caseSpecificResponse != null) {
			JsonNode caseSpecificResponseNode = mapper.readTree(caseSpecificResponse);
			caseSpecificInfoPojo = mapper.treeToValue(caseSpecificResponseNode, CaseSpecificInfoPOJO.class);
		}

		apiServiceResultPOJO.setCaseNumber(componentScoping.getCASE_NUMBER());
		apiServiceResultPOJO
				.setCaseSpecificId(caseSpecificInfoPojo != null ? caseSpecificInfoPojo.getCaseSpecificId() : 0);
		return apiServiceResultPOJO;
	}

	private void setApiServiceResponse(ObjectMapper mapper, ApiServiceResultPOJO apiServiceResultPOJO,
			String apiResponse, String checkId, String apiName) throws JsonProcessingException {

		List<OnlineVerificationChecks> onlineVerificationChecksList = apiServiceResultPOJO
				.getOnlineVerificationChecksList();
		OnlineVerificationChecks onlineVerificationChecks = new OnlineVerificationChecks();

		if (apiResponse != null && !StringUtils.isEmpty(apiResponse)) {
			JsonNode apiResponseNode = mapper.readValue(apiResponse, JsonNode.class);

			if (StringUtils.isNotEmpty(checkId)) {
				List<String> checkIdList = apiServiceResultPOJO.getCheckIdList();
				checkIdList.add(checkId);
				apiServiceResultPOJO.setCheckIdList(checkIdList);

				onlineVerificationChecks.setCheckId(checkId);
			}

			String apiOutput = "";
			String rawOutput = "";
			String apiInput = "";
			String finalStatus = "";

			if (StringUtils.equalsIgnoreCase(apiName, "MCA")) {
				JsonNode mcaResponseNode = apiResponseNode.has("MCA") ? apiResponseNode.get("MCA")
						: mapper.createObjectNode();
				apiOutput = mcaResponseNode.has("MCA Output") ? mcaResponseNode.get("MCA Output").asText() : "";
				rawOutput = mcaResponseNode.has("Raw Output") ? mcaResponseNode.get("Raw Output").asText() : "";
				apiInput = mcaResponseNode.has("MCA Input") ? mcaResponseNode.get("MCA Input").asText() : "";
				finalStatus = mcaResponseNode.has("status") ? mcaResponseNode.get("status").asText() : "";
				apiServiceResultPOJO.setApiName("MCA");
				onlineVerificationChecks.setApiName("MCA");

				String verifyId = mcaResponseNode.has("Verify_id") ? mcaResponseNode.get("Verify_id").asText() : "";
				onlineVerificationChecks.setVerifyId(verifyId);
			}
			if (StringUtils.equalsIgnoreCase(apiName, "WorldCheck")) {
				JsonNode worldResponseNode = apiResponseNode.has("WorldCheck") ? apiResponseNode.get("WorldCheck")
						: mapper.createObjectNode();
				apiOutput = worldResponseNode.has("World Check Output")
						? worldResponseNode.get("World Check Output").asText()
						: "";
				rawOutput = worldResponseNode.has("Raw Output") ? worldResponseNode.get("Raw Output").asText() : "";
				apiInput = worldResponseNode.has("World Check Input")
						? worldResponseNode.get("World Check Input").asText()
						: "";
				finalStatus = worldResponseNode.has("status") ? worldResponseNode.get("status").asText() : "";
				apiServiceResultPOJO.setApiName("World Check");
				onlineVerificationChecks.setApiName("World Check");
			}
			if (StringUtils.equalsIgnoreCase(apiName, "Adverse Media")
					|| StringUtils.equalsIgnoreCase(apiName, "Google")) {
				JsonNode adverseResponseNode = apiResponseNode.has("Adverse Media")
						? apiResponseNode.get("Adverse Media")
						: mapper.createObjectNode();
				apiOutput = adverseResponseNode.has("Adverse media Output")
						? adverseResponseNode.get("Adverse media Output").asText()
						: "";
				rawOutput = adverseResponseNode.has("Raw Output") ? adverseResponseNode.get("Raw Output").asText() : "";
				apiInput = adverseResponseNode.has("Adverse media Input")
						? adverseResponseNode.get("Adverse media Input").asText()
						: "";
				finalStatus = adverseResponseNode.has("status") ? adverseResponseNode.get("status").asText() : "";
				apiServiceResultPOJO.setApiName("Adverse media");
				onlineVerificationChecks.setApiName("Adverse media");

				String verifyId = adverseResponseNode.has("Verify_id") ? adverseResponseNode.get("Verify_id").asText()
						: "";
				onlineVerificationChecks.setVerifyId(verifyId);
			}
			if (StringUtils.equalsIgnoreCase(apiName, "Loan Defaulter (Cibil)")) {
				JsonNode loanResponseNode = apiResponseNode.has("Loan Defaulter")
						? apiResponseNode.get("Loan Defaulter")
						: mapper.createObjectNode();
				apiOutput = loanResponseNode.has("Loan Defaulter Output")
						? loanResponseNode.get("Loan Defaulter Output").asText()
						: "";
				rawOutput = loanResponseNode.has("Raw Output") ? loanResponseNode.get("Raw Output").asText() : "";
				apiInput = loanResponseNode.has("Loan Defaulter Input")
						? loanResponseNode.get("Loan Defaulter Input").asText()
						: "";
				finalStatus = loanResponseNode.has("status") ? loanResponseNode.get("status").asText() : "";
				apiServiceResultPOJO.setApiName("Loan Defaulter");
				onlineVerificationChecks.setApiName("Loan Defaulter");

				String verifyId = loanResponseNode.has("Verify_id") ? loanResponseNode.get("Verify_id").asText() : "";
				onlineVerificationChecks.setVerifyId(verifyId);
			}
			if (StringUtils.equalsIgnoreCase(apiName, "WatchOut")) {
				JsonNode watchOutResponseNode = apiResponseNode.has("WatchOut") ? apiResponseNode.get("WatchOut")
						: mapper.createObjectNode();
				apiOutput = watchOutResponseNode.has("WatchOut Output")
						? watchOutResponseNode.get("WatchOut Output").asText()
						: "";
				rawOutput = watchOutResponseNode.has("Raw Output") ? watchOutResponseNode.get("Raw Output").asText()
						: "";
				apiInput = watchOutResponseNode.has("WatchOut Input")
						? watchOutResponseNode.get("WatchOut Input").asText()
						: "";
				finalStatus = watchOutResponseNode.has("status") ? watchOutResponseNode.get("status").asText() : "";
				apiServiceResultPOJO.setApiName("Watchout");
				onlineVerificationChecks.setApiName("Watchout");
			}

			if (!StringUtils.equalsAnyIgnoreCase(finalStatus, "Manual", "Clear", "Record Found")) {
				finalStatus = "Pending";
			}

			onlineVerificationChecks.setInitialResult(finalStatus);
			onlineVerificationChecks.setResult(finalStatus);
			onlineVerificationChecks.setMatchedIdentifiers(apiOutput);
			onlineVerificationChecks.setOutputFile(rawOutput);
			onlineVerificationChecks.setInputFile(apiInput);
			onlineVerificationChecks.setCreatedDate(new Date().toString());
			onlineVerificationChecks.setUpdatedDate(new Date().toString());

			onlineVerificationChecksList.add(onlineVerificationChecks);
			apiServiceResultPOJO.setOnlineVerificationChecksList(onlineVerificationChecksList);

//			if (StringUtils.equalsIgnoreCase(finalStatus, "clear")) {
//				saveClearOutcomeAttempt(mapper, apiServiceResultPOJO, checkId);
//			} else if (StringUtils.equalsIgnoreCase(finalStatus, "Record Found")) {
//				saveDiscrepantProcessAttempt(mapper, apiServiceResultPOJO, checkId);
//			} else if (StringUtils.equalsIgnoreCase(finalStatus, "Manual")) {
//				saveReviewProcessAttempt(mapper, apiServiceResultPOJO, checkId);
//			}
		}
	}

	private void setManupatraResponse(ObjectMapper mapper, String checkId, ApiServiceResultPOJO apiServiceResultPOJO)
			throws JsonProcessingException {

		String manuPatraResponsePrimary = apiServiceResultPOJO.getManuPatraResponsePrimary();
		String manuPatraResponseSecondary = apiServiceResultPOJO.getManuPatraResponseSecondary();

		String primaryStatus = "";
		String primaryManupatraOutput = "";
		String primaryRawOutput = "";
		String primaryManupatraInput = "";
		String primaryKeyName = "";

		String secondaryStatus = "NA";
		String secondaryManupatraOutput = "";
		String secondaryRawOutput = "";
		String secondaryManupatraInput = "";
		String secondaryKeyName = "";

		if (manuPatraResponsePrimary != null) {
			JsonNode personalResponseNode = mapper.readTree(manuPatraResponsePrimary);

			if (personalResponseNode != null && personalResponseNode.has("Manupatra")) {
				JsonNode manupatraNode = personalResponseNode.get("Manupatra");

				if (manupatraNode != null && manupatraNode.has("status")) {

					primaryStatus = manupatraNode.get("status").asText();
					primaryManupatraOutput = manupatraNode.has("ManuPatra Output")
							? manupatraNode.get("ManuPatra Output").asText()
							: "";
					primaryRawOutput = manupatraNode.has("Raw Output") ? manupatraNode.get("Raw Output").asText() : "";
					primaryManupatraInput = manupatraNode.has("ManuPatra Input")
							? manupatraNode.get("ManuPatra Input").asText()
							: "";
					primaryKeyName = manupatraNode.has("Matched Key Name")
							? manupatraNode.get("Matched Key Name").asText()
							: "";
				}
			}
		}

		if (manuPatraResponseSecondary != null) {
			JsonNode personalResponseNode = mapper.readTree(manuPatraResponseSecondary);

			if (personalResponseNode != null && personalResponseNode.has("Manupatra")) {
				JsonNode manupatraNode = personalResponseNode.get("Manupatra");

				if (manupatraNode != null && manupatraNode.has("status")) {

					secondaryStatus = manupatraNode.get("status").asText();
					secondaryManupatraOutput = manupatraNode.has("ManuPatra Output")
							? manupatraNode.get("ManuPatra Output").asText()
							: "";
					secondaryRawOutput = manupatraNode.has("Raw Output") ? manupatraNode.get("Raw Output").asText()
							: "";
					secondaryManupatraInput = manupatraNode.has("ManuPatra Input")
							? manupatraNode.get("ManuPatra Input").asText()
							: "";
					secondaryKeyName = manupatraNode.has("Matched Key Name")
							? manupatraNode.get("Matched Key Name").asText()
							: "";
				}
			}
		}

		String finalStatus = onlineFinalService.getManupatraFinalStatus(primaryStatus, secondaryStatus);

		ObjectNode manupatraOutput = mapper.createObjectNode();
		manupatraOutput.put("primary", primaryManupatraOutput);
		manupatraOutput.put("secondary", secondaryManupatraOutput);

		ObjectNode rawOutput = mapper.createObjectNode();
		rawOutput.put("primary", primaryRawOutput);
		rawOutput.put("secondary", secondaryRawOutput);
		String rawOutputStr = mapper.writeValueAsString(rawOutput);

		ObjectNode manupatraInput = mapper.createObjectNode();
		manupatraInput.put("primary", primaryManupatraInput);
		manupatraInput.put("secondary", secondaryManupatraInput);
		String manupatraInputStr = mapper.writeValueAsString(manupatraInput);

		ObjectNode matchedKeyName = mapper.createObjectNode();
		matchedKeyName.put("primary", primaryKeyName);
		matchedKeyName.put("secondary", secondaryKeyName);
		String matchedKeyNameStr = mapper.writeValueAsString(matchedKeyName);
		if (StringUtils.isEmpty(primaryKeyName) && StringUtils.isEmpty(secondaryKeyName)) {
			matchedKeyNameStr = mapper.writeValueAsString(manupatraOutput);
		}

		List<OnlineVerificationChecks> onlineVerificationChecksList = apiServiceResultPOJO
				.getOnlineVerificationChecksList();
		OnlineVerificationChecks onlineVerificationChecks = new OnlineVerificationChecks();

		if (StringUtils.isNotEmpty(checkId)) {
			List<String> checkIdList = apiServiceResultPOJO.getCheckIdList();
			checkIdList.add(checkId);
			apiServiceResultPOJO.setCheckIdList(checkIdList);
			onlineVerificationChecks.setCheckId(checkId);
		}

		onlineVerificationChecks.setApiName("Manupatra");

		onlineVerificationChecks.setInitialResult(finalStatus);
		onlineVerificationChecks.setResult(finalStatus);

		onlineVerificationChecks.setOutputFile(rawOutputStr);
		onlineVerificationChecks.setInputFile(manupatraInputStr);
		onlineVerificationChecks.setMatchedIdentifiers(matchedKeyNameStr);

		onlineVerificationChecks.setCreatedDate(new Date().toString());
		onlineVerificationChecks.setUpdatedDate(new Date().toString());
		onlineVerificationChecksList.add(onlineVerificationChecks);

		logger.info("Size of List in Manupatra API : {}", onlineVerificationChecksList.size());

// 		Logic for saving Attempt
		apiServiceResultPOJO.setApiName("Manupatra");
		apiServiceResultPOJO.setOnlineVerificationChecksList(onlineVerificationChecksList);

//		if (StringUtils.equalsIgnoreCase(finalStatus, "clear")) {
//			saveClearOutcomeAttempt(mapper, apiServiceResultPOJO, checkId);
//		} else if (StringUtils.equalsIgnoreCase(finalStatus, "Record Found")) {
//			saveDiscrepantProcessAttempt(mapper, apiServiceResultPOJO, checkId);
//		} else if (StringUtils.equalsIgnoreCase(finalStatus, "Manual")) {
//			saveReviewProcessAttempt(mapper, apiServiceResultPOJO, checkId);
//		}
	}

	private void saveIntiatedAttempt(ObjectMapper mapper, ApiServiceResultPOJO apiServiceResultPOJO, String checkId)
			throws JsonProcessingException {
		if (StringUtils.isNotEmpty(checkId)) {

			CaseSpecificRecordDetailPOJO caseSpecificRecordDetail = new CaseSpecificRecordDetailPOJO();
			caseSpecificRecordDetail.setComponentName(apiServiceResultPOJO.getComponentName());
			caseSpecificRecordDetail.setProduct(apiServiceResultPOJO.getProductName());

			ObjectNode recordnode = mapper.createObjectNode();
			recordnode.put("record", "{}");
			caseSpecificRecordDetail.setComponentRecordField(recordnode.toString());
			caseSpecificRecordDetail.setCaseSpecificRecordStatus("Request Initiated");
			caseSpecificRecordDetail.setInstructionCheckId(checkId);
			caseSpecificRecordDetail.setCaseSpecificId(apiServiceResultPOJO.getCaseSpecificId());

			JsonNode caseSpecificRecordDetailNode = mapper.convertValue(caseSpecificRecordDetail, JsonNode.class);
			String caseSpecificRecordDetailStr = null;
			if (caseSpecificRecordDetailNode != null) {
				caseSpecificRecordDetailStr = onlineApiService
						.sendDataToCaseSpecificRecord(caseSpecificRecordDetailNode.toString());
			}
			CaseSpecificRecordDetailPOJO caseSpecificRecordDetail1 = new CaseSpecificRecordDetailPOJO();
			if (caseSpecificRecordDetailStr != null) {
				JsonNode caseSpecificRecordNode = mapper.readTree(caseSpecificRecordDetailStr);
				caseSpecificRecordDetail1 = mapper.treeToValue(caseSpecificRecordNode,
						CaseSpecificRecordDetailPOJO.class);
			}
			Long caseSpecificRecordId = null;// Treat as request ID for other like attempt history
			if (caseSpecificRecordDetail1 != null) {
				caseSpecificRecordId = caseSpecificRecordDetail1.getCaseSpecificDetailId();
			}
			apiServiceResultPOJO.setCaseSpecificRecordId(caseSpecificRecordId);

			AttemptHistory attemptHistory = new AttemptHistory();
			attemptHistory.setAttemptStatusid((long) 66);// (66, ' Request Initiated Via Integration -
															// ',
															// 'Valid', 3,
															// '2020-02-26
															// 13:28:06'),
			attemptHistory.setAttemptDescription("Request Initiated");
			attemptHistory.setName("Product Name:" + apiServiceResultPOJO.getProductName() + ", API Name:"
					+ apiServiceResultPOJO.getApiName());
			attemptHistory.setCheckid(checkId);

			AttemptStatusData attemptStatusData = new AttemptStatusData();
			attemptStatusData.setDepositionId((long) 13);
			attemptStatusData.setEndstatusId((long) 39);
			attemptStatusData.setModeId((long) 14);
			attemptHistory.setRequestid(caseSpecificRecordId);
			saveAttempt(mapper, attemptHistory, attemptStatusData);

			VerificationEventStatusPOJO verificationEventStatusPOJO = new VerificationEventStatusPOJO();
			verificationEventStatusPOJO.setCheckId(checkId);
			verificationEventStatusPOJO.setEventName("online");
			verificationEventStatusPOJO.setEventType("auto");
			verificationEventStatusPOJO.setCaseNo(apiServiceResultPOJO.getCaseNumber());
			verificationEventStatusPOJO.setUser("System");
			verificationEventStatusPOJO.setStatus("Request Initiated");
			verificationEventStatusPOJO.setRequestId(caseSpecificRecordId);
			String verificationEventStatusStr = mapper.writeValueAsString(verificationEventStatusPOJO);
			onlineApiService.sendDataToVerificationEventStatus(verifictioneventstatusRestUrl,
					verificationEventStatusStr);
		}
	}

	private void saveClearOutcomeAttempt(ObjectMapper mapper, ApiServiceResultPOJO apiServiceResultPOJO, String checkId)
			throws JsonProcessingException {
		if (StringUtils.isNotEmpty(checkId)) {

			AttemptHistory attemptHistory = new AttemptHistory();
			attemptHistory.setAttemptStatusid((long) 67);// (67, ' Auto Tagged - Clear -
															// ',
															// 'Valid', 3,
															// '2020-02-26
															// 13:28:06'),
			attemptHistory.setAttemptDescription("Result received with No records");
			attemptHistory.setName("Product Name:" + apiServiceResultPOJO.getProductName() + ", API Name:"
					+ apiServiceResultPOJO.getApiName());
			attemptHistory.setCheckid(checkId);
			attemptHistory.setRequestid(apiServiceResultPOJO.getCaseSpecificRecordId());

			AttemptStatusData attemptStatusData = new AttemptStatusData();
			attemptStatusData.setDepositionId((long) 3);
			attemptStatusData.setEndstatusId((long) 40); // Completed
			attemptStatusData.setModeId((long) 14);
			saveAttempt(mapper, attemptHistory, attemptStatusData);

			VerificationEventStatusPOJO verificationEventStatusPOJO = new VerificationEventStatusPOJO();
			verificationEventStatusPOJO.setCheckId(checkId);
			verificationEventStatusPOJO.setEventName("online");
			verificationEventStatusPOJO.setEventType("auto");
			verificationEventStatusPOJO.setCaseNo(apiServiceResultPOJO.getCaseNumber());
			verificationEventStatusPOJO.setUser("System");
			verificationEventStatusPOJO.setStatus("Result received with No records");
			verificationEventStatusPOJO.setRequestId(apiServiceResultPOJO.getCaseSpecificRecordId());
			String verificationEventStatusStr = mapper.writeValueAsString(verificationEventStatusPOJO);
			onlineApiService.sendDataToVerificationEventStatus(verifictioneventstatusRestUrl,
					verificationEventStatusStr);
		}
	}

	private void saveReviewProcessAttempt(ObjectMapper mapper, ApiServiceResultPOJO apiServiceResultPOJO,
			String checkId) throws JsonProcessingException {
		if (StringUtils.isNotEmpty(checkId)) {

			AttemptHistory attemptHistory = new AttemptHistory();
			attemptHistory.setAttemptStatusid((long) 68);// (68, ' Result Review - Manual -
															// ',
															// 'Valid', 3,
															// '2020-02-26
															// 13:28:06'),
			attemptHistory.setAttemptDescription("Result received to be Assigned");
			attemptHistory.setName("Product Name:" + apiServiceResultPOJO.getProductName() + ", API Name:"
					+ apiServiceResultPOJO.getApiName());
			attemptHistory.setCheckid(checkId);
			attemptHistory.setRequestid(apiServiceResultPOJO.getCaseSpecificRecordId());

			AttemptStatusData attemptStatusData = new AttemptStatusData();
			attemptStatusData.setDepositionId((long) 14); // Record Found
			attemptStatusData.setEndstatusId((long) 40); // Completed
			attemptStatusData.setModeId((long) 14); // Blank
			saveAttempt(mapper, attemptHistory, attemptStatusData);

			VerificationEventStatusPOJO verificationEventStatusPOJO = new VerificationEventStatusPOJO();
			verificationEventStatusPOJO.setCheckId(checkId);
			verificationEventStatusPOJO.setEventName("online");
			verificationEventStatusPOJO.setEventType("auto");
			verificationEventStatusPOJO.setCaseNo(apiServiceResultPOJO.getCaseNumber());
			verificationEventStatusPOJO.setUser("System");
			verificationEventStatusPOJO.setStatus("Result received to be Assigned");
			verificationEventStatusPOJO.setRequestId(apiServiceResultPOJO.getCaseSpecificRecordId());
			String verificationEventStatusStr = mapper.writeValueAsString(verificationEventStatusPOJO);
			onlineApiService.sendDataToVerificationEventStatus(verifictioneventstatusRestUrl,
					verificationEventStatusStr);
		}
	}

	private void saveDiscrepantProcessAttempt(ObjectMapper mapper, ApiServiceResultPOJO apiServiceResultPOJO,
			String checkId) throws JsonProcessingException {
		if (StringUtils.isNotEmpty(checkId)) {

			AttemptHistory attemptHistory = new AttemptHistory();
			attemptHistory.setAttemptStatusid((long) 69);// (68, ' Result Review - Manual -
															// ',
															// 'Valid', 3,
															// '2020-02-26
															// 13:28:06'),
			attemptHistory.setAttemptDescription("Record Found");
			attemptHistory.setName("Product Name:" + apiServiceResultPOJO.getProductName() + ", API Name:"
					+ apiServiceResultPOJO.getApiName());
			attemptHistory.setCheckid(checkId);
			attemptHistory.setRequestid(apiServiceResultPOJO.getCaseSpecificRecordId());
			attemptHistory.setExecutiveSummary("Record Found");
			AttemptStatusData attemptStatusData = new AttemptStatusData();
			attemptStatusData.setDepositionId((long) 14); // Record Found
			attemptStatusData.setEndstatusId((long) 40); // Completed
			attemptStatusData.setModeId((long) 14); // Blank
			saveAttempt(mapper, attemptHistory, attemptStatusData);

			VerificationEventStatusPOJO verificationEventStatusPOJO = new VerificationEventStatusPOJO();
			verificationEventStatusPOJO.setCheckId(checkId);
			verificationEventStatusPOJO.setEventName("online");
			verificationEventStatusPOJO.setEventType("auto");
			verificationEventStatusPOJO.setCaseNo(apiServiceResultPOJO.getCaseNumber());
			verificationEventStatusPOJO.setUser("System");
			verificationEventStatusPOJO.setStatus("Record Found");
			verificationEventStatusPOJO.setRequestId(apiServiceResultPOJO.getCaseSpecificRecordId());
			String verificationEventStatusStr = mapper.writeValueAsString(verificationEventStatusPOJO);
			onlineApiService.sendDataToVerificationEventStatus(verifictioneventstatusRestUrl,
					verificationEventStatusStr);
		}
	}

	private void saveAttempt(ObjectMapper mapper, AttemptHistory attemptHistory, AttemptStatusData attemptStatusData) {
		try {
			JsonNode attemptHistoryJsonNode = mapper.valueToTree(attemptHistory);
			logger.info("Value of attemptHistory bean to Json : {}", attemptHistoryJsonNode);
			String attemptHistoryStr = mapper.writeValueAsString(attemptHistoryJsonNode);
			logger.info("Value of attemptHistory Json to String : {}", attemptHistoryStr);

			String attemptHistoryResponse = onlineApiService.sendDataToAttempt(spocAttemptHistoryRestUrl,
					attemptHistoryStr);
			logger.info("Attempt History Response : {}", attemptHistoryResponse);
			AttemptHistory attemptHistoryNew = mapper.readValue(attemptHistoryResponse,
					new TypeReference<AttemptHistory>() {
					});
			if (attemptHistoryNew.getAttemptid() != null) {
				logger.info("Attempt saved sucessfully. : {}", attemptHistoryNew.getAttemptid());

				attemptStatusData.setAttemptId(attemptHistoryNew.getAttemptid());

				JsonNode attemptStatusDataJsonNode = mapper.valueToTree(attemptStatusData);
				String attemptStatusDataStr = mapper.writeValueAsString(attemptStatusDataJsonNode);

				String attemptStatusDataResponse = onlineApiService.sendDataToAttempt(spocAttemptStatusDataRestUrl,
						attemptStatusDataStr);
				AttemptStatusData attemptStatusDataNew = mapper.readValue(attemptStatusDataResponse,
						new TypeReference<AttemptStatusData>() {
						});
				if (attemptStatusDataNew.getStatusId() != null) {
					logger.info("Attempt status data saved sucessfully. : {}", attemptStatusDataNew.getStatusId());
				} else {
					logger.info("Attempt status data not saved.");
				}
			} else {
				logger.info("Attempt history not saved.");
			}
		} catch (IllegalArgumentException | JsonProcessingException e) {
			logger.error("Exception : {}", e.getMessage());
		}

	}

	private void saveOnlineVerifyCheck(ObjectMapper mapper, ComponentScoping componentScoping,
			ApiServiceResultPOJO apiServiceResultPOJO) {
		if (CollectionUtils.isNotEmpty(apiServiceResultPOJO.getOnlineVerificationChecksList())) {
			String dataEntryResult = "";
			try {
				dataEntryResult = mapper.writeValueAsString(apiServiceResultPOJO.getDataEntryMap());
			} catch (JsonProcessingException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}

			OnlineManualVerification onlineManualVerification = new OnlineManualVerification();
			onlineManualVerification.setCandidateName(componentScoping.getCandidate_Name());
			onlineManualVerification.setCaseNumber(componentScoping.getCASE_NUMBER());
			onlineManualVerification.setPackageName(componentScoping.getPackageName());
			onlineManualVerification.setClientName(componentScoping.getCLIENT_NAME());
			onlineManualVerification.setSBU(componentScoping.getSBU_NAME());
			onlineManualVerification.setCrn(componentScoping.getCASE_REF_NUMBER());
			onlineManualVerification.setStatus("Manual");
			onlineManualVerification.setTimeCreation(new Date().toString());
			onlineManualVerification.setUpdatedTime(new Date().toString());
			onlineManualVerification.setDataEntryResult(dataEntryResult);
			onlineManualVerification
					.setOnlineVerificationChecksList(apiServiceResultPOJO.getOnlineVerificationChecksList());
			// Convert Object to JsonNode
			JsonNode onlineManualVerificationNode = mapper.convertValue(onlineManualVerification, JsonNode.class);
			String onlineManualVerificationStr = onlineManualVerificationNode.toString();
			logger.info("Value Send to API : {}", onlineManualVerificationStr);

			String onlineServiceResponse = onlineApiService.sendDataToRestUrl(verificationOnlineManualUIUrl,
					onlineManualVerificationStr);
			logger.info("Response : {}", onlineServiceResponse);
		} else {
			logger.info("List is empty. All Online Request Resutl is Clear");
		}
	}

	@Override
	public List<String> processScheduledApiService(ObjectMapper mapper,
			OnlineVerificationChecks onlineVerificationChecks, List<String> checkIdList)
			throws JsonProcessingException {
		logger.info("In processScheduledApiService");
		checkIdList = checkIdList != null ? checkIdList : new ArrayList<>();
		String serviceName = onlineVerificationChecks.getApiName() != null ? onlineVerificationChecks.getApiName() : "";
		Map<String, String> serviceResponseMap = new HashMap<>();

		JsonNode inputResult = mapper.createObjectNode();
		if (!StringUtils.isEmpty(onlineVerificationChecks.getInputFile())) {
			inputResult = mapper.readTree(onlineVerificationChecks.getInputFile());
			inputResult = mapper.readTree(inputResult.asText());
		}

		String primaryName = "";
		String secondaryName = "";
		String name = inputResult.has("name") ? inputResult.get("name").asText() : "";
		String dob = inputResult.has("dob") ? inputResult.get("dob").asText() : "";
		String dinStr = "";
		String contexts = "Accused,Rape,Arrested,Fake,Fraud,Convicted,Assualt,"
				+ "Molested,Assassinated,FleshTrade,Burglary,SexRacket,Hawala,"
				+ "Betting,Smuggling,Pimp,Abeyance,Sabotage,Abetted,Racket,Criminal"
				+ ",Crime,Detained,Robbery,Forgery,Mortgage,Terrorist,Duped,Ban,"
				+ "Debarred,Wanted,Bankrupt,Summon,Bribery,Corruption,Trafficking,"
				+ "Hijack,Arson,Counterfeit,Piracy,CiberCrime,Hacking,Phishing,"
				+ "Extortion,Moneylaundering,Slavery,Manipulation,Stole,Snatch,"
				+ "Theft,Kidnapping,Hostage,Ransacke,Sack,SexualExploitation,"
				+ "WarCrimes,DistributionofFakeMedicine,Sentenced,AviationCrime,"
				+ "Mugshot,Stab,Harrasment,Fight,Quarrel,Disqalified,Foundguilty,"
				+ "HeldResponsible,Dispute,Alleged,Disqualifieddirector,Duping,"
				+ "Attacked,ChainSnatchers,Custody,Chitfundscam,illegal,"
				+ "legalproceedings,Narcotics ,nabbed,apprehend,gruesome,destroy,"
				+ "rapist,abduct,shoplifting,kingpin,contemnor,bootlegger,ambush,"
				+ "Cheat,cold-heartedmurder,Corpse,brutal,grive,abrasion,regret,"
				+ "unfortunateevent,gash,AircraftHijack,ArmsTrafficking,Cybercrime,"
				+ "Embezzlement,TaxFraud,TaxEvasion,IllicitTraffickinginstolengoods,"
				+ "InsiderTrading&MarketManipulation,MigrantSmuggling," + "IndecentAssault,Narcotics Trafficking,"
				+ "Pharmaceuticalproducttrafficking,Banned/FakeMedicines,"
				+ "IllegalProduction&Distribution(Narcotics/pharma/armsetc),"
				+ "SecuritiesFraud,Piracy(Sea),IllegalCartelInformation,PriceFixing,"
				+ "Antitrustviolations,LawViolations,Racketeering,Terrorism,"
				+ "TerrorFinancing,WantsandWarrants,parole,Probation,offender,"
				+ "Sexoffender,Committingacrime,jailed,inprison,behindthebars,"
				+ "firearm,drugs,alcohol/liquor,passjudgement,imprisonment,"
				+ "drugspeddler,impostor,Swindle,dacoit,deceive,manhandle,booked,"
				+ "Blacklisted ,trick,auto-lifter,unlawful,Illegaltrader,"
				+ "misappropriation,stealing,thief,larceny,anti-corruption,"
				+ "anti-moneylaundering,prosecute,charged,investigation,suspected,"
				+ "offense,fines,penalty,compensation,damages,sue,insolvent,"
				+ "borrower,impersonate,prostitution,brothel,infringement,"
				+ "iniquitous,breach,debauchery,dishonest,illegality,Battery,"
				+ "Homicide,manslaughter,misdemeanor,misfeasance,Felony,"
				+ "infractions,whitecollarcrime,vandalism,shoplifting,violence,"
				+ "treason,publicintoxication,trespass,disorderlyconduct,"
				+ "recklessdriving,drink&drive,hit&run,parkingornoiseviolations,"
				+ "buildingcodeviolations,littering,habitualtrafficoffender,"
				+ "Publicdrunkenness,workingunlicensed,drug-dealing,"
				+ "Succomedtoinjury,Lure,implead,hire,Posing,demanding,anonymus," + "steroid,cannabies,weed";

		if (!StringUtils.isEmpty(serviceName)) {
			ObjectNode personalInfo = mapper.createObjectNode();

			String requestUrl = "";
			if (StringUtils.equalsIgnoreCase(serviceName, "MCA")) {

				if (onlineVerificationChecks.getVerifyId() != null
						&& !StringUtils.isEmpty(onlineVerificationChecks.getVerifyId())) {
					requestUrl = verifyIdApiUrl;
					personalInfo.put("name", name);
					personalInfo.put("verify_id", onlineVerificationChecks.getVerifyId());
					personalInfo.put("api", "mca");
				} else {
					requestUrl = mcaRestUrl;
					personalInfo.put("name", name);
					personalInfo.put("dob", dob);
				}
			}

			else if (StringUtils.equalsIgnoreCase(serviceName, "Adverse Media")) {
				if (onlineVerificationChecks.getVerifyId() != null
						&& !StringUtils.isEmpty(onlineVerificationChecks.getVerifyId())) {
					requestUrl = verifyIdApiUrl;
					personalInfo.put("name", name);
					personalInfo.put("verify_id", onlineVerificationChecks.getVerifyId());
					personalInfo.put("api", "adversemedia");
				} else {
					requestUrl = adversemediaRestUrl;
					personalInfo.put("name", name);
					personalInfo.put("dob", dob);
				}
			} else

			if (StringUtils.equalsIgnoreCase(serviceName, "Manupatra")) {
				inputResult = mapper.readTree(onlineVerificationChecks.getInputFile());
				requestUrl = manupatraRestUrl;
				JsonNode manupatraPrimaryNode = inputResult.has("primary")
						? mapper.readTree(inputResult.get("primary").asText())
						: mapper.createObjectNode();
				JsonNode manupatraSecondaryNode = inputResult.has("secondary") ? inputResult.get("secondary")
						: mapper.createObjectNode();
				primaryName = manupatraPrimaryNode.has("formattedName")
						? manupatraPrimaryNode.get("formattedName").asText()
						: "";
				secondaryName = manupatraSecondaryNode.has("formattedName")
						? manupatraSecondaryNode.get("formattedName").asText()
						: "";
				personalInfo.put("name", primaryName);
				personalInfo.put("dob", dob);
				personalInfo.put("contexts", contexts);
			}
			if (StringUtils.equalsIgnoreCase(serviceName, "Loan Defaulter")) {
				if (onlineVerificationChecks.getVerifyId() != null
						&& !StringUtils.isEmpty(onlineVerificationChecks.getVerifyId())) {
					requestUrl = verifyIdApiUrl;
					personalInfo.put("name", name);
					personalInfo.put("verify_id", onlineVerificationChecks.getVerifyId());
					personalInfo.put("api", "loan");
				} else {
					requestUrl = loanRestUrl;
					personalInfo.put("name", name);
					personalInfo.put("dob", dob);
				}
			} else if (StringUtils.equalsIgnoreCase(serviceName, "Watchout")) {
				requestUrl = watchoutRestUrl;
				String firstName = inputResult.has("firstname") ? inputResult.get("firstname").asText() : "";
				String middleName = inputResult.has("middlename") ? inputResult.get("middlename").asText() : "";
				String lastName = inputResult.has("lastname") ? inputResult.get("lastname").asText() : "";
				String formattedName = "";
				if (StringUtils.isEmpty(middleName)) {
					formattedName = firstName + " " + lastName;
				} else {
					formattedName = firstName + " " + middleName + " " + lastName;
				}
				personalInfo.put("name", formattedName.trim());
				personalInfo.put("dob", dob);

				String mcaResponseStr = "";
				if (onlineVerificationChecks.getVerifyId() != null
						&& !StringUtils.isEmpty(onlineVerificationChecks.getVerifyId())) {
					personalInfo.put("name", name);
					personalInfo.put("verify_id", onlineVerificationChecks.getVerifyId());
					personalInfo.put("api", "mca");
					mcaResponseStr = onlineApiService.sendDataToRestUrl(verifyIdApiUrl, personalInfo.toString());
				} else {
					personalInfo.put("name", name);
					personalInfo.put("dob", dob);
					mcaResponseStr = onlineApiService.sendDataToRestUrl(mcaRestUrl, personalInfo.toString());
				}

				JsonNode mcaResponseNode = mapper.readValue(mcaResponseStr, JsonNode.class);
				JsonNode mcaResponse = mcaResponseNode.has("MCA") ? mcaResponseNode.get("MCA")
						: mapper.createObjectNode();
				dinStr = mcaResponse.has("din") ? mcaResponse.get("din").asText() : "";
				personalInfo.put("din", dinStr);
			}

			String apiResponse = "";
			ObjectNode objectNode = mapper.createObjectNode();
			if (!StringUtils.isEmpty(requestUrl)) {
				try {
					apiResponse = onlineApiService.sendDataToRestUrl(requestUrl, personalInfo.toString());
					if (StringUtils.equalsIgnoreCase(serviceName, "Manupatra")) {
						objectNode.put("Primary result", apiResponse);

						if (StringUtils.isEmpty(secondaryName)) {
							objectNode.put("Secondary result", "");
							apiResponse = mapper.writeValueAsString(objectNode);
						} else {
							personalInfo.put("name", secondaryName);
							apiResponse = onlineApiService.sendDataToRestUrl(requestUrl, personalInfo.toString());

							objectNode.put("Secondary result", apiResponse);
							apiResponse = mapper.writeValueAsString(objectNode);
						}

					}
				} catch (Exception e) {
					logger.info("Exception : {}", e.getMessage());
				}
			}
			if (StringUtils.isEmpty(apiResponse)) {
				serviceResponseMap.put(serviceName, apiResponse);
			}

			String retryNo = onlineVerificationChecks.getRetryNo() != null ? onlineVerificationChecks.getRetryNo() : "";

			if (StringUtils.isEmpty(retryNo) || StringUtils.equalsIgnoreCase(retryNo, "null")) {
				retryNo = "1";
			} else {
				try {
					int retryNoInt = Integer.parseInt(retryNo);
					retryNoInt += 1;
					retryNo = String.valueOf(retryNoInt);
				} catch (NumberFormatException e) {
					logger.error(e.getMessage(), e);
					e.printStackTrace();
				}
			}

			onlineVerificationChecks.setRetryNo(retryNo);
			updateVerifyChecks(mapper, onlineVerificationChecks, apiResponse, checkIdList);
		}

		return checkIdList;
	}

	private List<String> updateVerifyChecks(ObjectMapper mapper, OnlineVerificationChecks onlineVerificationChecks,
			String apiResponse, List<String> checkIdList) {

		if (StringUtils.equalsIgnoreCase(onlineVerificationChecks.getApiName(), "Manupatra")) {
			try {
				checkIdList = setUpdatedManupatraResponse(mapper, onlineVerificationChecks, apiResponse, checkIdList);
			} catch (JsonProcessingException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		} else {
			try {
				checkIdList = setUpdatedApiResponse(mapper, onlineVerificationChecks, apiResponse, checkIdList);
			} catch (JsonProcessingException e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
		return checkIdList;
	}

	private List<String> setUpdatedApiResponse(ObjectMapper mapper, OnlineVerificationChecks onlineVerificationChecks,
			String apiResponse, List<String> checkIdList) throws JsonMappingException, JsonProcessingException {

		String checkId = onlineVerificationChecks.getCheckId() != null ? onlineVerificationChecks.getCheckId() : "";
		String apiName = onlineVerificationChecks.getApiName() != null ? onlineVerificationChecks.getApiName() : "";
		if (apiResponse != null && !StringUtils.isEmpty(apiResponse) && !StringUtils.isEmpty(apiName)) {
			JsonNode apiResponseNode = mapper.readValue(apiResponse, JsonNode.class);

			if (StringUtils.isNotEmpty(checkId)) {
				checkIdList = checkIdList != null ? checkIdList : new ArrayList<>();
				checkIdList.add(checkId);
			}

			String apiOutput = "";
			String rawOutput = "";
			String apiInput = "";
			String finalStatus = "";

			if (StringUtils.equalsIgnoreCase(apiName, "MCA")) {
				JsonNode mcaResponseNode = apiResponseNode.has("MCA") ? apiResponseNode.get("MCA")
						: mapper.createObjectNode();
				apiOutput = mcaResponseNode.has("MCA Output") ? mcaResponseNode.get("MCA Output").asText() : "";
				rawOutput = mcaResponseNode.has("Raw Output") ? mcaResponseNode.get("Raw Output").asText() : "";
				apiInput = mcaResponseNode.has("MCA Input") ? mcaResponseNode.get("MCA Input").asText() : "";
				finalStatus = mcaResponseNode.has("status") ? mcaResponseNode.get("status").asText() : "";

				String verifyId = mcaResponseNode.has("Verify_id") ? mcaResponseNode.get("Verify_id").asText() : "";
				onlineVerificationChecks.setVerifyId(verifyId);
			}
			if (StringUtils.equalsIgnoreCase(apiName, "Adverse Media")
					|| StringUtils.equalsIgnoreCase(apiName, "Google")) {
				JsonNode adverseResponseNode = apiResponseNode.has("Adverse Media")
						? apiResponseNode.get("Adverse Media")
						: mapper.createObjectNode();
				if (adverseResponseNode.isEmpty()) {
					adverseResponseNode = apiResponseNode.has("AdverseMedia") ? apiResponseNode.get("AdverseMedia")
							: mapper.createObjectNode();
				}
				apiOutput = adverseResponseNode.has("Adverse Media Output")
						? adverseResponseNode.get("Adverse Media Output").asText()
						: "";
				rawOutput = adverseResponseNode.has("Raw Output") ? adverseResponseNode.get("Raw Output").asText() : "";
				apiInput = adverseResponseNode.has("Adverse media Input")
						? adverseResponseNode.get("Adverse media Input").asText()
						: "";
				finalStatus = adverseResponseNode.has("status") ? adverseResponseNode.get("status").asText() : "";

				String verifyId = adverseResponseNode.has("Verify_id") ? adverseResponseNode.get("Verify_id").asText()
						: "";
				onlineVerificationChecks.setVerifyId(verifyId);
			}
			if (StringUtils.equalsIgnoreCase(apiName, "Loan Defaulter (Cibil)")) {
				JsonNode loanResponseNode = apiResponseNode.has("Loan Defaulter")
						? apiResponseNode.get("Loan Defaulter")
						: mapper.createObjectNode();
				apiOutput = loanResponseNode.has("Loan Defaulter Output")
						? loanResponseNode.get("Loan Defaulter Output").asText()
						: "";
				rawOutput = loanResponseNode.has("Raw Output") ? loanResponseNode.get("Raw Output").asText() : "";
				apiInput = loanResponseNode.has("Loan Defaulter Input")
						? loanResponseNode.get("Loan Defaulter Input").asText()
						: "";
				finalStatus = loanResponseNode.has("status") ? loanResponseNode.get("status").asText() : "";

				String verifyId = loanResponseNode.has("Verify_id") ? loanResponseNode.get("Verify_id").asText() : "";
				onlineVerificationChecks.setVerifyId(verifyId);
			}
			if (StringUtils.equalsIgnoreCase(apiName, "WatchOut")) {
				JsonNode watchOutResponseNode = apiResponseNode.has("WatchOut") ? apiResponseNode.get("WatchOut")
						: mapper.createObjectNode();
				apiOutput = watchOutResponseNode.has("WatchOut Output")
						? watchOutResponseNode.get("WatchOut Output").asText()
						: "";
				rawOutput = watchOutResponseNode.has("Raw Output") ? watchOutResponseNode.get("Raw Output").asText()
						: "";
				apiInput = watchOutResponseNode.has("WatchOut Input")
						? watchOutResponseNode.get("WatchOut Input").asText()
						: "";
				finalStatus = watchOutResponseNode.has("status") ? watchOutResponseNode.get("status").asText() : "";
			}

			if (StringUtils.equalsAnyIgnoreCase(finalStatus, "Manual", "Clear", "Record Found")) {
				onlineVerificationChecks.setPendingStatus("0");
			} else {
				onlineVerificationChecks.setPendingStatus("1");
				finalStatus = "Pending";
			}

			onlineVerificationChecks.setResult(finalStatus);
			onlineVerificationChecks.setMatchedIdentifiers(apiOutput);
			onlineVerificationChecks.setOutputFile(rawOutput);
			onlineVerificationChecks.setInputFile(apiInput);
			onlineVerificationChecks.setUpdatedDate(new Date().toString());
		}

		JsonNode objectNode = mapper.convertValue(onlineVerificationChecks, JsonNode.class);
		String verifyChecksStr = mapper.writeValueAsString(objectNode);

		onlineApiService.sendDataToLocalRest(updateVerifyChecksUrl, verifyChecksStr);

		return checkIdList;
	}

	private List<String> setUpdatedManupatraResponse(ObjectMapper mapper,
			OnlineVerificationChecks onlineVerificationChecks, String apiResponse, List<String> checkIdList)
			throws JsonProcessingException {

		JsonNode apiResponseNode = mapper.readValue(apiResponse, JsonNode.class);

		JsonNode manuPatraResponsePrimary = apiResponseNode.has("Primary result")
				? mapper.readTree(apiResponseNode.get("Primary result").asText())
				: mapper.createObjectNode();
		JsonNode manuPatraResponseSecondary = apiResponseNode.has("Secondary result")
				? mapper.readTree(apiResponseNode.get("Secondary result").asText())
				: mapper.createObjectNode();

		String primaryStatus = "";
		String primaryManupatraOutput = "";
		String primaryRawOutput = "";
		String primaryManupatraInput = "";
		String primaryKeyName = "";

		String secondaryStatus = "NA";
		String secondaryManupatraOutput = "";
		String secondaryRawOutput = "";
		String secondaryManupatraInput = "";
		String secondaryKeyName = "";

		JsonNode manupatraPrimaryNode = manuPatraResponsePrimary.has("Manupatra")
				? manuPatraResponsePrimary.get("Manupatra")
				: mapper.createObjectNode();

		if (manupatraPrimaryNode.has("status")) {

			primaryStatus = manupatraPrimaryNode.get("status").asText();
			primaryManupatraOutput = manupatraPrimaryNode.has("ManuPatra Output")
					? manupatraPrimaryNode.get("ManuPatra Output").asText()
					: "";
			primaryRawOutput = manupatraPrimaryNode.has("Raw Output") ? manupatraPrimaryNode.get("Raw Output").asText()
					: "";
			primaryManupatraInput = manupatraPrimaryNode.has("ManuPatra Input")
					? manupatraPrimaryNode.get("ManuPatra Input").asText()
					: "";
			primaryKeyName = manupatraPrimaryNode.has("Matched Key Name")
					? manupatraPrimaryNode.get("Matched Key Name").asText()
					: "";
		}

		JsonNode manupatraSecondaryNode = manuPatraResponseSecondary.has("Manupatra")
				? manuPatraResponseSecondary.get("Manupatra")
				: mapper.createObjectNode();

		if (manupatraSecondaryNode.has("status")) {

			secondaryStatus = manupatraSecondaryNode.get("status").asText();
			secondaryManupatraOutput = manupatraSecondaryNode.has("ManuPatra Output")
					? manupatraSecondaryNode.get("ManuPatra Output").asText()
					: "";
			secondaryRawOutput = manupatraSecondaryNode.has("Raw Output")
					? manupatraSecondaryNode.get("Raw Output").asText()
					: "";
			secondaryManupatraInput = manupatraSecondaryNode.has("ManuPatra Input")
					? manupatraSecondaryNode.get("ManuPatra Input").asText()
					: "";
			secondaryKeyName = manupatraSecondaryNode.has("Matched Key Name")
					? manupatraSecondaryNode.get("Matched Key Name").asText()
					: "";
		}

		String finalStatus = onlineFinalService.getManupatraFinalStatus(primaryStatus, secondaryStatus);

		ObjectNode manupatraOutput = mapper.createObjectNode();
		manupatraOutput.put("primary", primaryManupatraOutput);
		manupatraOutput.put("secondary", secondaryManupatraOutput);

		ObjectNode rawOutput = mapper.createObjectNode();
		rawOutput.put("primary", primaryRawOutput);
		rawOutput.put("secondary", secondaryRawOutput);
		String rawOutputStr = mapper.writeValueAsString(rawOutput);

		ObjectNode manupatraInput = mapper.createObjectNode();
		manupatraInput.put("primary", primaryManupatraInput);
		manupatraInput.put("secondary", secondaryManupatraInput);
		String manupatraInputStr = mapper.writeValueAsString(manupatraInput);

		ObjectNode matchedKeyName = mapper.createObjectNode();
		matchedKeyName.put("primary", primaryKeyName);
		matchedKeyName.put("secondary", secondaryKeyName);
		String matchedKeyNameStr = mapper.writeValueAsString(matchedKeyName);
		if (StringUtils.isEmpty(primaryKeyName) && StringUtils.isEmpty(secondaryKeyName)) {
			matchedKeyNameStr = mapper.writeValueAsString(manupatraOutput);
		}

		String checkId = onlineVerificationChecks.getCheckId() != null ? onlineVerificationChecks.getCheckId() : "";

		if (StringUtils.isNotEmpty(checkId)) {
			checkIdList = checkIdList != null ? checkIdList : new ArrayList<>();
			checkIdList.add(checkId);
		}

		if (StringUtils.equalsAnyIgnoreCase(finalStatus, "Manual", "Clear", "Record Found")) {
			onlineVerificationChecks.setPendingStatus("0");
		} else {
			onlineVerificationChecks.setPendingStatus("1");
		}

		onlineVerificationChecks.setResult(finalStatus);
		onlineVerificationChecks.setOutputFile(rawOutputStr);
		onlineVerificationChecks.setInputFile(manupatraInputStr);
		onlineVerificationChecks.setMatchedIdentifiers(matchedKeyNameStr);
		onlineVerificationChecks.setUpdatedDate(new Date().toString());

		JsonNode objectNode = mapper.convertValue(onlineVerificationChecks, JsonNode.class);
		String verifyChecksStr = mapper.writeValueAsString(objectNode);

		onlineApiService.sendDataToLocalRest(updateVerifyChecksUrl, verifyChecksStr);

		return checkIdList;
	}

	@Override
	public void runParallelService(List<OnlineVerificationChecks> onlineVerificationChecksList)
			throws InterruptedException, ExecutionException {
		// Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		if (onlineVerificationChecksList != null && !CollectionUtils.isEmpty(onlineVerificationChecksList)) {
			List<String> serviceNames = onlineVerificationChecksList.stream().map(OnlineVerificationChecks::getApiName)
					.collect(Collectors.toList());
			serviceNames = new ArrayList<>(new HashSet<>(serviceNames));
			List<String> checkIdList = onlineVerificationChecksList.stream().map(OnlineVerificationChecks::getCheckId)
					.collect(Collectors.toList());

			checkIdList = new ArrayList<>(new HashSet<>(checkIdList));
			Long onlineVerificationCheckId = (long) 0;

			List<CompletableFuture<Void>> completableFutureList = new ArrayList<>();

			for (String serviceName : serviceNames) {
				OnlineVerificationChecks onlineVerificationCheck = onlineVerificationChecksList.stream()
						.filter(p -> StringUtils.equalsIgnoreCase(p.getApiName(), serviceName))
						.collect(Collectors.toList()).get(0);
				String retryNo = onlineVerificationCheck.getRetryNo() != null ? onlineVerificationCheck.getRetryNo()
						: "1";
				if (StringUtils.equalsIgnoreCase(retryNo, "null")) {
					retryNo = "1";
				}
				int retryNoInt = Integer.parseInt(retryNo);
				String pendingStatus = onlineVerificationCheck.getPendingStatus() != null
						? onlineVerificationCheck.getPendingStatus()
						: "1";

				if (retryNoInt < 6 && (StringUtils.equalsAnyIgnoreCase(pendingStatus, "1", "null")
						|| StringUtils.isEmpty(pendingStatus))) {
					onlineVerificationCheckId = onlineVerificationCheck.getOnlineVerificationCheckId();
					CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
						try {
							List<String> checkIdListTmp = new ArrayList<>();
							processScheduledApiService(mapper, onlineVerificationCheck, checkIdListTmp);
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
							e.printStackTrace();
						}
					});
					completableFutureList.add(completableFuture);
				}
			}

			CompletableFuture<Void> future = CompletableFuture
					.allOf(completableFutureList.toArray(new CompletableFuture[completableFutureList.size()]));
			future.get(); // this line waits for all to be completed

			if (onlineVerificationCheckId != 0 && CollectionUtils.size(onlineVerificationChecksList) > 1) {
				updateOtherOnlineChecks(mapper, onlineVerificationCheckId, onlineVerificationChecksList);
			}
			checkIdList = new ArrayList<>(new HashSet<>(checkIdList));

			onlineApiService.sendDataToL3ByCheckId(verificationStatusUrlL3, checkIdList);
		}
	}

	private void updateOtherOnlineChecks(ObjectMapper mapper, Long onlineVerificationCheckId,
			List<OnlineVerificationChecks> onlineVerificationCheckList) {
		String requestUrl = onlineverifychecksRestUrl + onlineVerificationCheckId;
		String onlineResponse = apiService.sendDataToGet(requestUrl);
		try {
			if (onlineResponse != null) {
				OnlineVerificationChecks newOnlineVerificationChecks = mapper.readValue(onlineResponse,
						OnlineVerificationChecks.class);

				if (newOnlineVerificationChecks != null) {
					for (OnlineVerificationChecks onlineVerificationChecks : onlineVerificationCheckList) {
						if (onlineVerificationCheckId != onlineVerificationChecks.getOnlineVerificationCheckId()) {

							onlineVerificationChecks.setPendingStatus(newOnlineVerificationChecks.getPendingStatus());
							onlineVerificationChecks.setResult(newOnlineVerificationChecks.getResult());
							onlineVerificationChecks.setOutputFile(newOnlineVerificationChecks.getOutputFile());
							onlineVerificationChecks.setInputFile(newOnlineVerificationChecks.getInputFile());
							onlineVerificationChecks
									.setMatchedIdentifiers(newOnlineVerificationChecks.getMatchedIdentifiers());
							onlineVerificationChecks.setRetryNo(newOnlineVerificationChecks.getRetryNo());
							onlineVerificationChecks.setUpdatedDate(new Date().toString());

							JsonNode objectNode = mapper.convertValue(onlineVerificationChecks, JsonNode.class);
							String verifyChecksStr = mapper.writeValueAsString(objectNode);

							apiService.sendDataToPost(updateVerifyChecksUrl, verifyChecksStr);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception occured while saving other online verification checks : {}", e.getMessage());
		}

	}

	@Override
	public void callBack(String postStr) {
		try {

			logger.debug("postStr : \n {}", postStr);
			URL url = new URL(env.getProperty("online.router.callback.url"));
			logger.debug("Using callback URL: \n {}", url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			// Creating the ObjectMapper object
			String authToken = JsonParser.parseString(postStr).getAsJsonObject().get("metadata").getAsJsonObject()
					.get("requestAuthToken").getAsString();

			logger.debug("Auth Token : \n {}", authToken);
			con.setRequestProperty("tokenId", authToken);

			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write(postStr.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();
			logger.debug("Callback POST Response Code: {} : {}", responseCode, con.getResponseMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private String sendDataToL3Mi(JsonNode recordNode, ComponentScoping componentScoping, Component component)
			throws JsonProcessingException {
		// Creating the ObjectMapper object
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String checkId = recordNode.has("checkId") ? recordNode.get("checkId").asText() : "";
		String miRemarks = recordNode.has("miRemarks") ? recordNode.get("miRemarks").asText() : "";

		TaskSpecsPOJO taskSpecs = new TaskSpecsPOJO();
		CaseReferencePOJO caseReference = mapper.convertValue(componentScoping.getCaseReference(),
				CaseReferencePOJO.class);

		caseReference.setCheckId(checkId);
		caseReference.setNgStatus("MI-RQ");
		caseReference.setNgStatusDescription("Missing Information - Requested");
		caseReference.setSbuName(componentScoping.getSBU_NAME());
		caseReference.setProductName(component.getPRODUCT());
		caseReference.setPackageName(componentScoping.getPackageName());
		caseReference.setComponentName("Database");

		CheckVerificationPOJO checkVerification = new CheckVerificationPOJO();
		checkVerification.setCountry("India");
		checkVerification.setExecutiveSummaryComments("");
		checkVerification.setReportComments("");
		checkVerification.setResultCode("");
		checkVerification.setEmailId("");
		checkVerification.setVerifierName("");
		checkVerification.setGeneralRemarks("");
		checkVerification.setModeOfVerification("Online");
		checkVerification.setVerifiedDate("");
		checkVerification.setAction("checklevelmi");
		checkVerification.setSubAction("databasemi");
		checkVerification.setActionCode("");
		checkVerification.setComponentName("Database");
		checkVerification.setProductName(component.getPRODUCT());
		checkVerification.setAttempts("");
		checkVerification.setDepartmentName("");
		checkVerification.setKeyWords("");
		checkVerification.setCost("");
		checkVerification.setIsThisAVerificationAttempt("No");
		checkVerification.setInternalNotes(miRemarks);
		checkVerification.setDateVerificationCompleted("");
		checkVerification.setDisposition("");
		checkVerification.setExpectedClosureDate("");
		checkVerification.setEndStatusOfTheVerification("Additional Information Required");
		checkVerification.setVerifierDesignation("");
		checkVerification.setFollowUpDateAndTimes("");
		checkVerification.setVerifierNumber("");
		checkVerification.setMiRemarks(miRemarks);

		taskSpecs.setCaseReference(caseReference);
		taskSpecs.setCheckVerification(checkVerification);

		taskSpecs.setQuestionaire(new ArrayList<>());

		FileUploadPOJO fileUpload = new FileUploadPOJO();
		fileUpload.setVerificationReplyDocument(new ArrayList<>());
		taskSpecs.setFileUpload(fileUpload);

		String getVerificationStatusforL3 = mapper.writeValueAsString(taskSpecs);
		String sendInfol3VeriStatus = null;

		try {
			logger.info("getVerificationStatusforL3Node : {} ", getVerificationStatusforL3);
			sendInfol3VeriStatus = onlineApiService.sendDataToRest(verificationStatusL3Url, getVerificationStatusforL3,
					null);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		return sendInfol3VeriStatus;
	}
}
