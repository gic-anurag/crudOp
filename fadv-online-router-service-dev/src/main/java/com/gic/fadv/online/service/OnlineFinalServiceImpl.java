package com.gic.fadv.online.service;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class OnlineFinalServiceImpl implements OnlineFinalService {

	private static final String PRIMARY_RESULT = "Primary result";
	private static final String SECONDARY_RESULT = "Secondary result";
	private static final String MANUAL = "Manual";
	private static final String PENDING = "Pending";
	private static final String RECORD_FOUND = "Record Found";
	private static final String STATUS = "status";
	private static final String MANUPATRA = "Manupatra";
	private static final String PRIMARY = "primary";
	private static final String SECONDARY = "secondary";
	private static final String MANUPATRA_INPUT = "ManuPatra Input";
	private static final String MANUPATRA_OUTPUT = "ManuPatra Output";
	private static final String RAW_OUTPUT = "Raw Output";
	private static final String MATCHED_KEY_NAME = "Matched Key Name";
	private static final String INPUT_FILE = "inputFile";
	private static final String OUTPUT_FILE = "outputFile";
	private static final String MATCHED_IDENTIFIER = "matchedIdentifier";
	private static final String FINAL_STATUS = "finalStatus";
	private static final String CLEAR = "Clear";
	private static final String VERIFYID = "verifyId";
	private static final String VERIFY_ID = "Verify_id";
	private static final String API_NAME = "apiName";
	private static final String WORLD_CHECK = "Worldcheck";
	private static final String WATCHOUT = "Watchout";
	private static final String MCA = "MCA";
	private static final String ADVERSE_MEDIA = "Adverse Media";
	private static final String LOAN_DEFAULTER = "Loan Defaulter";

	@Override
	public ObjectNode setManupatraResponse(ObjectMapper mapper, JsonNode apiResponseNode) {

		JsonNode manuPatraResponsePrimary = apiResponseNode.has(PRIMARY_RESULT) ? apiResponseNode.get(PRIMARY_RESULT)
				: mapper.createObjectNode();
		JsonNode manuPatraResponseSecondary = apiResponseNode.has(SECONDARY_RESULT)
				? apiResponseNode.get(SECONDARY_RESULT)
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

		if (manuPatraResponsePrimary != null && manuPatraResponsePrimary.has(MANUPATRA)) {
			JsonNode manupatraNode = manuPatraResponsePrimary.get(MANUPATRA);

			if (manupatraNode != null && manupatraNode.has(STATUS)) {

				primaryStatus = manupatraNode.get(STATUS).asText();
				primaryManupatraOutput = manupatraNode.has(MANUPATRA_OUTPUT)
						? manupatraNode.get(MANUPATRA_OUTPUT).asText()
						: "";
				primaryRawOutput = manupatraNode.has(RAW_OUTPUT) ? manupatraNode.get(RAW_OUTPUT).asText() : "";
				primaryManupatraInput = manupatraNode.has(MANUPATRA_INPUT) ? manupatraNode.get(MANUPATRA_INPUT).asText()
						: "";
				primaryKeyName = manupatraNode.has(MATCHED_KEY_NAME) ? manupatraNode.get(MATCHED_KEY_NAME).asText()
						: "";
			}
		}

		if (manuPatraResponseSecondary != null && manuPatraResponseSecondary.has(MANUPATRA)) {
			JsonNode manupatraNode = manuPatraResponseSecondary.get(MANUPATRA);

			if (manupatraNode != null && manupatraNode.has(STATUS)) {

				secondaryStatus = manupatraNode.get(STATUS).asText();
				secondaryManupatraOutput = manupatraNode.has(MANUPATRA_OUTPUT)
						? manupatraNode.get(MANUPATRA_OUTPUT).asText()
						: "";
				secondaryRawOutput = manupatraNode.has(RAW_OUTPUT) ? manupatraNode.get(RAW_OUTPUT).asText() : "";
				secondaryManupatraInput = manupatraNode.has(MANUPATRA_INPUT)
						? manupatraNode.get(MANUPATRA_INPUT).asText()
						: "";
				secondaryKeyName = manupatraNode.has(MATCHED_KEY_NAME) ? manupatraNode.get(MATCHED_KEY_NAME).asText()
						: "";
			} 
		}
		String finalStatus = getManupatraFinalStatus(primaryStatus, secondaryStatus);

		ObjectNode manupatraOutput = mapper.createObjectNode();
		manupatraOutput.put(PRIMARY, primaryManupatraOutput);
		manupatraOutput.put(SECONDARY, secondaryManupatraOutput);

		ObjectNode rawOutput = mapper.createObjectNode();
		rawOutput.put(PRIMARY, primaryRawOutput);
		rawOutput.put(SECONDARY, secondaryRawOutput);

		ObjectNode manupatraInput = mapper.createObjectNode();
		manupatraInput.put(PRIMARY, primaryManupatraInput);
		manupatraInput.put(SECONDARY, secondaryManupatraInput);

		ObjectNode matchedKeyName = mapper.createObjectNode();
		matchedKeyName.put(PRIMARY, primaryKeyName);
		matchedKeyName.put(SECONDARY, secondaryKeyName);

		ObjectNode responseNode = mapper.createObjectNode();
		responseNode.put(API_NAME, MANUPATRA);
		responseNode.set(INPUT_FILE, manupatraInput);
		responseNode.set(MATCHED_IDENTIFIER, matchedKeyName);
		responseNode.set(OUTPUT_FILE, rawOutput);
		responseNode.put(FINAL_STATUS, finalStatus);
		responseNode.put(VERIFYID, "");

		if (StringUtils.isEmpty(primaryKeyName) && StringUtils.isEmpty(secondaryKeyName)) {
			responseNode.set(MATCHED_IDENTIFIER, manupatraOutput);
		}
		return responseNode;
	}

	@Override
	public String getManupatraFinalStatus(String primaryStatus, String secondaryStatus) {
		String finalStatus = "";
		if (StringUtils.isEmpty(primaryStatus) && StringUtils.isEmpty(secondaryStatus)) {
			return CLEAR;
		}
		List<String> status = Arrays.asList(MANUAL, RECORD_FOUND, CLEAR);
		if (StringUtils.equalsIgnoreCase(secondaryStatus, "NA")) {
			if (status.contains(primaryStatus)) {
				return primaryStatus;
			} else {
				return PENDING;
			}
		} else {
		
			if (StringUtils.equalsIgnoreCase(primaryStatus, MANUAL)
					|| StringUtils.equalsIgnoreCase(secondaryStatus, MANUAL)) {
				finalStatus = MANUAL;
			} else if (StringUtils.equalsIgnoreCase(primaryStatus, RECORD_FOUND)
					|| StringUtils.equalsIgnoreCase(secondaryStatus, RECORD_FOUND)) {
				finalStatus = RECORD_FOUND;
			} else if (StringUtils.equalsIgnoreCase(primaryStatus, CLEAR)
					&& StringUtils.equalsIgnoreCase(secondaryStatus, CLEAR)) {
				finalStatus = CLEAR;
			} else {
				finalStatus = PENDING;
			}

		}
		
		if (StringUtils.isEmpty(primaryStatus) || StringUtils.isEmpty(secondaryStatus)) {
			finalStatus = MANUAL;
		}
		return finalStatus;
		/*
		 * else if (StringUtils.equalsIgnoreCase(primaryStatus, CLEAR) &&
		 * StringUtils.equalsIgnoreCase(secondaryStatus, CLEAR)) { finalStatus = CLEAR;
		 * }
		 */
	}

	@Override
	public ObjectNode setMcaResponse(ObjectMapper mapper, JsonNode apiResponseNode) {
		JsonNode mcaResponseNode = apiResponseNode.has(MCA) ? apiResponseNode.get(MCA) : mapper.createObjectNode();
		JsonNode apiOutput = mcaResponseNode.has("MCA Output") ? mcaResponseNode.get("MCA Output")
				: mapper.createObjectNode();
		JsonNode rawOutput = mcaResponseNode.has(RAW_OUTPUT) ? mcaResponseNode.get(RAW_OUTPUT)
				: mapper.createObjectNode();
		JsonNode apiInput = mcaResponseNode.has("MCA Input") ? mcaResponseNode.get("MCA Input")
				: mapper.createObjectNode();
		String finalStatus = mcaResponseNode.has(STATUS) ? mcaResponseNode.get(STATUS).asText() : "";
		String verifyId = mcaResponseNode.has(VERIFY_ID) ? mcaResponseNode.get(VERIFY_ID).asText() : "";

		if (!StringUtils.equalsAnyIgnoreCase(finalStatus, MANUAL, CLEAR, RECORD_FOUND)) {
			finalStatus = PENDING;
		}
		ObjectNode responseNode = mapper.createObjectNode();
		responseNode.put(API_NAME, MCA);
		responseNode.set(INPUT_FILE, apiInput);
		responseNode.set(MATCHED_IDENTIFIER, apiOutput);
		responseNode.set(OUTPUT_FILE, rawOutput);
		responseNode.put(FINAL_STATUS, finalStatus);
		responseNode.put(VERIFYID, verifyId);

		return responseNode;
	}

	@Override
	public ObjectNode setWatchoutResponse(ObjectMapper mapper, JsonNode apiResponseNode) {
		JsonNode watchOutResponseNode = apiResponseNode.has("WatchOut") ? apiResponseNode.get("WatchOut")
				: mapper.createObjectNode();
		JsonNode apiOutput = watchOutResponseNode.has("WatchOut Output") ? watchOutResponseNode.get("WatchOut Output")
				: mapper.createObjectNode();
		JsonNode rawOutput = watchOutResponseNode.has(RAW_OUTPUT) ? watchOutResponseNode.get(RAW_OUTPUT)
				: mapper.createObjectNode();
		JsonNode apiInput = watchOutResponseNode.has("WatchOut Input") ? watchOutResponseNode.get("WatchOut Input")
				: mapper.createObjectNode();
		String finalStatus = watchOutResponseNode.has(STATUS) ? watchOutResponseNode.get(STATUS).asText() : "";
		String verifyId = watchOutResponseNode.has(VERIFY_ID) ? watchOutResponseNode.get(VERIFY_ID).asText() : "";

		if (!StringUtils.equalsAnyIgnoreCase(finalStatus, MANUAL, CLEAR, RECORD_FOUND)) {
			finalStatus = PENDING;
		}
		ObjectNode responseNode = mapper.createObjectNode();
		responseNode.put(API_NAME, WATCHOUT);
		responseNode.set(INPUT_FILE, apiInput);
		responseNode.set(MATCHED_IDENTIFIER, apiOutput);
		responseNode.set(OUTPUT_FILE, rawOutput);
		responseNode.put(FINAL_STATUS, finalStatus);
		responseNode.put(VERIFYID, verifyId);

		return responseNode;
	}

	@Override
	public ObjectNode setWorldCheckResponse(ObjectMapper mapper, JsonNode apiResponseNode) {
		JsonNode worldCheckNode = apiResponseNode.has("WorldCheck") ? apiResponseNode.get("WorldCheck")
				: mapper.createObjectNode();
		JsonNode apiOutput = worldCheckNode.has("World Check Output") ? worldCheckNode.get("World Check Output")
				: mapper.createObjectNode();
		JsonNode rawOutput = worldCheckNode.has(RAW_OUTPUT) ? worldCheckNode.get(RAW_OUTPUT)
				: mapper.createObjectNode();
		JsonNode apiInput = worldCheckNode.has("World Check Input") ? worldCheckNode.get("World Check Input")
				: mapper.createObjectNode();
		String finalStatus = worldCheckNode.has(STATUS) ? worldCheckNode.get(STATUS).asText() : "";
		String verifyId = worldCheckNode.has(VERIFY_ID) ? worldCheckNode.get(VERIFY_ID).asText() : "";

		if (!StringUtils.equalsAnyIgnoreCase(finalStatus, MANUAL, CLEAR, RECORD_FOUND)) {
			finalStatus = PENDING;
		}
		ObjectNode responseNode = mapper.createObjectNode();
		responseNode.put(API_NAME, WORLD_CHECK);
		responseNode.set(INPUT_FILE, apiInput);
		responseNode.set(MATCHED_IDENTIFIER, apiOutput);
		responseNode.set(OUTPUT_FILE, rawOutput);
		responseNode.put(FINAL_STATUS, finalStatus);
		responseNode.put(VERIFYID, verifyId);

		return responseNode;
	}

	@Override
	public ObjectNode setAdverseMediaResponse(ObjectMapper mapper, JsonNode apiResponseNode) {
		JsonNode adverseMediaNode = apiResponseNode.has(ADVERSE_MEDIA) ? apiResponseNode.get(ADVERSE_MEDIA)
				: mapper.createObjectNode();
		JsonNode apiOutput = adverseMediaNode.has("Adverse Media Output") ? adverseMediaNode.get("Adverse Media Output")
				: mapper.createObjectNode();
		JsonNode rawOutput = adverseMediaNode.has(RAW_OUTPUT) ? adverseMediaNode.get(RAW_OUTPUT)
				: mapper.createObjectNode();
		JsonNode apiInput = adverseMediaNode.has("Adverse media Input") ? adverseMediaNode.get("Adverse media Input")
				: mapper.createObjectNode();
		String finalStatus = adverseMediaNode.has(STATUS) ? adverseMediaNode.get(STATUS).asText() : "";
		String verifyId = adverseMediaNode.has(VERIFY_ID) ? adverseMediaNode.get(VERIFY_ID).asText() : "";

		if (!StringUtils.equalsAnyIgnoreCase(finalStatus, MANUAL, CLEAR, RECORD_FOUND)) {
			finalStatus = PENDING;
		}
		ObjectNode responseNode = mapper.createObjectNode();
		responseNode.put(API_NAME, ADVERSE_MEDIA);
		responseNode.set(INPUT_FILE, apiInput);
		responseNode.set(MATCHED_IDENTIFIER, apiOutput);
		responseNode.set(OUTPUT_FILE, rawOutput);
		responseNode.put(FINAL_STATUS, finalStatus);
		responseNode.put(VERIFYID, verifyId);

		return responseNode;
	}

	@Override
	public ObjectNode setLoanDefaulterResponse(ObjectMapper mapper, JsonNode apiResponseNode) {
		JsonNode loanResponseNode = apiResponseNode.has(LOAN_DEFAULTER) ? apiResponseNode.get(LOAN_DEFAULTER)
				: mapper.createObjectNode();
		JsonNode apiOutput = loanResponseNode.has("Loan Defaulter Output")
				? loanResponseNode.get("Loan Defaulter Output")
				: mapper.createObjectNode();
		JsonNode rawOutput = loanResponseNode.has(RAW_OUTPUT) ? loanResponseNode.get(RAW_OUTPUT)
				: mapper.createObjectNode();
		JsonNode apiInput = loanResponseNode.has("Loan Defaulter Input") ? loanResponseNode.get("Loan Defaulter Input")
				: mapper.createObjectNode();
		String finalStatus = loanResponseNode.has(STATUS) ? loanResponseNode.get(STATUS).asText() : "";
		String verifyId = loanResponseNode.has(VERIFY_ID) ? loanResponseNode.get(VERIFY_ID).asText() : "";

		if (!StringUtils.equalsAnyIgnoreCase(finalStatus, MANUAL, CLEAR, RECORD_FOUND)) {
			finalStatus = PENDING;
		}
		ObjectNode responseNode = mapper.createObjectNode();
		responseNode.put(API_NAME, LOAN_DEFAULTER);
		responseNode.set(INPUT_FILE, apiInput);
		responseNode.set(MATCHED_IDENTIFIER, apiOutput);
		responseNode.set(OUTPUT_FILE, rawOutput);
		responseNode.put(FINAL_STATUS, finalStatus);
		responseNode.put(VERIFYID, verifyId);

		return responseNode;
	}
}
