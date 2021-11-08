package com.gic.fadv.online.service;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.online.utility.Utility;

@Service
public class OnlineProcessApiServiceImpl implements OnlineProcessApiService {

	@Autowired
	ApiService apiService;

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

	private static final Logger logger = LoggerFactory.getLogger(OnlineProcessApiServiceImpl.class);
	private static final String WORLD_CHECK = "Worldcheck";
	private static final String WATCHOUT = "Watchout";
	private static final String MCA = "MCA";
	private static final String DIN = "din";
	private static final String ADVERSE_MEDIA = "Adverse Media";
	private static final String GOOGLE = "Google";
	private static final String MANUPATRA = "Manupatra";
	private static final String LOAN_DEFAULTER_CIBIL = "Loan Defaulter (Cibil)";
	private static final String LOAN_DEFAULTER = "Loan Defaulter";
	private static final String FATHERS_NAME = "fathersname";
	private static final String DOB = "dob";
	private static final String PRIMARY_NAME = "primaryName";
	private static final String SECONDARY_NAME = "secondaryName";
	private static final String ADDRESS = "address";
	private static final String NAME = "name";
	private static final String FATHER_NAME = "father_name";
	private static final String PRIMARY_RESULT = "Primary result";
	private static final String SECONDARY_RESULT = "Secondary result";
	private static final String CONTEXTS = "Accused,Rape,Arrested,Fake,Fraud,Convicted,Assualt,"
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

	@Override
	public void processApiService(ObjectMapper mapper, String serviceName, Map<String, String> serviceResponseMap,
			JsonNode dataEntryNode) {

		ObjectNode personalInfoNode = getPersonalInfo(mapper, dataEntryNode);
		String dinStr = checkAndProcessWorldCheck(mapper, serviceName, serviceResponseMap);

		if (StringUtils.equalsIgnoreCase(serviceName, MCA)) {
			String mcaResponse = processAPIRecordWise(mapper, personalInfoNode, mcaRestUrl);
			serviceResponseMap.put(MCA, mcaResponse);
		}
		if (StringUtils.equalsIgnoreCase(serviceName, WORLD_CHECK)) {
			if (dinStr != null && !StringUtils.isEmpty(dinStr)) {
				personalInfoNode.put("din", dinStr);
			}
			if(personalInfoNode.has(DOB)) {
				String formattedDob=Utility.formatDateUtil(personalInfoNode.get(DOB).asText());
				personalInfoNode.put(DOB, formattedDob);
			}
			serviceResponseMap.put(WORLD_CHECK, processAPIRecordWise(mapper, personalInfoNode, worldcheckRestUrl));
		}
		if (StringUtils.equalsIgnoreCase(serviceName, ADVERSE_MEDIA)
				|| StringUtils.equalsIgnoreCase(serviceName, GOOGLE)) {
			personalInfoNode.put("contexts", CONTEXTS);
			serviceResponseMap.put(ADVERSE_MEDIA, processAPIRecordWise(mapper, personalInfoNode, adversemediaRestUrl));
		}
		checkAndProcessManupatra(mapper, serviceName, personalInfoNode, serviceResponseMap, dataEntryNode);
		if (StringUtils.equalsIgnoreCase(serviceName, LOAN_DEFAULTER_CIBIL)
				|| StringUtils.equalsIgnoreCase(serviceName, LOAN_DEFAULTER)) {
			serviceResponseMap.put(LOAN_DEFAULTER, processAPIRecordWise(mapper, personalInfoNode, loanRestUrl));
		}
		if (StringUtils.equalsIgnoreCase(serviceName, WATCHOUT)) {
			if (dinStr != null && !StringUtils.isEmpty(dinStr)) {
				personalInfoNode.put("din", dinStr);
			}
			serviceResponseMap.put(WATCHOUT, processAPIRecordWise(mapper, personalInfoNode, watchoutRestUrl));
		}
	}

	private ObjectNode getPersonalInfo(ObjectMapper mapper, JsonNode dataEntryNode) {
		ObjectNode personalInfo = mapper.createObjectNode();
		personalInfo.put(NAME, dataEntryNode.has(PRIMARY_NAME) ? dataEntryNode.get(PRIMARY_NAME).asText() : "");
		personalInfo.put(DOB, dataEntryNode.has(DOB) ? dataEntryNode.get(DOB).asText() : "");
		personalInfo.put(FATHER_NAME, dataEntryNode.has(FATHERS_NAME) ? dataEntryNode.get(FATHERS_NAME).asText() : "");
		personalInfo.put(ADDRESS, dataEntryNode.has(ADDRESS) ? dataEntryNode.get(ADDRESS).asText() : "");

		return personalInfo;
	}

