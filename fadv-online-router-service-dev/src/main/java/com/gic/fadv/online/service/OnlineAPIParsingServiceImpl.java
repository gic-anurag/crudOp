package com.gic.fadv.online.service;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.online.model.PersonInfoSearch;

@Service
public class OnlineAPIParsingServiceImpl implements OnlineAPIParsingService{
	
	private static final Logger logger = LoggerFactory.getLogger(OnlineAPIParsingServiceImpl.class);
	
	public void parseWorldCheckResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String worldCheckResponse, String din, ObjectNode resultWorldCheck) {
		if (worldCheckResponse == null) {
			resultWorldCheck.put("World Check Output", "Auto Tag Loan Defaulter as Clear");
			logger.info("Auto Tag Loan Defaulter as Clear" + resultWorldCheck);
		} else {
			logger.info("Auto Tag Loan Defaulter Else Part");
			JsonNode resultResponse=null;
			try {
				resultResponse = (ObjectNode) mapper.readTree(worldCheckResponse);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (resultResponse.has("name")) {
				if (resultResponse.get("name").asText().equalsIgnoreCase(personalInfoSearch.getName())) {
					logger.info("FD: Event Action: Match Date of Birth");
					ArrayNode resultRecords = (ArrayNode) resultResponse.get("results");
					if (!resultRecords.isEmpty()) {
						logger.info("Result is not null");
						logger.info("Size of Result" + resultRecords.size());
						for (int i = 0; i < resultRecords.size(); i++) {
							if (resultRecords.get(i).has("matchStrength")) {
								if (resultRecords.get(i).get("matchStrength").asText().equalsIgnoreCase("EXACT")) {
									ArrayNode eventList = (ArrayNode) resultRecords.get(i).get("events");
									if (eventList != null && eventList.size() > 0) {
										for (int j = 0; j < eventList.size(); j++) {
											if (eventList.get(j).get("fullDate").asText()
													.equalsIgnoreCase(personalInfoSearch.getDob())) {
												logger.info(
														"Date Birth is Matched! FD: Identity Document Action: Match DIN from Cross Directorship from Step 1");
												ArrayNode identityDocumentList = (ArrayNode) resultRecords.get(i)
														.get("identityDocuments");
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
																	break;
																}
															}
															if (categoriesFlag) {
																resultWorldCheck.put("World Check Output",
																		"(Primary Match) Send for Manual review under check type “Global Database Checks”");
															} else {
																resultWorldCheck.put("World Check Output",
																		"( Primary Match) Send for Manual review under check type “Web & Media”");
															}
														}
													}
												} else {
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
																break;
															}
														}
														if (categoriesFlag) {
															resultWorldCheck.put("World Check Output",
																	"( Secondary Match)  Send for Manual review under check type “Global Database Checks”");
														} else {
															resultWorldCheck.put("World Check Output",
																	"( Secondary Match)  Send for Manual review under check type “Web & Media”");
														}
													}
												} else {
													logger.info("Address didn't match");
												}
											}
										}
									} else {
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
												ArrayNode identityDocumentList = (ArrayNode) resultRecords.get(i)
														.get("identityDocuments");
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
																	break;
																}
															}
															if (categoriesFlag) {
																resultWorldCheck.put("World Check Output",
																		"(Secondary Match) Send for Manual review under check type “Global Database Checks”");
															} else {
																resultWorldCheck.put("World Check Output",
																		"( Secondary Match) Send for Manual review under check type “Web & Media”");
															}
														}
													}
												} else {
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
																break;
															}
														}
														if (categoriesFlag) {
															resultWorldCheck.put("World Check Output",
																	"( Secondary Match)  Send for Manual review under check type “Global Database Checks”");
														} else {
															resultWorldCheck.put("World Check Output",
																	"( Secondary Match)  Send for Manual review under check type “Web & Media”");
														}
													}
												} else {
													logger.info("Address didn't match");
												}
											}
										}
									} else {
										logger.info("Event List is Empty");
									}
								} else {
									logger.info("match Strength is not EXACT and MEDIUM");
									resultWorldCheck.put("World Check Output",
											"Match Strength is not EXACT and MEDIUM");
								}
							}
						}
					} else {
						resultWorldCheck.put("World Check Output", "Result list is empty");
					}
				} else {
					resultWorldCheck.put("World Check Output",
							"Auto Tag “Web & Media & Global Database check” as No records found");
				}
			} else {
				resultWorldCheck.put("World Check Output", "name tag is not found in worldCheck response");
			}
			// resultWorldCheck.put("World Check Output", "Auto Tag Loan Defaulter Else Part");
		}
	}

	public void parseManupatraResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch, String manuPatraResponse,
			ObjectNode resultManupatra) {
		if (manuPatraResponse == null) {
			resultManupatra.put("ManuPatra Output", "Auto Tag Civil Litigation as clear");
			logger.info("Auto Tag Civil Litigation as clear" + resultManupatra);
		} else {
			logger.info("Auto Tag Civil Litigation Else Part" + resultManupatra);
			JsonNode resultResponse=null;
			try {
				resultResponse = (ObjectNode) mapper.readTree(manuPatraResponse);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Value of Raw Data" + resultResponse.get("rawData"));
			logger.info("Value of Raw Data" + resultResponse.get("rawData"));
			// System.out.println("Value of Raw Data"+resultResponse.get("rawData"));
			if (resultResponse.get("rawData") != null) {
				String rawDataStr = resultResponse.get("rawData").asText();
				JsonNode rawDataStrNode=null;
				try {
					rawDataStrNode = (ObjectNode) mapper.readTree(rawDataStr);
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.info("Value of JsonNode rawData" + rawDataStrNode);
				//Take Data From RAW DATA
				JsonNode rawDataStrNodeDataNode=rawDataStrNode.get("data");
				logger.info("Size of ArrayNode"+rawDataStrNodeDataNode.size());
				logger.info("Value of ArrayNode"+rawDataStrNodeDataNode);
				ArrayNode dataListValue=null;
				try {
					dataListValue = (ArrayNode)mapper.readTree(rawDataStrNodeDataNode.asText());
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.info("Value of Data in Json"+dataListValue);
				/*
				 * Iterate for file data in each data ArrayNode
				 */
				logger.info("Size of Data List Value"+dataListValue.size());
				Boolean nameMatchflag=false;
				for(int i=0;i<dataListValue.size();i++) {
					logger.info("Value of title in Data Json"+dataListValue.get(i).get("title"));
					logger.info("Value of title in Data Json"+dataListValue.get(i).get("fileData"));
					JsonNode fileDataNode=dataListValue.get(i).get("fileData");
					JsonNode titleNode=dataListValue.get(i).get("title");
					if(titleNode!=null) {
						if(titleNode.asText().contains(personalInfoSearch.getName())) {
							nameMatchflag=true;
							logger.info("Match Found!Search for Other Parameters (address / State/ City / Father’s name) in fileData");
							if(fileDataNode!=null) {
								String fileDataStr=fileDataNode.asText();
								JsonNode fileDataValue=null;
								try {
									fileDataValue = mapper.readTree(fileDataStr);
								} catch (JsonMappingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (JsonProcessingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								logger.info("Value of Data in Json"+fileDataValue);
								logger.info("Value of Result in Data Json"+fileDataValue.get("Result"));
								//Since Result and File Data Doesn't have (address / State/ City / Father’s name)
								//So, putting the else condition in ManuPatra Output
								// (Secondary Match): Send for Manual review under India Civil Litigation & Criminal  
								//Provide copy of the hyperlink from FD : Filedata
								resultManupatra.put("ManuPatra Output", "Auto Tag Civil Litigation as clear");
								resultManupatra.put("ManuPatra FileData", fileDataValue.get("Result").asText());
								
								//If address and other match found then
								//(Primary Match)  Send for Manual review under India Civil Litigation & Criminal.
								//Provide copy of the hyperlink from FD : Filedata	
							}
						}
					}
				}
				if(!nameMatchflag) {
					logger.info("nameMatchflag is false");
					resultManupatra.put("ManuPatra Output", "Auto Tag Civil Litigation as clear");
				}
			}else {
				resultManupatra.put("ManuPatra Output", "Raw Data Empty/Null");
			}
		}
	}

	public void parseWatchoutResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String fulfillmentResponse, String din, ObjectNode resultWatchOut) {
		if(fulfillmentResponse==null) 
		{ 
			resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as clear");
		  logger.info("Auto Tag Disqualified directorship as clear"); 
		  }else {
			  logger.info("Auto Tag Disqualified directorship Else Part"+resultWatchOut);
			  JsonNode resultResponse=null;
			try {
				resultResponse = (ObjectNode) mapper.readTree(fulfillmentResponse);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  logger.info("Value of Raw Data"+resultResponse.get("rawData"));
			 if(resultResponse.get("rawData")!=null) 
			 { 
				 String rawDataStr=resultResponse.get("rawData").asText(); 
				 JsonNode rawDataStrNode=null;
				try {
					rawDataStrNode = (ObjectNode) mapper.readTree(rawDataStr);
				} catch (JsonMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 logger.info("Value of JsonNode rawData"+rawDataStrNode);
			//Logic for Matching the PIN_CIN_DIN
		   	  Boolean panFlag=false; 
		   	//Logic for Matching the Defaulter_Name
			  Boolean defaulterFlag=false; 
		   	  if(rawDataStrNode.get("Result").get("Table").isArray()) {
		   		 ArrayNode tableList=(ArrayNode)rawDataStrNode.get("Result").get("Table"); 
		   		 for(int i=0;i<tableList.size();i++) { 
			   		  if(tableList.get(i).get("PAN_CIN_DIN")!=null) 
			   		  {
			   			  String pan_cin_din=tableList.get(i).get("PAN_CIN_DIN").asText();
			   			  if(pan_cin_din.equalsIgnoreCase("DIN:"+din)) 
			   			  { 
			   				  panFlag=true;
			   				  logger.info("Auto Tag Disqualified directorship as records found");
			   				  resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as records found");
			   				  resultWatchOut.put("WatchOut Annexure",tableList.get(i).get("Regulatory_Action_Source1").asText());
			   				  resultWatchOut.put("WatchOut Summary",tableList.get(i).get("Regulatory_Charges").asText()); 
			   				  //Provide copy of the hyperlink from FD : Regulatory_Action_Source1 for Annexure 
			   				  //Provide summary of hit from FD Regulatory_Charges 
			   				  } 
			   			  } 
			   		  }
		   		//Logic for Matching the Defaulter_Name
				  if(!panFlag) { 
					  for(int i=0;i<tableList.size();i++) {
						  if(tableList.get(i).get("Defaulter_Name")!=null) { 
							  String defaulterName=tableList.get(i).get("Defaulter_Name").asText();
							  if(defaulterName.equalsIgnoreCase(personalInfoSearch.getName())) {
								  logger.info("Send for Manual review"); defaulterFlag=true;
								  resultWatchOut.put("WatchOut Output", "Send for Manual review");
								  resultWatchOut.put("WatchOut Annexure",tableList.get(i).get("Regulatory_Action_Source1").asText());
								  resultWatchOut.put("WatchOut Summary",tableList.get(i).get("Regulatory_Charges").asText()); 
								  } 
							  } 
						  } 
					  }
		   	  }else {
		   		 JsonNode tableList=rawDataStrNode.get("Result").get("Table"); 
		   		 if(tableList.get("PAN_CIN_DIN")!=null) 
			   		  {
			   			  String pan_cin_din=tableList.get("PAN_CIN_DIN").asText();
			   			  if(pan_cin_din.equalsIgnoreCase("DIN:"+din)) 
			   			  { 
			   				  panFlag=true;
			   				  logger.info("Auto Tag Disqualified directorship as records found");
			   				  resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as records found");
			   				  resultWatchOut.put("WatchOut Annexure",tableList.get("Regulatory_Action_Source1").asText());
			   				  resultWatchOut.put("WatchOut Summary",tableList.get("Regulatory_Charges").asText()); 
			   				  //Provide copy of the hyperlink from FD : Regulatory_Action_Source1 for Annexure 
			   				  //Provide summary of hit from FD Regulatory_Charges 
			   				  } 
			   			  } 
		   		 		//Logic for Matching the Defaulter_Name
						  if(!panFlag) { 
							  if(tableList.get("Defaulter_Name")!=null) { 
									  String defaulterName=tableList.get("Defaulter_Name").asText();
									  if(defaulterName.equalsIgnoreCase(personalInfoSearch.getName())) {
										  logger.info("Send for Manual review"); defaulterFlag=true;
										  resultWatchOut.put("WatchOut Output", "Send for Manual review");
										  resultWatchOut.put("WatchOut Annexure",tableList.get("Regulatory_Action_Source1").asText());
										  resultWatchOut.put("WatchOut Summary",tableList.get("Regulatory_Charges").asText()); 
										  } 
									  } 
							  	}
						  }
			if(!defaulterFlag) { 
			  resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship as clear "); 
			  } 
		  }
		  //resultWatchOut.put("WatchOut Output","Auto Tag Disqualified directorship Else Part"); 
		}
	}

	public void parseLoanDefaulterResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String personalResponse, ObjectNode resultLoan) {
		if (personalResponse == null) {
			resultLoan.put("Loan Defaulter Output", "Auto Tag Loan Defaulter as Clear");
			logger.info("Auto Tag Loan Defaulter as Clear" + resultLoan);
		} else {
			logger.info("Auto Tag Loan Defaulter Else Part" + resultLoan);
			JsonNode resultResponse=null;
			try {
				resultResponse = (ObjectNode) mapper.readTree(personalResponse);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ArrayNode results = (ArrayNode) resultResponse.get("results");
			ArrayNode data = (ArrayNode) results.get(0).get("data");
			if (data != null) {
				if (data.size() > 0) {
					for (int i = 0; i < data.size(); i++) {
						if (data.get(i).get("sub_type") != null) {
							if (data.get(i).get("sub_type").asText().equalsIgnoreCase("cibil defaulters")) {
								if (data.get(i).get("name").asText()
										.equalsIgnoreCase(personalInfoSearch.getName())) {
									if (data.get(i).get("address").asText()
											.equalsIgnoreCase(personalInfoSearch.getAddress())) {
										logger.info(
												" (Primary Match)Send for Manual review.Provide copy of the hyperlink "
														+ "from FD : Link & FD: url for Annexure");
										resultLoan.put("Loan Defaulter Output",
												" (Primary Match)Send for Manual review.Provide copy of the hyperlink "
														+ "from FD : Link & FD: url for Annexure");
										resultLoan.put("Link", data.get(i).get("link").asText());
										resultLoan.put("Annexure", data.get(i).get("url").asText());
									} else {
										if (data.get(i).get("state_name").asText()
												.equalsIgnoreCase(personalInfoSearch.getState())) {
											logger.info(
													"Output:- (Secondary Match). Send for Manual review. Provide copy of"
															+ " the hyperlink from FD : Link & FD: url for Annexure");
											resultLoan.put("Loan Defaulter Output",
													"Output:- (Secondary Match). Send for Manual review. Provide copy of"
															+ "the hyperlink from FD : Link & FD: url for Annexure");
											resultLoan.put("Link", data.get(i).get("link").asText());
											resultLoan.put("Annexure", data.get(i).get("url").asText());
										} else {
											resultLoan.put("Loan Defaulter Output",
													"Auto Tag Loan Defaulter as Clear");
											logger.info("Auto Tag Loan Defaulter as Clear" + resultLoan);
										}
									}
								} else {
									resultLoan.put("Loan Defaulter Output", "Auto Tag Loan Defaulter as Clear");
									logger.info("Auto Tag Loan Defaulter as Clear" + resultLoan);
								}
							} else {
								logger.info("Sub Type is not cibil");
							}
						} else {
							logger.info("Sub Type is null");
						}
					}
				} else {
					resultLoan.put("Loan Defaulter Output", "Data is Empty.Auto Tag Loan Defaulter as Clear");
					logger.info("Data is Empty");
				}
			} else {
				logger.info("Data is Null for cibil");
			}
			// resultLoan.put("Loan Defaulter Output", "Auto Tag Loan Defaulter Else Part");
		}
	}

	public void parseAdverseMediaResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch,
			String personalResponse, ObjectNode resultAdverseMedia) {
		if (personalResponse == null) {
			resultAdverseMedia.put("Adverse media Output", "Auto Tag Web & Media as clear");
			logger.info("Auto Tag Web & Media as clear" + resultAdverseMedia);
		} else {
			logger.info("Auto Tag Web & Media Else Part");
			JsonNode resultResponse=null;
			try {
				resultResponse = (ObjectNode) mapper.readTree(personalResponse);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ArrayNode results = (ArrayNode) resultResponse.get("results");
			ArrayNode data = (ArrayNode) results.get(0).get("data");
			if (data.size() > 0) {
				JsonNode nameNode = data.get(0).get("name");
				if (nameNode != null) {
					String name = data.get(0).get("name").asText();
					if (name.equalsIgnoreCase(personalInfoSearch.getName())) {
						if (data.get(0).get("address").asText().equalsIgnoreCase(personalInfoSearch.getAddress())) {
							logger.info(
									"(Primary Match) Send for Manual review under web and Media Provide copy of the"
											+ " hyperlink from FD : Link");
							resultAdverseMedia.put("Adverse media Output",
									"Send for Manual review under web and Media Provide copy of the"
											+ " hyperlink from FD : Link");
							resultAdverseMedia.put("Link", data.get(0).get("link").asText());
							resultAdverseMedia.put("Annexure", data.get(0).get("url").asText());
						} else {
							logger.info("(Secondary Match) Send for Manual review under Web & Media "
									+ " Provide copy of the hyperlink from FD : Link");
							resultAdverseMedia.put("Adverse media Output", "Send for Manual review "
									+ "under Web & Media  Provide copy of the hyperlink from FD :" + "Link");
							resultAdverseMedia.put("Link", data.get(0).get("link").asText());
							resultAdverseMedia.put("Annexure", data.get(0).get("url").asText());
						}
					} else {
						resultAdverseMedia.put("Adverse media Output", "Auto Tag Web & Media as clear ");
						logger.info("Auto Tag Cross directorship as clear" + resultAdverseMedia);
					}
				} else {
					logger.info("Name is Null");
					resultAdverseMedia.put("Adverse media Output", "Auto Tag Web & Media as clear");
					logger.info("Auto Tag Cross directorship as clear" + resultAdverseMedia);
				}
			} else {
				logger.info("data is Empty");
				resultAdverseMedia.put("Adverse Media Output", "Auto Tag Web & Media. Data is Empty");
			}
		}
	}

	public String parseMCAResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch, String personalResponse,
			ObjectNode resultMCA) {
		String din=null;
		if(personalResponse==null) {
			  resultMCA.put("MCA Output", "Auto Tag Cross directorship as clear");
			  logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
		  }else {
			  JsonNode resultResponse=null;
			try {
				resultResponse = (ObjectNode) mapper.readTree(personalResponse);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  ArrayNode results=(ArrayNode)resultResponse.get("results"); 
			  ArrayNode data=(ArrayNode)results.get(0).get("data"); 
			  if(data!=null && data.size()>0) {
				  JsonNode dinNode =data.get(0).get("din"); 
				  if(dinNode!=null) {
					  din=dinNode.asText(); 
				} 
				  JsonNode nameNode=data.get(0).get("name");
				  if(nameNode!=null) { 
					  String name= data.get(0).get("name").asText();
					  if(name.equalsIgnoreCase(personalInfoSearch.getName())) { 
						  resultMCA.put("MCA Output","Auto Tag Cross directorship as Record found");
						  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
					}else {
						resultMCA.put("MCA Output","Auto Tag Cross directorship as clear");
						logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
						} 
				}else {
					logger.info("Name is Null"); 
					resultMCA.put("MCA Output","Auto Tag Cross directorship as clear");
					logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
					} 
				}else{
					resultMCA.put("MCA Output","Data is Empty.Auto Tag Cross directorship as clear");
					logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
					} 
			  }
		return din;
	}

	public void timeOut() {
		try { 
			TimeUnit.SECONDS.sleep(100); 
		} catch (InterruptedException ie) 
		{
		  Thread.currentThread().interrupt(); 
		}
	}

}
