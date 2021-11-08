package com.gic.fadv.online.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.online.model.PersonInfoSearch;
import com.gic.fadv.online.utility.Utility;

@Service
public class ParseAPIResponseServiceImpl implements ParseAPIResponseService {

	private static final Logger logger = LoggerFactory.getLogger(ParseAPIResponseServiceImpl.class);

	@Autowired
	private ApiService apiService;
	@Value("${online.worldcheck.profile.rest.url}")
	private String profileUrl;

	@Override
	public ObjectNode parseAdverseMediaResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String personalResponse, ObjectNode resultAdverseMedia) {

		boolean clear = false;
		boolean manual = false;

		String clearStr = "";
		String manualStr = "";
		ArrayNode links = mapper.createArrayNode();
		if (personalResponse == null) {
			// resultAdverseMedia.put("Adverse media Output", "Auto Tag Web & Media as
			// clear");
			clear = true;
			clearStr = "Auto Tag Web & Media as clear";
			logger.info("Auto Tag Web & Media as clear" + resultAdverseMedia);
		} else {
			logger.info("Auto Tag Web & Media Else Part");
			JsonNode resultResponse = null;
			try {
				resultResponse = (ObjectNode) mapper.readTree(personalResponse);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (resultResponse != null) {
				ArrayNode results = resultResponse.has("results") ? (ArrayNode) resultResponse.get("results")
						: mapper.createArrayNode();
				ArrayNode data = mapper.createArrayNode();
				if (results != null && !results.isEmpty()) {
					data = results.get(0).has("data") ? (ArrayNode) results.get(0).get("data")
							: mapper.createArrayNode();
				}
				if (data != null && !data.isEmpty()) {
					for (int i = 0; i < data.size(); i++) {
						// JsonNode nameNode = data.get(0).get("name");
						JsonNode nameNode = data.get(i).get("short_article");
						if (nameNode != null) {
							// String name = data.get(0).get("name").asText();
							String shortArticle = data.get(i).get("short_article").asText();
							// Split on the basis of ,

							// if
							// (shortArticle.toLowerCase().contains(personalInfoSearch.getName().toLowerCase()))
							// {
							if (Utility.checkContains(shortArticle, personalInfoSearch.getName())) {
								/*
								 * if (data.get(i).get("address").asText().equalsIgnoreCase(personalInfoSearch.
								 * getAddress())) { logger.info(
								 * "(Primary Match) Send for Manual review under web and Media Provide copy of the"
								 * + " hyperlink from FD : Link");
								 * resultAdverseMedia.put("Adverse media Output",
								 * "Send for Manual review under web and Media Provide copy of the" +
								 * " hyperlink from FD : Link"); resultAdverseMedia.put("Link",
								 * data.get(0).get("link").asText()); resultAdverseMedia.put("Annexure",
								 * data.get(0).get("url").asText()); resultAdverseMedia.put("Link",
								 * data.get(i).get("link").asText()); resultAdverseMedia.put("Annexure",
								 * data.get(i).get("url").asText()); break; } else {
								 */
								logger.info("(Secondary Match) Send for Manual review under Web & Media "
										+ " Provide copy of the hyperlink from FD : Link");
//										resultAdverseMedia.put("Adverse media Output", "Send for Manual review "
//												+ "under Web & Media  Provide copy of the hyperlink from FD :" + "Link");
								manual = true;
								manualStr = "Send for Manual review "
										+ "under Web & Media  Provide copy of the hyperlink from FD :" + "Link";
								// resultAdverseMedia.put("Link", data.get(0).get("link").asText());
								// resultAdverseMedia.put("Annexure", data.get(0).get("url").asText());

								if (data.get(i).get("link") != null) {
									// resultAdverseMedia.put("Link", data.get(i).get("link").asText());
									links.add(data.get(i).get("link").asText());
								}
								/*
								 * if(data.get(i).get("url")!=null) { resultAdverseMedia.put("Annexure",
								 * data.get(i).get("url").asText()); }else {
								 * resultAdverseMedia.put("Annexure","Data for url is Empty/Null"); }
								 */
							} else {
								clear = true;
								clearStr = "Auto Tag Web & Media as clear ";
								// resultAdverseMedia.put("Adverse media Output", "Auto Tag Web & Media as clear
								// ");
								logger.info("Auto Tag Cross directorship as clear" + resultAdverseMedia);
							}

							/*
							 * if (name.equalsIgnoreCase(personalInfoSearch.getName())) { if
							 * (data.get(0).get("address").asText().equalsIgnoreCase(personalInfoSearch.
							 * getAddress())) { logger.info(
							 * "(Primary Match) Send for Manual review under web and Media Provide copy of the"
							 * + " hyperlink from FD : Link");
							 * resultAdverseMedia.put("Adverse media Output",
							 * "Send for Manual review under web and Media Provide copy of the" +
							 * " hyperlink from FD : Link"); resultAdverseMedia.put("Link",
							 * data.get(0).get("link").asText()); resultAdverseMedia.put("Annexure",
							 * data.get(0).get("url").asText()); } else {
							 * logger.info("(Secondary Match) Send for Manual review under Web & Media " +
							 * " Provide copy of the hyperlink from FD : Link");
							 * resultAdverseMedia.put("Adverse media Output", "Send for Manual review " +
							 * "under Web & Media  Provide copy of the hyperlink from FD :" + "Link");
							 * resultAdverseMedia.put("Link", data.get(0).get("link").asText());
							 * resultAdverseMedia.put("Annexure", data.get(0).get("url").asText()); } } else
							 * { resultAdverseMedia.put("Adverse media Output",
							 * "Auto Tag Web & Media as clear ");
							 * logger.info("Auto Tag Cross directorship as clear" + resultAdverseMedia); }
							 */
						} else {
							logger.info("Name is Null");
							clear = true;
							clearStr = "Auto Tag Web & Media as clear";
							// resultAdverseMedia.put("Adverse media Output", "Auto Tag Web & Media as
							// clear");
							logger.info("Auto Tag Cross directorship as clear" + resultAdverseMedia);
						}
					}
				} else {
					logger.info("data is Empty");
					clear = true;
					clearStr = "Auto Tag Web & Media. Data is Empty";
					resultAdverseMedia.put("Adverse Media Output", "Auto Tag Web & Media. Data is Empty");
				}
			}
		}

		String finalResult = "";
		String finalStatus = "";
		if (manual) {
			finalResult = manualStr;
			finalStatus = "Manual";
			resultAdverseMedia.set("Links", links);
		} else if (clear) {
			finalResult = clearStr;
			finalStatus = "Clear";
		}

		resultAdverseMedia.put("Adverse Media Output", finalResult);
		resultAdverseMedia.put("status", finalStatus);

		return resultAdverseMedia;
	}

	@Override
	public ObjectNode parseLoanDefaulterResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String personalResponse, ObjectNode resultLoan) {

		boolean clear = false;
		boolean manual = false;

		String clearStr = "";
		String manualStr = "";
		ArrayNode links = mapper.createArrayNode();
		ArrayNode annexureList = mapper.createArrayNode();

		if (personalResponse == null) {
			// resultLoan.put("Loan Defaulter Output", "Auto Tag Loan Defaulter as Clear");
			clear = true;
			clearStr = "Auto Tag Loan Defaulter as Clear";
			logger.info("Auto Tag Loan Defaulter as Clear" + resultLoan);
		} else {
			logger.info("Auto Tag Loan Defaulter Else Part" + resultLoan);
			JsonNode resultResponse = null;
			try {
				resultResponse = (ObjectNode) mapper.readTree(personalResponse);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (resultResponse != null) {
				ArrayNode results = (ArrayNode) resultResponse.get("results");
				ArrayNode data = (ArrayNode) results.get(0).get("data");
				if (data != null) {
					if (data.size() > 0) {
						for (int i = 0; i < data.size(); i++) {
							/* if (data.get(i).get("sub_type") != null) { */
							// if (data.get(i).get("sub_type").asText().equalsIgnoreCase("cibil
							// defaulters")) {
							/*
							 * if
							 * (data.get(i).get("name").asText().equalsIgnoreCase(personalInfoSearch.getName
							 * ())) {
							 */
							if (Utility.checkContains(data.get(i).get("name").asText(), personalInfoSearch.getName())) {
								if (data.get(i).get("address").asText()
										.equalsIgnoreCase(personalInfoSearch.getAddress())) {
									logger.info(" (Primary Match)Send for Manual review.Provide copy of the hyperlink "
											+ "from FD : Link & FD: url for Annexure");
//											resultLoan.put("Loan Defaulter Output",
//													" (Primary Match)Send for Manual review.Provide copy of the hyperlink "
//															+ "from FD : Link & FD: url for Annexure");
									manual = true;
									manualStr = " (Primary Match)Send for Manual review.Provide copy of the hyperlink "
											+ "from FD : Link & FD: url for Annexure";
									// resultLoan.put("Link", data.get(i).get("link").asText());
									// resultLoan.put("Annexure", data.get(i).get("url").asText());
									if (data.get(i).get("link") != null) {
										links.add(data.get(i).get("link").asText());
									}
									if (data.get(i).get("url") != null) {
										annexureList.add(data.get(i).get("url").asText());
									}

									// break;
								} else {
									String stateName = data.get(i).get("state_name") != null
											? data.get(i).get("state_name").asText()
											: "";
									// if
									// (data.get(i).get("state_name").asText().equalsIgnoreCase(personalInfoSearch.getState()))
									// {
									if (stateName.equalsIgnoreCase(personalInfoSearch.getState())) {
										logger.info(
												"Output:- (Secondary Match). Send for Manual review. Provide copy of"
														+ " the hyperlink from FD : Link & FD: url for Annexure");
//												resultLoan.put("Loan Defaulter Output",
//														"Output:- (Secondary Match). Send for Manual review. Provide copy of"
//																+ "the hyperlink from FD : Link & FD: url for Annexure");
										manual = true;
										manualStr = "Output:- (Secondary Match). Send for Manual review. Provide copy of"
												+ "the hyperlink from FD : Link & FD: url for Annexure";
										// resultLoan.put("Link", data.get(i).get("link").asText());
										// resultLoan.put("Annexure", data.get(i).get("url").asText());
										if (data.get(i).get("link") != null) {
											links.add(data.get(i).get("link").asText());
										}
										if (data.get(i).get("url") != null) {
											annexureList.add(data.get(i).get("url").asText());
										}
										// break;
									} else {
										// resultLoan.put("Loan Defaulter Output", "Auto Tag Loan Defaulter as Clear");
										logger.info("Output:- (Tertiary Match). Send for Manual review. Provide copy of"
												+ " the hyperlink from FD : Link & FD: url for Annexure");
//												resultLoan.put("Loan Defaulter Output",
//														"Output:- (Secondary Match). Send for Manual review. Provide copy of"
//																+ "the hyperlink from FD : Link & FD: url for Annexure");
										manual = true;
										manualStr = "Output:- (Tertiary Match). Send for Manual review. Provide copy of"
												+ "the hyperlink from FD : Link & FD: url for Annexure";
										// resultLoan.put("Link", data.get(i).get("link").asText());
										// resultLoan.put("Annexure", data.get(i).get("url").asText());
										if (data.get(i).get("link") != null) {
											links.add(data.get(i).get("link").asText());
										}
										if (data.get(i).get("url") != null) {
											annexureList.add(data.get(i).get("url").asText());
										}
										// break;
									}
								}
							} else {
								// resultLoan.put("Loan Defaulter Output", "Auto Tag Loan Defaulter as Clear");
								clear = true;
								clearStr = "Auto Tag Loan Defaulter as Clear";
								logger.info("Auto Tag Loan Defaulter as Clear" + resultLoan);
							}
							/*
							 * } else { logger.info("Sub Type is not cibil"); }
							 */
							/*
							 * } else { logger.info("Sub Type is null"); }
							 */
						}
					} else {
						clear = true;
						clearStr = "Data is Empty.Auto Tag Loan Defaulter as Clear";
						// resultLoan.put("Loan Defaulter Output", "Data is Empty.Auto Tag Loan
						// Defaulter as Clear");
						logger.info("Data is Empty");
					}
				} else {
					logger.info("Data is Null for cibil");
				}
				// resultLoan.put("Loan Defaulter Output", "Auto Tag Loan Defaulter Else Part");
			}

		}
		String finalResult = "";
		String finalStatus = "";
		if (manual) {
			finalResult = manualStr;
			finalStatus = "Manual";
			resultLoan.set("Link", links);
			resultLoan.set("Annexure", annexureList);
		} else if (clear) {
			finalResult = clearStr;
			finalStatus = "Clear";
		}

		resultLoan.put("Loan Defaulter Output", finalResult);
		resultLoan.put("status", finalStatus);

		return resultLoan;
	}

	@Override
	public ObjectNode parseManupatraResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String manuPatraResponse, ObjectNode resultManupatra) {

		boolean clear = false;
		boolean manual = false;
		boolean gateWayTimeout = false;
		String gateWayTimeoutStr = "";
		String clearStr = "";
		String manualStr = "";

		if (manuPatraResponse == null) {
			// resultManupatra.put("ManuPatra Output", "Auto Tag Civil Litigation as
			// clear");
			gateWayTimeout = true;
			gateWayTimeoutStr = "GateWay Time Out";
			logger.info("GateWay Time Out" + resultManupatra);
			/*
			 * clearStr = "Auto Tag Civil Litigation as clear";
			 * logger.info("Auto Tag Civil Litigation as clear" + resultManupatra);
			 */
		} else {
			logger.info("Auto Tag Civil Litigation Else Part" + resultManupatra);
			JsonNode resultResponse = null;
			try {
				resultResponse = (ObjectNode) mapper.readTree(manuPatraResponse);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (resultResponse != null) {

			}
			logger.info("Value of Raw Data" + resultResponse.get("rawData"));
			// System.out.println("Value of Raw Data"+resultResponse.get("rawData"));
			if (resultResponse.get("rawData") != null) {
				logger.info("Length of Raw Data" + resultResponse.get("rawData").asText().length());
				if (resultResponse.get("rawData").asText().length() > 1) {
					String rawDataStr = resultResponse.get("rawData").asText();
					JsonNode rawDataStrNode = null;
					try {
						rawDataStrNode = (ObjectNode) mapper.readTree(rawDataStr);
					} catch (JsonProcessingException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					JsonNode rawDataStrNodeDataNode = null;
					if (rawDataStrNode != null) {
						logger.info("Value of JsonNode rawData" + rawDataStrNode);
						// Take Data From RAW DATA
						rawDataStrNodeDataNode = rawDataStrNode.get("data");
					}
					if (rawDataStrNodeDataNode != null) {
						logger.info("Size of ArrayNode" + rawDataStrNodeDataNode.size());
						logger.info("Value of ArrayNode" + rawDataStrNodeDataNode);
						ArrayNode dataListValue = null;
						try {
							dataListValue = (ArrayNode) mapper.readTree(rawDataStrNodeDataNode.asText());
						} catch (JsonProcessingException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						if (dataListValue != null) {
							logger.info("Value of Data in Json" + dataListValue);
							/*
							 * Iterate for file data in each data ArrayNode
							 */
							logger.info("Size of Data List Value" + dataListValue.size());
							/*
							 * Split Name in First name, Middle Name and last Name
							 */
							String[] names = personalInfoSearch.getName().split(" ");
							String firstName = null, lastName = null, middleName = null;
							if (names.length == 3) {
								firstName = names[0];
								lastName = names[2];
								middleName = names[1];
							} else if (names.length == 2) {
								firstName = names[0];
								lastName = names[1];
							} else {
								firstName = personalInfoSearch.getName();
							}
							Boolean nameMatchflag = false, firstNameMatchFlag = false, lastNameMatchFlag = false;
							ObjectNode firstNameMatchResultNode = mapper.createObjectNode();
							ObjectNode lastNameMatchResultNode = mapper.createObjectNode();
							for (int i = 0; i < dataListValue.size(); i++) {
								logger.info("Value of title in Data Json" + dataListValue.get(i).get("title"));
								logger.info("Value of title in Data Json" + dataListValue.get(i).get("fileData"));
								JsonNode fileDataNode = dataListValue.get(i).get("fileData");
								JsonNode titleNode = dataListValue.get(i).get("title");
								if (fileDataNode != null) {
									if (fileDataNode.asText().contains(personalInfoSearch.getName())) {
										nameMatchflag = true;
										logger.info(
												"Match Found!Search for Other Parameters (address / State/ City / Father’s name) in fileData");
										String fileDataStr = fileDataNode.asText();
										JsonNode fileDataValue = null;
										try {
											fileDataValue = mapper.readTree(fileDataStr);
										} catch (JsonProcessingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										if (fileDataValue != null) {
											logger.info("Value of Data in Json" + fileDataValue);
											logger.info("Value of Result in Data Json" + fileDataValue.get("Result"));
											// Since Result and File Data Doesn't have (address / State/ City / Father’s
											// name)
											// So, putting the else condition in ManuPatra Output
											// (Secondary Match): Send for Manual review under India Civil Litigation &
											// Criminal 
											// Provide copy of the hyperlink from FD : Filedata
											// resultManupatra.put("ManuPatra Output", "Send for Manual review under
											// India Civil Litigation & Criminal");
											manual = true;
											manualStr = "Send for Manual review under India Civil Litigation & Criminal";
											resultManupatra.put("Matched Key Name", personalInfoSearch.getName());
											if (fileDataValue.get("Result") != null) {
												resultManupatra.put("ManuPatra FileData",
														fileDataValue.get("Result").asText());

											} else {
												resultManupatra.put("ManuPatra FileData",
														"file data Result field is Empty/Null");
											}
										}

										// break;
										// If address and other match found then
										// (Primary Match)  Send for Manual review under India Civil Litigation &
										// Criminal.
										// Provide copy of the hyperlink from FD : Filedata

									} else if (firstName != null && fileDataNode.asText().contains(firstName)) {
										firstNameMatchFlag = true;
										/* Store all data in a Array Node */
										logger.info(
												"Match Found!Search for Other Parameters (address / State/ City / Father’s name) in fileData");
										String fileDataStr = fileDataNode.asText();
										JsonNode fileDataValue = null;
										try {
											fileDataValue = mapper.readTree(fileDataStr);
										} catch (JsonProcessingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										if (fileDataValue != null) {
											logger.info("Value of Data in Json" + fileDataValue);
											logger.info("Value of Result in Data Json" + fileDataValue.get("Result"));

											// firstNameMatchResultNode.put("ManuPatra Output", "Send for Manual review
											// under India Civil Litigation & Criminal");
											manual = true;
											manualStr = "Send for Manual review under India Civil Litigation & Criminal";
											if (fileDataValue.get("Result") != null) {
												firstNameMatchResultNode.put("ManuPatra FileData",
														fileDataValue.get("Result").asText());

											} else {
												firstNameMatchResultNode.put("ManuPatra FileData",
														"file data Result field is Empty/Null");
											}
										}

										firstNameMatchResultNode.put("Matched Key Name", firstName);
									} else if (lastName != null && fileDataNode.asText().contains(lastName)) {
										lastNameMatchFlag = true;
										logger.info(
												"Match Found!Search for Other Parameters (address / State/ City / Father’s name) in fileData");
										String fileDataStr = fileDataNode.asText();
										JsonNode fileDataValue = null;
										try {
											fileDataValue = mapper.readTree(fileDataStr);
										} catch (JsonProcessingException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										if (fileDataValue != null) {
											logger.info("Value of Data in Json" + fileDataValue);
											logger.info("Value of Result in Data Json" + fileDataValue.get("Result"));
											// lastNameMatchResultNode.put("ManuPatra Output", "Send for Manual review
											// under India Civil Litigation & Criminal");
											manual = true;
											manualStr = "Send for Manual review under India Civil Litigation & Criminal";
											if (fileDataValue.get("Result") != null) {
												lastNameMatchResultNode.put("ManuPatra FileData",
														fileDataValue.get("Result").asText());

											} else {
												lastNameMatchResultNode.put("ManuPatra FileData",
														"file data Result field is Empty/Null");
											}
										}

										lastNameMatchResultNode.put("Matched Key Name", lastName);
									}
								}
							}
							if (!nameMatchflag) {
								/*
								 * Let's Check for first name Search
								 */
								if (firstNameMatchFlag) {
									resultManupatra.set("ManuPatra Output",
											firstNameMatchResultNode.get("ManuPatra Output"));
									resultManupatra.set("Matched Key Name",
											firstNameMatchResultNode.get("Matched Key Name"));
									resultManupatra.set("ManuPatra FileData",
											firstNameMatchResultNode.get("ManuPatra FileData"));

								} else if (lastNameMatchFlag) {
									resultManupatra.set("ManuPatra Output",
											lastNameMatchResultNode.get("ManuPatra Output"));
									resultManupatra.set("Matched Key Name",
											lastNameMatchResultNode.get("Matched Key Name"));
									resultManupatra.set("ManuPatra FileData",
											lastNameMatchResultNode.get("ManuPatra FileData"));
								} else {
									logger.info("nameMatchflag is false");
									clear = true;
									clearStr = "Auto Tag Civil Litigation as clear";
									// resultManupatra.put("ManuPatra Output", "Auto Tag Civil Litigation as
									// clear");
								}
							}
						}
					} else {
						clear = true;
						clearStr = "Auto Tag Civil Litigation as clear.Data field of Raw Data is Empty";
						// resultManupatra.put("ManuPatra Output", "Auto Tag Civil Litigation as
						// clear.Data field of Raw Data is Empty");
						logger.info("Raw Data is Empty");
					}
				} else {
					clear = true;
					clearStr = "Raw Data is Empty.Auto Tag Civil Litigation as clear";
					// resultManupatra.put("ManuPatra Output", "Raw Data is Empty.Auto Tag Civil
					// Litigation as clear");
					logger.info("Raw Data is Empty");
				}
			} else {
				clear = true;
				clearStr = "Raw Data is Null.Auto Tag Civil Litigation as clear";
				// resultManupatra.put("ManuPatra Output", "Raw Data is Null.Auto Tag Civil
				// Litigation as clear");
				logger.info("Raw Data is Null");
			}
		}

		String finalResult = "";
		String finalStatus = "";
		if (gateWayTimeout) {
			finalResult = gateWayTimeoutStr;
			finalStatus = "GateWayTimeOut";
		} else if (manual) {
			finalResult = manualStr;
			finalStatus = "Manual";
		} else if (clear) {
			finalResult = clearStr;
			finalStatus = "Clear";
		}

		resultManupatra.put("ManuPatra Output", finalResult);
		resultManupatra.put("status", finalStatus);

		return resultManupatra;
	}

	@Override
	public String parseMCAResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch, String personalResponse,
			ObjectNode resultMCA) {

		boolean clear = false;
		boolean manual = false;
		boolean recordFound = false;
		boolean pending = false;

		String clearStr = "";
		String manualStr = "";
		String recordFoundStr = "";
		String pendingStr = "";
		String din = null;
		if (personalResponse == null) {
			// resultMCA.put("MCA Output", "Auto Tag Cross directorship as clear");
			clearStr = "Auto Tag Cross directorship as clear";
			logger.info("Auto Tag Cross directorship as clear" + resultMCA);
			clear = true;
		} else {
			JsonNode resultResponse = null;
			try {
				resultResponse = (ObjectNode) mapper.readTree(personalResponse);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (resultResponse != null) {

				if (resultResponse.has("status") && resultResponse.get("status").asInt() == 0) {
					pending = true;
					pendingStr = "Request is Pending";
					logger.info("Request Pending" + resultResponse);
				} else {
					ArrayNode results = (ArrayNode) resultResponse.get("results");
					if (results != null) {
						if (results.get(0).has("status")) {

							ArrayNode data = (ArrayNode) results.get(0).get("data");
							if (data != null && data.size() > 0) {
								for (int i = 0; i < data.get(0).size(); i++) {
									JsonNode innerNode = data.get(0).get(i);
									JsonNode nameNode;
									JsonNode typNode;
									String typeNode = "";

									if (innerNode.isArray()) {
										nameNode = innerNode.get("name");
										typNode = innerNode.get("type");
									} else {
										nameNode = innerNode.get("name");
										typNode = innerNode.get("type");
									}

									if (typNode != null) {
										typeNode = typNode.asText();
									}

									/////////////////////// Service type///////////////////////////////////

									if (typeNode.equalsIgnoreCase("mca_dob")) {
										// ArrayNode data=(ArrayNode)results.get(0).get("data");
										// Check for data Size and iterate over it
										// if(data!=null && data.size()>0) {
										Boolean nameFlag = false;

										// JsonNode nameNode=data.get(i).get("name");
										if (nameNode != null) {
											// String name= data.get(i).get("name").asText();
											String name = nameNode.asText();
											logger.info("Value of name" + name);
											logger.info("Value of personal Info Search" + personalInfoSearch.getName());

											if (name.equalsIgnoreCase(personalInfoSearch.getName())) {
												// resultMCA.put("MCA Output","Auto Tag Cross directorship as Record
												// found with Date of Birth Match");
												recordFound = true;
												recordFoundStr = "Auto Tag Cross directorship as Record found with Date of Birth Match";
												logger.info("Auto Tag Cross directorship as Record found" + resultMCA);
												nameFlag = true;
												JsonNode dinNode;
												// JsonNode dinNode =data.get(i).get("din");
												if (innerNode.isArray()) {
													dinNode = innerNode.get("din");
												} else {
													dinNode = innerNode.get("din");
												}
												if (dinNode != null) {
													din = dinNode.asText();
												}
												// break;
											} else {
												if (Utility.checkContains(name, personalInfoSearch.getName())) {
													manual = true;
													manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
													logger.info(
															"Auto Tag Cross directorship as Record found" + resultMCA);
													nameFlag = true;
												} else {
													/*
													 * Split Name in First name, Middle Name and last Name
													 */
													String[] names = personalInfoSearch.getName().split(" ");
													logger.info("Value of String After Split" + names);
													String firstName = null, lastName = null, middleName = null;
													if (names.length == 3) {
														firstName = names[0];
														lastName = names[2];
														middleName = names[1];
														logger.info("Value of Split Names" + firstName + lastName
																+ middleName + name);
														if (Utility.checkContains(name, firstName)
																&& Utility.checkContains(name, lastName)) {
															recordFound = true;
															recordFoundStr = "Auto Tag Cross directorship as Record found with Date of Birth Match";
															logger.info("Auto Tag Cross directorship as Record found"
																	+ resultMCA);
															nameFlag = true;
															JsonNode dinNode;
															// JsonNode dinNode =data.get(i).get("din");
															if (innerNode.isArray()) {
																dinNode = innerNode.get("din");
															} else {
																dinNode = innerNode.get("din");
															}
															if (dinNode != null) {
																din = dinNode.asText();
															}

														} else if (Utility.checkContains(name, firstName)
																|| Utility.checkContains(name, lastName)) {
															manual = true;
															manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
															logger.info("Auto Tag Cross directorship as Record found"
																	+ resultMCA);
															nameFlag = true;
														}
													} else if (names.length == 2) {
														firstName = names[0];
														lastName = names[1];
														logger.info(
																"Value of Split Names" + firstName + lastName + name);
														if (Utility.checkContains(name, firstName)
																&& Utility.checkContains(name, lastName)) {
															recordFound = true;
															recordFoundStr = "Auto Tag Cross directorship as Record found with Date of Birth Match";
															logger.info("Auto Tag Cross directorship as Record found"
																	+ resultMCA);
															nameFlag = true;
															JsonNode dinNode;
															// JsonNode dinNode =data.get(i).get("din");
															if (innerNode.isArray()) {
																dinNode = innerNode.get("din");
															} else {
																dinNode = innerNode.get("din");
															}
															if (dinNode != null) {
																din = dinNode.asText();
															}

														} else if (Utility.checkContains(name, firstName)
																|| Utility.checkContains(name, lastName)) {
															manual = true;
															manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
															logger.info("Auto Tag Cross directorship as Record found"
																	+ resultMCA);
															nameFlag = true;
														}
													} else {
														firstName = personalInfoSearch.getName();
														logger.info("Value of Split Names" + firstName + name);
														if (Utility.checkContains(name, firstName)) {
															manual = true;
															manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
															logger.info("Auto Tag Cross directorship as Record found"
																	+ resultMCA);
															nameFlag = true;
														}
													}
												}
											}
										}
										if (!nameFlag) {
											// resultMCA.put("MCA Output","Auto Tag Cross directorship as clear");
											clear = true;
											clearStr = "Auto Tag Cross directorship as clear";
											logger.info("Auto Tag Cross directorship as clear" + resultMCA);
										}
//										}else{
//											//resultMCA.put("MCA Output","Data is Empty.Auto Tag Cross directorship as clear");
//											logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
//											clear = true;
//											clearStr = "Data is Empty.Auto Tag Cross directorship as clear";
//										}			  
									} else if (typeNode.equalsIgnoreCase("mca_inhouse")
											|| typeNode.equalsIgnoreCase("google_dir")) {
										// ArrayNode data=(ArrayNode)results.get(0).get("data");
										// Check for data Size and iterate over it
										// if(data!=null && data.size()>0) {
										Boolean nameFlag = false;
										// for(int i=0;i<data.size();i++) {
//											  JsonNode nameNode;
//											  if(data.get(i).isArray()) {
//												 nameNode =data.get(i).get(0).get("name");
//											  }else {
//												  nameNode=data.get(i).get("name");
//											  }
										// JsonNode nameNode=data.get(i).get("name");
										if (nameNode != null) {
											// String name= data.get(i).get("name").asText();
											String name = nameNode.asText();
											logger.info("Value of name" + name);
											logger.info("Value of personal Info Search" + personalInfoSearch.getName());
											if (name.equalsIgnoreCase(personalInfoSearch.getName())) {
												// resultMCA.put("MCA Output","Send Cross directorship check for Manual
												// review as Record found with only Name match");
												manual = true;
												manualStr = "Send Cross directorship check for Manual review as Record found with only Name match";
												logger.info("Auto Tag Cross directorship as Record found" + resultMCA);
												nameFlag = true;
												JsonNode dinNode;
												// JsonNode dinNode =data.get(i).get("din");
//													  if(innerNode.isArray()) {
//														  dinNode =innerNode.get("din");
//														  }else {
//															  dinNode=innerNode.get("din");
//														  }
//													  if(dinNode!=null) {
//														  din=dinNode.asText(); 
//													  } 
												// break;
											} else {
												if (Utility.checkContains(name, personalInfoSearch.getName())) {
													manual = true;
													manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
													logger.info(
															"Auto Tag Cross directorship as Record found" + resultMCA);
													nameFlag = true;
												} else {
													/*
													 * Split Name in First name, Middle Name and last Name
													 */
													String[] names = personalInfoSearch.getName().split(" ");
													logger.info("Value of String After Split" + names);
													String firstName = null, lastName = null, middleName = null;
													if (names.length == 3) {
														firstName = names[0];
														lastName = names[2];
														middleName = names[1];
														logger.info("Value of Split Names" + firstName + lastName
																+ middleName + name);
														if (Utility.checkContains(name, firstName)
																|| Utility.checkContains(name, lastName)) {
															manual = true;
															manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
															logger.info("Auto Tag Cross directorship as Record found"
																	+ resultMCA);
															nameFlag = true;
														}
													} else if (names.length == 2) {
														firstName = names[0];
														lastName = names[1];
														logger.info(
																"Value of Split Names" + firstName + lastName + name);
														if (Utility.checkContains(name, firstName)
																|| Utility.checkContains(name, lastName)) {
															manual = true;
															manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
															logger.info("Auto Tag Cross directorship as Record found"
																	+ resultMCA);
															nameFlag = true;
														}
													} else {
														firstName = personalInfoSearch.getName();
														logger.info("Value of Split Names" + firstName + name);
														if (Utility.checkContains(name, firstName)) {
															manual = true;
															manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
															logger.info("Auto Tag Cross directorship as Record found"
																	+ resultMCA);
															nameFlag = true;
														}
													}
												}
											}
										}
										// }
										if (!nameFlag) {
											// resultMCA.put("MCA Output","Auto Tag Cross directorship as clear");
											clear = true;
											clearStr = "Auto Tag Cross directorship as clear";
											logger.info("Auto Tag Cross directorship as clear" + resultMCA);
										}
//										}else{
//											//resultMCA.put("MCA Output","Data is Empty.Auto Tag Cross directorship as clear");
//											clear = true;
//											clearStr = "Data is Empty.Auto Tag Cross directorship as clear";
//											logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
//										}	
									} else if (typeNode.equalsIgnoreCase("no_data")) {
										// resultMCA.put("MCA Output", "Auto Tag Cross directorship as clear.status
										// fields has value no_status");
										clear = true;
										clearStr = "Auto Tag Cross directorship as clear.status fields has value no_status";
										logger.info("Status is no_data");
									} else {
										// resultMCA.put("MCA Output", "Auto Tag Cross directorship as clear.");
										clear = true;
										clearStr = "Auto Tag Cross directorship as clear.";
										logger.info("Status is no_data");
									}

									/////////////////////////////////////////////////////////////
								}

							} else {
								clear = true;
								clearStr = "Auto Tag Cross directorship as clear.";
								logger.info("data is Empty");
							}

							String status = results.get(0).get("status").asText();
							logger.info("Value of Status is" + status);
							/*
							 * Logic for Status no_data
							 */
							if (status.equalsIgnoreCase("no_data")) {
								clear = true;
								clearStr = "Auto Tag Cross directorship as clear.";
								logger.info("Status is no_data");
							}
							List<String> statusList = new ArrayList<String>();
							if (status.contains(",")) {
								statusList = Arrays.asList(status.split(","));
							} else {
								statusList.add(status);
							}
							for (String status1 : statusList) {
								/*
								 * if(status1.equalsIgnoreCase("mca_dob")) { ArrayNode
								 * data=(ArrayNode)results.get(0).get("data"); //Check for data Size and iterate
								 * over it if(data!=null && data.size()>0) { Boolean nameFlag=false; for(int
								 * i=0;i<data.size();i++) { JsonNode nameNode; if(data.get(i).isArray()) {
								 * nameNode =data.get(i).get(0).get("name"); }else {
								 * nameNode=data.get(i).get("name"); } //JsonNode
								 * nameNode=data.get(i).get("name"); if(nameNode!=null) { //String name=
								 * data.get(i).get("name").asText(); String name= nameNode.asText();
								 * logger.info("Value of name"+name);
								 * logger.info("Value of personal Info Search"+personalInfoSearch.getName());
								 * 
								 * if(name.equalsIgnoreCase(personalInfoSearch.getName())) {
								 * //resultMCA.put("MCA Output"
								 * ,"Auto Tag Cross directorship as Record found with Date of Birth Match");
								 * recordFound = true; recordFoundStr =
								 * "Auto Tag Cross directorship as Record found with Date of Birth Match";
								 * logger.info("Auto Tag Cross directorship as Record found"+resultMCA);
								 * nameFlag=true; JsonNode dinNode; //JsonNode dinNode =data.get(i).get("din");
								 * if(data.get(i).isArray()) { dinNode =data.get(i).get(0).get("din"); }else {
								 * dinNode=data.get(i).get("din"); } if(dinNode!=null) { din=dinNode.asText(); }
								 * //break; } } } if(!nameFlag) {
								 * //resultMCA.put("MCA Output","Auto Tag Cross directorship as clear"); clear =
								 * true; clearStr = "Auto Tag Cross directorship as clear";
								 * logger.info("Auto Tag Cross directorship as clear"+resultMCA); } }else{
								 * //resultMCA.put("MCA Output"
								 * ,"Data is Empty.Auto Tag Cross directorship as clear");
								 * logger.info("Auto Tag Cross directorship as clear"+resultMCA); clear = true;
								 * clearStr = "Data is Empty.Auto Tag Cross directorship as clear"; } }else
								 * if(status1.equalsIgnoreCase("mca_inhouse") ||
								 * status1.equalsIgnoreCase("google_dir")){ ArrayNode
								 * data=(ArrayNode)results.get(0).get("data"); //Check for data Size and iterate
								 * over it if(data!=null && data.size()>0) { Boolean nameFlag=false; for(int
								 * i=0;i<data.size();i++) { JsonNode nameNode; if(data.get(i).isArray()) {
								 * nameNode =data.get(i).get(0).get("name"); }else {
								 * nameNode=data.get(i).get("name"); } //JsonNode
								 * nameNode=data.get(i).get("name"); if(nameNode!=null) { //String name=
								 * data.get(i).get("name").asText(); String name= nameNode.asText();
								 * logger.info("Value of name"+name);
								 * logger.info("Value of personal Info Search"+personalInfoSearch.getName());
								 * if(name.equalsIgnoreCase(personalInfoSearch.getName())) {
								 * //resultMCA.put("MCA Output"
								 * ,"Send Cross directorship check for Manual review as Record found with only Name match"
								 * ); manual = true; manualStr =
								 * "Send Cross directorship check for Manual review as Record found with only Name match"
								 * ; logger.info("Auto Tag Cross directorship as Record found"+resultMCA);
								 * nameFlag=true; JsonNode dinNode; //JsonNode dinNode =data.get(i).get("din");
								 * if(data.get(i).isArray()) { dinNode =data.get(i).get(0).get("din"); }else {
								 * dinNode=data.get(i).get("din"); } if(dinNode!=null) { din=dinNode.asText(); }
								 * //break; } } } if(!nameFlag) {
								 * //resultMCA.put("MCA Output","Auto Tag Cross directorship as clear"); clear =
								 * true; clearStr = "Auto Tag Cross directorship as clear";
								 * logger.info("Auto Tag Cross directorship as clear"+resultMCA); } }else{
								 * //resultMCA.put("MCA Output"
								 * ,"Data is Empty.Auto Tag Cross directorship as clear"); clear = true;
								 * clearStr = "Data is Empty.Auto Tag Cross directorship as clear";
								 * logger.info("Auto Tag Cross directorship as clear"+resultMCA); } }else
								 * if(status1.equalsIgnoreCase("no_data")) { //resultMCA.put("MCA Output",
								 * "Auto Tag Cross directorship as clear.status fields has value no_status");
								 * clear = true; clearStr =
								 * "Auto Tag Cross directorship as clear.status fields has value no_status";
								 * logger.info("Status is no_data"); }else { //resultMCA.put("MCA Output",
								 * "Auto Tag Cross directorship as clear."); clear = true; clearStr =
								 * "Auto Tag Cross directorship as clear."; logger.info("Status is no_data"); }
								 */
							}
						} else {
							// resultMCA.put("MCA Output", "Auto Tag Cross directorship as clear.Result has
							// no status fields");
							clear = true;
							clearStr = "Auto Tag Cross directorship as clear.Result has no status fields";
							logger.info("Result has no status fields");
						}
					} else {
						// resultMCA.put("MCA Output", "Auto Tag Cross directorship as clear.MCA
						// Response has no Result fields");
						clear = true;
						clearStr = "Auto Tag Cross directorship as clear.MCA Response has no Result fields";
						logger.info("MCA Response has no Result fields");
					}
				}
			}
		}
		String finalResult = "";
		String finalStatus = "";
		if (pending) {
			finalResult = pendingStr;
			finalStatus = "Pending Request";
		} else if (recordFound) {
			finalResult = recordFoundStr;
			finalStatus = "Record Found";
		} else if (manual) {
			finalResult = manualStr;
			finalStatus = "Manual";
		} else if (clear) {
			finalResult = clearStr;
			finalStatus = "Clear";
		}

		resultMCA.put("MCA Output", finalResult);
		resultMCA.put("status", finalStatus);
		// resultMCA.put("verify_id", finalStatus);
		return din;
	}

	@Override
	public ObjectNode parseWatchoutResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String fulfillmentResponse, String din, ObjectNode resultWatchOut) {

		boolean clear = false;
		boolean manual = false;
		boolean recordFound = false;

		String clearStr = "";
		String manualStr = "";
		String recordFoundStr = "";

		if (fulfillmentResponse == null) {
			// resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as
			// clear");
			clear = true;
			clearStr = "Auto Tag Disqualified directorship as clear";
			logger.info("Auto Tag Disqualified directorship as clear");
		} else {
			logger.info("Auto Tag Disqualified directorship Else Part" + resultWatchOut);
			JsonNode resultResponse = null;
			try {
				resultResponse = (ObjectNode) mapper.readTree(fulfillmentResponse);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (resultResponse != null) {
				logger.info("Value of Raw Data" + resultResponse.get("rawData"));
				logger.info("Value of raw data Empty" + resultResponse.get("rawData").isEmpty());

				if (resultResponse.get("rawData") != null) {
					logger.info("Length of Raw Data" + resultResponse.get("rawData").asText().length());
					if (resultResponse.get("rawData").asText().length() > 1) {
						String rawDataStr = resultResponse.get("rawData").asText();
						JsonNode rawDataStrNode = null;
						try {
							rawDataStrNode = (ObjectNode) mapper.readTree(rawDataStr);
						} catch (JsonProcessingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						logger.info("Value of JsonNode rawData" + rawDataStrNode);
						// Logic for Matching the PIN_CIN_DIN
						Boolean panFlag = false;
						if (rawDataStrNode.get("Result").has("Table")) {
							if (rawDataStrNode.get("Result").get("Table").isArray()) {
								ArrayNode tableList = (ArrayNode) rawDataStrNode.get("Result").get("Table");
								for (int i = 0; i < tableList.size(); i++) {
									if (tableList.get(i).get("PAN_CIN_DIN") != null) {
										String pan_cin_din = tableList.get(i).get("PAN_CIN_DIN").asText();
										if (pan_cin_din.equalsIgnoreCase("DIN:" + din)) {
											panFlag = true;
											logger.info("Auto Tag Disqualified directorship as records found");
											// resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship
											// as records found");
											recordFound = true;
											recordFoundStr = "Auto Tag Disqualified directorship as records found";
											resultWatchOut.put("WatchOut Annexure",
													tableList.get(i).get("Regulatory_Action_Source1").asText());
											resultWatchOut.put("WatchOut Summary",
													tableList.get(i).get("Regulatory_Charges").asText());
											// Provide copy of the hyperlink from FD : Regulatory_Action_Source1 for
											// Annexure
											// Provide summary of hit from FD Regulatory_Charges
										}
									}
								}
								// Logic for Matching the Defaulter_Name
								Boolean defaulterFlag = false;
								if (!panFlag) {
									for (int i = 0; i < tableList.size(); i++) {
										if (tableList.get(i).get("Defaulter_Name") != null) {
											String defaulterName = tableList.get(i).get("Defaulter_Name").asText();
											if (defaulterName.equalsIgnoreCase(personalInfoSearch.getName())) {
												logger.info("Send for Manual review");
												defaulterFlag = true;
												// resultWatchOut.put("WatchOut Output", "Send for Manual review");
												manual = true;
												manualStr = "Send for Manual review";
												resultWatchOut.put("WatchOut Annexure",
														tableList.get(i).get("Regulatory_Action_Source1").asText());
												resultWatchOut.put("WatchOut Summary",
														tableList.get(i).get("Regulatory_Charges").asText());
											}
										}
									}
								}
								if (!defaulterFlag) {
									// resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as
									// clear ");
									clear = true;
									clearStr = "Auto Tag Disqualified directorship as clear ";
								}
							} else {
								JsonNode tableList = rawDataStrNode.get("Result").get("Table");
								if (tableList.get("PAN_CIN_DIN") != null) {
									String pan_cin_din = tableList.get("PAN_CIN_DIN").asText();
									if (pan_cin_din.equalsIgnoreCase("DIN:" + din)) {
										panFlag = true;
										logger.info("Auto Tag Disqualified directorship as records found");
										// resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as
										// records found");
										recordFound = true;
										recordFoundStr = "Auto Tag Disqualified directorship as records found";
										resultWatchOut.put("WatchOut Annexure",
												tableList.get("Regulatory_Action_Source1").asText());
										resultWatchOut.put("WatchOut Summary",
												tableList.get("Regulatory_Charges").asText());
										// Provide copy of the hyperlink from FD : Regulatory_Action_Source1 for
										// Annexure
										// Provide summary of hit from FD Regulatory_Charges
									}
								}
								Boolean defaulterFlag = false;
								if (!panFlag) {
									if (tableList.get("Defaulter_Name") != null) {
										String defaulterName = tableList.get("Defaulter_Name").asText();
										if (defaulterName.equalsIgnoreCase(personalInfoSearch.getName())) {
											logger.info("Send for Manual review");
											defaulterFlag = true;
											manual = true;
											manualStr = "Send for Manual review";
											// resultWatchOut.put("WatchOut Output", "Send for Manual review");
											resultWatchOut.put("WatchOut Annexure",
													tableList.get("Regulatory_Action_Source1").asText());
											resultWatchOut.put("WatchOut Summary",
													tableList.get("Regulatory_Charges").asText());
										}
									}
								}
								if (!defaulterFlag) {
									clear = true;
									clearStr = "Auto Tag Disqualified directorship as clear ";
									// resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as
									// clear ");
								}
							}
						} else {
							clear = true;
							clearStr = "Auto Tag Disqualified directorship as clear.Result has no Table Fields";
							// resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as
							// clear.Result has no Table Fields");
							logger.info("Result has no Table Fields");
						}
					} else {
						clear = true;
						clearStr = "Raw Data is Empty. Auto Tag Disqualified directorship as clear";
						// resultWatchOut.put("WatchOut Output","Raw Data is Empty. Auto Tag
						// Disqualified directorship as clear");
						logger.info("raw Data is empty");
					}
				} else {
					logger.info("Value of Raw data is null");
					clear = true;
					clearStr = "Raw Data is null. Auto Tag Disqualified directorship as clear";
					// resultWatchOut.put("WatchOut Output","Raw Data is null. Auto Tag Disqualified
					// directorship as clear");
				}
				// resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship Else
				// Part");
			}
		}

		String finalResult = "";
		String finalStatus = "";
		if (recordFound) {
			finalResult = recordFoundStr;
			finalStatus = "Record Found";
		} else if (manual) {
			finalResult = manualStr;
			finalStatus = "Manual";
		} else if (clear) {
			finalResult = clearStr;
			finalStatus = "Clear";
		}

		resultWatchOut.put("WatchOut Output", finalResult);
		resultWatchOut.put("status", finalStatus);

		return resultWatchOut;
	}

	@Override
	public ObjectNode parseWorldCheckResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String worldCheckResponse, String din, ObjectNode resultWorldCheck) {

		boolean clear = false;
		boolean manual = false;
		boolean recordFound = false;

		String clearStr = "";
		String manualStr = "";
		String recordFoundStr = "";

		if (worldCheckResponse == null) {
			// resultWorldCheck.put("World Check Output", "Auto Tag Loan Defaulter as
			// Clear");
			clear = true;
			clearStr = "Auto Tag Loan Defaulter as Clear";
			logger.info("Auto Tag Loan Defaulter as Clear" + resultWorldCheck);
		} else {
			logger.info("Auto Tag Loan Defaulter Else Part " + worldCheckResponse);
			JsonNode resultResponse = null;
			try {
				resultResponse = (ObjectNode) mapper.readTree(worldCheckResponse);
			} catch (Exception e) {
				logger.info("Got Excption:{}",e.getMessage());
				//e.printStackTrace();
			}
			if (resultResponse != null) {
				if (resultResponse.has("name")) {
					if (resultResponse.get("name").asText().equalsIgnoreCase(personalInfoSearch.getName())) {
						logger.info("FD: Event Action: Match Date of Birth");
						ArrayNode resultRecords = (ArrayNode) resultResponse.get("results");
						if (resultRecords != null) {
							logger.info("Result is not null");
							logger.info("Size of Result" + resultRecords.size());
							// Put Logic for Resolution (resolution) --> StatusId (statusId)
							Boolean resolutionFlag = false;
							logger.info("Search For Resolution. If Found null make flag true and run the new Logic");
							String referenceId = "";
							//Make ArrayList of referenceID
							List<String> referenceIdList=new ArrayList<>();
							for (int i = 0; i < resultRecords.size(); i++) {
								if (resultRecords.get(i).get("resolution") != null
										&& resultRecords.get(i).get("resolution").asText() != "null") {
									//resolutionFlag = false;
								} else {
									resolutionFlag = true;
									logger.info("Take out reference Id On which need to call profile URL");
									referenceId = resultRecords.get(i).get("referenceId") != null
											? resultRecords.get(i).get("referenceId").asText()
											: "";
									referenceIdList.add(referenceId);
									//break;
									}
							}
							if (resolutionFlag) {
								logger.info("Value of referenceIdList ID" + referenceIdList);
								if(!referenceIdList.isEmpty()) {
									List <String> referenceIddList=new ArrayList<>();
									for(int i=0;i<referenceIdList.size();i++) {
										/*if (StringUtils.isNotEmpty(referenceId)) {*/
										String referenceIdd=referenceIdList.get(i);
										if (StringUtils.isNotEmpty(referenceIdd)) {
											logger.info("Reference Id is not null, call profile URL");
											String response = apiService.sendDataToGet(profileUrl + referenceIdd);
											referenceIddList.add(response);
											manual = true;
											manualStr = referenceIddList.toString();
										} else {
											logger.info("Reference ID is Empty" + referenceIdd);
											//clear = true;
											//clearStr = "Reference ID is Empty";
										}
									}
								}else {
									logger.info("referenceId List is Empty" + referenceIdList);
									clear = true;
									clearStr = "Reference ID List is Empty";
								}
								
							} else {
								Boolean matchStrengthFlag = false;
								for (int i = 0; i < resultRecords.size(); i++) {
									if (resultRecords.get(i).has("matchStrength")) {
										matchStrengthFlag = true;
										if (resultRecords.get(i).get("matchStrength").asText()
												.equalsIgnoreCase("EXACT")) {
											ArrayNode eventList = (ArrayNode) resultRecords.get(i).get("events");
											if (eventList != null && eventList.size() > 0) {
												for (int j = 0; j < eventList.size(); j++) {
													if (eventList.get(j).get("fullDate").asText()
															.equalsIgnoreCase(personalInfoSearch.getDob())) {
														logger.info(
																"Date Birth is Matched! FD: Identity Document Action: Match DIN from Cross Directorship from Step 1");
														ArrayNode identityDocumentList = (ArrayNode) resultRecords
																.get(i).get("identityDocuments");
														if (identityDocumentList != null
																&& identityDocumentList.size() > 0) {
															Boolean identityDocumentFlag = false;
															for (int k = 0; k < identityDocumentList.size(); k++) {
																if (identityDocumentList.get(k).get("type") != null) {
																	if (identityDocumentList.get(k).get("type").asText()
																			.equalsIgnoreCase("din")) {
																		if (identityDocumentList.get(k).get("number")
																				.asText().equalsIgnoreCase(din)) {
																			identityDocumentFlag = true;
																		}
																	}
																}
															}
															if (identityDocumentFlag) {
																logger.info("Din found in Identity Document List");
																resultWorldCheck.put("World Check Output",
																		"Auto Tag Disqualified directorship as records found");
																recordFound = true;
																recordFoundStr = "Auto Tag Disqualified directorship as records found";
															} else {
																logger.info(
																		"Din not found in Identity Document List. Search for categories");
																ArrayNode categories = (ArrayNode) resultRecords.get(i)
																		.get("categories");
																if (categories != null && categories.size() > 0) {
																	Boolean categoriesFlag = false;
																	for (int l = 0; l < categories.size(); l++) {
																		if (categories.get(l).asText()
																				.equalsIgnoreCase("SANCTIONS")) {
																			categoriesFlag = true;
																			// break;
																		}
																	}
																	if (categoriesFlag) {
																		// resultWorldCheck.put("World Check
																		// Output","(Primary Match) Send for Manual
																		// review under check type “Global Database
																		// Checks”");
																		manual = true;
																		manualStr = "(Primary Match) Send for Manual review under check type “Global Database Checks”";
																	} else {
																		// resultWorldCheck.put("World Check Output","(
																		// Primary Match) Send for Manual review under
																		// check type “Web & Media”");
																		manual = true;
																		manualStr = "( Primary Match) Send for Manual review under check type “Web & Media”";
																	}
																} else {
																	// resultWorldCheck.put("World Check Output","Search
																	// for categories is empty");
																	clear = true;
																	clearStr = "Search for categories is empty";
																	logger.info("Search for categories is empty");
																}
															}
														} else {
															clear = true;
															clearStr = "IdentityDocumentList is Empty";
															// resultWorldCheck.put("World Check
															// Output","IdentityDocumentList is Empty");
															logger.info("Value of IdentityDocumentList is Empty");
														}
													} else {
														logger.info(
																"Date Birth is not Matched! Check for address in event");
														if (eventList.get(j).get("address").asText()
																.equalsIgnoreCase(personalInfoSearch.getAddress())) {
															ArrayNode categories = (ArrayNode) resultRecords.get(i)
																	.get("categories");
															if (categories != null && categories.size() > 0) {
																Boolean categoriesFlag = false;
																for (int l = 0; l < categories.size(); l++) {
																	if (categories.get(l).asText()
																			.equalsIgnoreCase("SANCTIONS")) {
																		categoriesFlag = true;
																		// break;
																	}
																}
																if (categoriesFlag) {
																	manual = true;
																	manualStr = "( Secondary Match)  Send for Manual review under check type “Global Database Checks”";
//																resultWorldCheck.put("World Check Output",
//																		"( Secondary Match)  Send for Manual review under check type “Global Database Checks”");
																} else {
																	manual = true;
																	manualStr = "( Secondary Match)  Send for Manual review under check type “Web & Media”";
//																resultWorldCheck.put("World Check Output",
//																		"( Secondary Match)  Send for Manual review under check type “Web & Media”");
																}
															} else {
																// resultWorldCheck.put("World Check Output","categories
																// is empty");
																clear = true;
																clearStr = "categories is empty";
																logger.info("categories is null or empty");
															}
														} else {
															clear = true;
															clearStr = "Address didn't match";
															// resultWorldCheck.put("World Check Output","Address didn't
															// match");
															logger.info("Address didn't match");
														}
													}
												}
											} else {
												clear = true;
												clearStr = "Event List is Empty";
												// resultWorldCheck.put("World Check Output","Event List is Empty");
												logger.info("Event List is Empty");
											}
										} else if (resultRecords.get(i).get("matchStrength").asText()
												.equalsIgnoreCase("MEDIUM")) {
											ArrayNode eventList = (ArrayNode) resultRecords.get(i).get("events");
											if (eventList != null && eventList.size() > 0) {
												for (int j = 0; j < eventList.size(); j++) {
													if (eventList.get(j).get("fullDate").asText()
															.equalsIgnoreCase(personalInfoSearch.getDob())) {
														logger.info(
																"Date Birth is Matched! FD: Identity Document Action: Match DIN from Cross Directorship from Step 1");
														ArrayNode identityDocumentList = (ArrayNode) resultRecords
																.get(i).get("identityDocuments");
														if (identityDocumentList != null
																&& identityDocumentList.size() > 0) {
															Boolean identityDocumentFlag = false;
															for (int k = 0; k < identityDocumentList.size(); k++) {
																if (identityDocumentList.get(k).get("type") != null) {
																	if (identityDocumentList.get(k).get("type").asText()
																			.equalsIgnoreCase("din")) {
																		if (identityDocumentList.get(k).get("number")
																				.asText().equalsIgnoreCase(din)) {
																			identityDocumentFlag = true;
																		}
																	}
																}
															}
															if (identityDocumentFlag) {
																logger.info("Din found in Identity Document List");
																// resultWorldCheck.put("World Check Output","Auto Tag
																// Disqualified directorship as records found");
																recordFound = true;
																recordFoundStr = "Auto Tag Disqualified directorship as records found";
															} else {
																logger.info(
																		"Din not found in Identity Document List. Search for categories");
																ArrayNode categories = (ArrayNode) resultRecords.get(i)
																		.get("categories");
																if (categories != null && categories.size() > 0) {
																	Boolean categoriesFlag = false;
																	for (int l = 0; l < categories.size(); l++) {
																		if (categories.get(l).asText()
																				.equalsIgnoreCase("SANCTIONS")) {
																			categoriesFlag = true;
																			// break;
																		}
																	}
																	if (categoriesFlag) {
																		manual = true;
																		manualStr = "(Secondary Match) Send for Manual review under check type “Global Database Checks”";
//																	resultWorldCheck.put("World Check Output",
//																			"(Secondary Match) Send for Manual review under check type “Global Database Checks”");
																	} else {
																		manual = true;
																		manualStr = "( Secondary Match) Send for Manual review under check type “Web & Media”";
//																	resultWorldCheck.put("World Check Output",
//																			"( Secondary Match) Send for Manual review under check type “Web & Media”");
																	}
																}
															}
														} else {
															clear = true;
															clearStr = "IdentityDocumentList is Empty";
															// resultWorldCheck.put("World Check
															// Output","IdentityDocumentList is Empty");
															logger.info("Value of IdentityDocumentList is Empty");
														}
													} else {
														logger.info(
																"Date Birth is not Matched! Check for address in event");
														if (eventList.get(j).get("address").asText()
																.equalsIgnoreCase(personalInfoSearch.getAddress())) {
															ArrayNode categories = (ArrayNode) resultRecords.get(i)
																	.get("categories");
															if (categories != null && categories.size() > 0) {
																Boolean categoriesFlag = false;
																for (int l = 0; l < categories.size(); l++) {
																	if (categories.get(l).asText()
																			.equalsIgnoreCase("SANCTIONS")) {
																		categoriesFlag = true;
																		// break;
																	}
																}
																if (categoriesFlag) {
																	manual = true;
																	manualStr = "( Secondary Match)  Send for Manual review under check type “Global Database Checks”";
//																resultWorldCheck.put("World Check Output",
//																		"( Secondary Match)  Send for Manual review under check type “Global Database Checks”");
																} else {
																	manual = true;
																	manualStr = "( Secondary Match)  Send for Manual review under check type “Web & Media”";
//																resultWorldCheck.put("World Check Output",
//																		"( Secondary Match)  Send for Manual review under check type “Web & Media”");
																}
															}
														} else {
															clear = true;
															clearStr = "Address didn't match";
															// resultWorldCheck.put("World Check Output","Address didn't
															// match");
															logger.info("Address didn't match");
														}
													}
												}
											} else {
												clear = true;
												clearStr = "Event List is Empty";
												// resultWorldCheck.put("World Check Output","Event List is Empty");
												logger.info("Event List is Empty");
											}
										} else {
											logger.info("match Strength is not EXACT and MEDIUM");
											clear = true;
											clearStr = "Match Strength is not EXACT and MEDIUM";
											// resultWorldCheck.put("World Check Output", "Match Strength is not EXACT
											// and MEDIUM");
										}
									}
								}
								if (!matchStrengthFlag) {
									clear = true;
									clearStr = "matchStrength tag is not found";
									// resultWorldCheck.put("World Check Output", "matchStrength tag is not found");
								}
							}
						} else {
							clear = true;
							clearStr = "Result list is empty";
							// resultWorldCheck.put("World Check Output", "Result list is empty");
						}
					} else {
						recordFound = true;
						recordFoundStr = "Auto Tag “Web & Media & Global Database check” as No records found";
						// resultWorldCheck.put("World Check Output","Auto Tag “Web & Media & Global
						// Database check” as No records found");
					}
				} else {
					clear = true;
					clearStr = "name tag is not found in worldCheck response";
					resultWorldCheck.put("World Check Output", "name tag is not found in worldCheck response");
				}
				// resultWorldCheck.put("World Check Output", "Auto Tag Loan Defaulter Else
				// Part");
			}

		}

		String finalResult = "";
		String finalStatus = "";
		if (recordFound) {
			finalResult = recordFoundStr;
			finalStatus = "Record Found";
		} else if (manual) {
			finalResult = manualStr;
			finalStatus = "Manual";
		} else if (clear) {
			finalResult = clearStr;
			finalStatus = "Clear";
		}

		resultWorldCheck.put("World Check Output", finalResult);
		resultWorldCheck.put("status", finalStatus);

		return resultWorldCheck;
	}

}