	private void checkAndProcessManupatra(ObjectMapper mapper, String serviceName, ObjectNode personalInfoNode,
			Map<String, String> serviceResponseMap, JsonNode dataEntryNode) {
		if (StringUtils.equalsIgnoreCase(serviceName, MANUPATRA)) {
			try {
				serviceResponseMap.put(MANUPATRA,
						processManupatraAPIRecordWise(mapper, dataEntryNode, personalInfoNode));
			} catch (JsonProcessingException e) {
				logger.error("Exception while mapping manupatra response : {}", e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private String checkAndProcessWorldCheck(ObjectMapper mapper, String serviceName,
			Map<String, String> serviceResponseMap) {
		String dinStr = "";
		if (StringUtils.equalsIgnoreCase(serviceName, WORLD_CHECK)
				|| StringUtils.equalsIgnoreCase(serviceName, WATCHOUT)) {
			String mcaResponse = serviceResponseMap.get(MCA);
			mcaResponse = mcaResponse != null ? mcaResponse : "{}";

			try {
				JsonNode responseNode = mapper.readTree(mcaResponse);
				JsonNode mcaResponseNode = responseNode.has(MCA) ? responseNode.get(MCA) : mapper.createObjectNode();
				dinStr = mcaResponseNode.has(DIN) ? mcaResponseNode.get(DIN).asText() : "";
			} catch (JsonProcessingException e) {
				logger.error("Exception : {}", e.getMessage());
				e.printStackTrace();
			}

		}
		return dinStr;
	}

	private String processAPIRecordWise(ObjectMapper mapper, ObjectNode personalInfoNode, String requestUrl) {
		try {
			String personalInfoStr = mapper.writeValueAsString(personalInfoNode);
			return apiService.sendDataToPost(requestUrl, personalInfoStr);
		} catch (Exception e) {
			logger.info("Exception while processing MCA : {}", e.getMessage());
			return "{}";
		}
	}

	private String processManupatraAPIRecordWise(ObjectMapper mapper, JsonNode dataEntryNode,
			ObjectNode personalInfoNode) throws JsonProcessingException {

		String primaryResponse = "{}";
		String secondaryResponse = "{}";
		JsonNode primaryResponseNode = mapper.createObjectNode();
		JsonNode secondaryResponseNode = mapper.createObjectNode();

		ObjectNode objectNode = mapper.createObjectNode();
		try {
			String personalInfoStr = mapper.writeValueAsString(personalInfoNode);
			primaryResponse = apiService.sendDataToPost(manupatraRestUrl, personalInfoStr);
			
			
			try {
				primaryResponseNode = StringUtils.isNotEmpty(primaryResponse) 
						? mapper.readTree(primaryResponse) : mapper.createObjectNode();
			} catch (Exception e) {
				logger.error("Exception while mapping primary reponse : {}", e.getMessage());
				primaryResponseNode = mapper.createObjectNode();
			}
			objectNode.set(PRIMARY_RESULT, primaryResponseNode);
			String seconDaryName = dataEntryNode.has(SECONDARY_NAME) ? dataEntryNode.get(SECONDARY_NAME).asText() : "";
			if (!StringUtils.isEmpty(seconDaryName)) {
				personalInfoNode.put(NAME, seconDaryName);
				personalInfoStr = mapper.writeValueAsString(personalInfoNode);
				secondaryResponse = apiService.sendDataToPost(manupatraRestUrl, personalInfoStr);
				try {
					secondaryResponseNode = StringUtils.isNotEmpty(secondaryResponse) 
							? mapper.readTree(secondaryResponse) : mapper.createObjectNode();
				} catch (Exception e) {
					logger.error("Exception while mapping primary reponse : {}", e.getMessage());
					secondaryResponseNode = mapper.createObjectNode();
				}
				objectNode.set(SECONDARY_RESULT, secondaryResponseNode);
			} else {
				objectNode.set(SECONDARY_RESULT, secondaryResponseNode);
			}
		} catch (Exception e) {
			logger.info("Exception while calling Manupatra with primary name : {}", e.getMessage());
			objectNode.set(PRIMARY_RESULT, primaryResponseNode);
			objectNode.set(SECONDARY_RESULT, secondaryResponseNode);
		}
		return mapper.writeValueAsString(objectNode);
	}
}
