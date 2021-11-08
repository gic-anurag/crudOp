package com.gic.fadv.online.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gic.fadv.online.model.PersonInfoSearch;
import com.gic.fadv.online.service.OnlineApiService;
import com.gic.fadv.online.service.ParseAPIResponseService;
import com.gic.fadv.online.utility.Utility;
import com.google.gson.JsonParser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/india")
@Api(value = "Cross Reference", description = "Operations pertaining to Online API Controller (Cross Reference master data)")
public class OnlineAPIMCAController {

	@Autowired
	private Environment env;
	@Autowired
	private OnlineApiService onlineApiService;
	@Autowired
	private ParseAPIResponseService parseAPIResponseService;
	
	private static final Logger logger = LoggerFactory.getLogger(OnlineAPIMCAController.class);

	@ApiOperation(value = "This service is used to process Records at Online API and return the result ", response = List.class)
	@PostMapping(path = "/async/onlineapi/mca", consumes = "application/json", produces = "application/json")
	public DeferredResult<ResponseEntity<String>> doAsyncProcess(@RequestBody String inStr) {
		DeferredResult<ResponseEntity<String>> ret = new DeferredResult<>();
		ForkJoinPool.commonPool().submit(() -> {
			logger.info("Got async Request:\n" + inStr);
			processRequest(inStr, true);
			ret.setResult(ResponseEntity.ok("ok"));
		});
		ret.onCompletion(() -> logger.info("async process request done"));
		return ret;
	}

	@ApiOperation(value = "This service is used to process Records at Online API and return the result ", response = List.class)
	@PostMapping(path = "/onlineapi/mca", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> doProcess(@RequestBody String inStr) {
		try {
			logger.info("Got Request:\n" + inStr);
			String response = processRequest(inStr, false);
			logger.info("Processed Request");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
		}
	}

	private String processRequest(String inStr, boolean asyncStatus) {
		try {
			// Creating the ObjectMapper object
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			// Converting the JSONString to Object
			PersonInfoSearch personalInfoSearch = mapper.readValue(inStr, PersonInfoSearch.class);
			
			String personalResponse = null;
			String verifyId=null;
			String watchOutResponse=null;
			String din = null;
			
			ObjectNode resultMCA = mapper.createObjectNode();
			ObjectNode finalResultNode = mapper.createObjectNode();
			ObjectNode personalSearchNode= mapper.createObjectNode();
			/*----------------------MCA Call ----------------------------------------*/
			/*
			 * Call credit_reputational_mca API And Search For DIN
			 */
			
			//This will be used in future. For now it's commented
			//personalInfoSearch.setServices("credit_reputational_mca");
			personalInfoSearch.setServices("mca_dir_combo");
			logger.info("Making Search String For credit_reputational_mca");
			personalSearchNode.put("name",personalInfoSearch.getName());
			personalSearchNode.put("dob",personalInfoSearch.getDob());
			personalSearchNode.put("services",personalInfoSearch.getServices());
			LocalDateTime startTime = LocalDateTime.now();
			logger.info("First Search String.Personal Search String" + personalSearchNode.toString());
			try {
				verifyId = onlineApiService.sendDataToServicePersonalRest(personalSearchNode.toString());
			} catch (Exception e) {
				logger.error("Error in Calling API of credit_reputational_cibil" + e.getMessage());
			}
			logger.info("Parse String and Take out VerifyID"+verifyId);
			logger.info("Got VerifyID. Make Json and hit rest End points for final response");
			/*
			 * 2nd API Call
			 */
			ObjectNode verifyNode= mapper.createObjectNode();
			verifyNode.put("verify_id", verifyId);
			logger.info("Second Seearch String.Personal Search String" + verifyNode.toString());
			try {
				personalResponse = onlineApiService.sendDataToFinalPersonalRest(verifyNode.toString());
				//personalResponse = onlineApiService.sendDataToPersonalVerifyRest(verifyNode.toString());
			} catch (Exception e) {
				logger.error("Error in Calling API of credit_reputational_cibil" + e.getMessage());
			} 
			logger.info("Personal Rest Response MCA "+personalResponse);
			logger.info("Some sleep after Personal API");
			// Put Time out Code here
			//timeOut();
			/*
			 * Parsing Result For Personal Info Search (credit_reputational_mca) and look
			 * for din and Add MCA Result to Final Result
			 */
			  logger.info("Assign Input to MCA Input"); 
			  resultMCA.put("MCA Input",personalSearchNode.toString()); 
			  //Put Extra Field for keeping verifyID
			  resultMCA.put("Verify_id", verifyId);
			  resultMCA.put("Raw Output", personalResponse);
			  din=parseAPIResponseService.parseMCAResponse(mapper, personalInfoSearch, personalResponse, resultMCA);
			  //din=parseMCAResponse(mapper, personalInfoSearch, personalResponse, resultMCA);
			  //Set Return Value also
			  resultMCA.put("din", din);
			  logger.info("Value of din from MCA Response"+din);
			  LocalDateTime endTime = LocalDateTime.now();
			  Duration duration = Duration.between(startTime, endTime);
			  logger.info(duration.getSeconds() + " seconds");
			  resultMCA.put("Start Time",startTime.toString());
			  resultMCA.put("End Time",endTime.toString());
			  resultMCA.put("Time Taken",duration.getSeconds());
			  finalResultNode.set("MCA", resultMCA);
			  logger.info("Value of Final Result After MCA"+finalResultNode);
			/*----------------------MCA Call End----------------------------------------*/
			
			/*-------------------------Watchout Call------------------------------------*/
			 if(din!=null) {
				 ObjectNode personalInfoNode=mapper.createObjectNode();
				 personalInfoNode.put("name",personalInfoSearch.getName());
				 personalInfoNode.put("din",din);
				 logger.info("Input String for Watchout" + personalSearchNode.toString());
				 try {
					 watchOutResponse = onlineApiService.sendDataToWatchOutRest(personalInfoNode.toString());
					} catch (Exception e) {
						logger.error("Error in Calling API of credit_reputational_cibil" + e.getMessage());
					}
					logger.info("Watchout Response"+watchOutResponse);
					if(watchOutResponse!=null) {
						JsonNode WatchOutResultResponse=null;
						try {
							WatchOutResultResponse=(ObjectNode) mapper.readTree(watchOutResponse);
						}catch(Exception e) {
							e.printStackTrace();
						}
						if(WatchOutResultResponse!=null) {
							finalResultNode.set("WatchOut", WatchOutResultResponse.get("WatchOut"));
						}else {
							finalResultNode.put("WatchOut", "Some Parsing issue");
						}
					}else {
						 finalResultNode.put("WatchOut", "Response is Null");
					}
			 }else {
				 logger.info("WatchOut", "Din is null.So, watchout is called without DIN");
				 ObjectNode personalInfoNode=mapper.createObjectNode();
				 personalInfoNode.put("name",personalInfoSearch.getName());
				 //personalInfoNode.put("din",din);
				 logger.info("Input String for Watchout" + personalSearchNode.toString());
				 try {
					 watchOutResponse = onlineApiService.sendDataToWatchOutRest(personalInfoNode.toString());
					} catch (Exception e) {
						logger.error("Error in Calling API of credit_reputational_cibil" + e.getMessage());
					}
					logger.info("Watch Out Response"+watchOutResponse);
					if(watchOutResponse!=null) {
						JsonNode WatchOutResultResponse=null;
						try {
							WatchOutResultResponse=(ObjectNode) mapper.readTree(watchOutResponse);
						}catch(Exception e) {
							e.printStackTrace();
						}
						if(WatchOutResultResponse!=null) {
							finalResultNode.set("WatchOut", WatchOutResultResponse.get("WatchOut"));
						}else {
							finalResultNode.put("WatchOut", "Some Parsing issue");
						}
					}else {
						 finalResultNode.put("WatchOut", "Response is Null");
					}
			 }
			 /*----------------------Watchout Call End----------------------------------------*/  
			String onlineResponse = mapper.writeValueAsString(finalResultNode);
			String returnStr = onlineResponse;
			if (asyncStatus)
				callback(returnStr);
			return returnStr;
			} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (asyncStatus)
				callback(e.getMessage());
			return e.getMessage();
		}
	}

	public String parseMCAResponse(ObjectMapper mapper, PersonInfoSearch personalInfoSearch, String personalResponse,
			ObjectNode resultMCA) throws JsonProcessingException, JsonMappingException {
		boolean clear = false;
		boolean manual = false;
		boolean recordFound = false;
		boolean pending=false;
		
		String clearStr = "";
		String manualStr = "";
		String recordFoundStr = "";
		String pendingStr="";
		String din=null;
		if(personalResponse==null) {
			  //resultMCA.put("MCA Output", "Auto Tag Cross directorship as clear");
			  clearStr = "Auto Tag Cross directorship as clear";
			  logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
			  clear = true;
		  }else {
			  JsonNode resultResponse=(ObjectNode) mapper.readTree(personalResponse);
				  if(resultResponse.has("status") && resultResponse.get("status").asInt()==0) {
					  pending=true;
					  pendingStr="Request is Pending";
					  logger.info("Request Pending"+resultResponse);
				  }else {
					  ArrayNode results=(ArrayNode)resultResponse.get("results"); 
					  if(results!=null) {
					  if(results.get(0).has("status")) {
						  
						  ArrayNode data=(ArrayNode)results.get(0).get("data");
						  if(data!=null && data.size()>0) {
							  for(int i=0;i<data.get(0).size();i++) {
								  JsonNode innerNode = data.get(0).get(i);
								  JsonNode nameNode;
								  JsonNode typNode;
								  String typeNode="";
								  
								  if(innerNode.isArray()) {
									 nameNode = innerNode.get("name");
									 typNode = innerNode.get("type");
								  }else {
									  nameNode=innerNode.get("name");
									  typNode = innerNode.get("type");
								  }
								  
								  if(typNode !=null) {
									  typeNode = typNode.asText();
								  }
								  
								  ///////////////////////Service type///////////////////////////////////

								  if(typeNode.equalsIgnoreCase("mca_dob")) {
									  //ArrayNode data=(ArrayNode)results.get(0).get("data"); 
									  //Check for data Size and iterate over it
									  //if(data!=null && data.size()>0) {
										  Boolean nameFlag=false;
										  
											  //JsonNode nameNode=data.get(i).get("name");
											  if(nameNode!=null) { 
												  //String name= data.get(i).get("name").asText();
												  String name= nameNode.asText();
												  logger.info("Value of name"+name);
												  logger.info("Value of personal Info Search"+personalInfoSearch.getName());
												  
												  if(name.equalsIgnoreCase(personalInfoSearch.getName())) { 
													  //resultMCA.put("MCA Output","Auto Tag Cross directorship as Record found with Date of Birth Match");
													  recordFound = true;
													  recordFoundStr = "Auto Tag Cross directorship as Record found with Date of Birth Match";
													  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
													  nameFlag=true;
													  JsonNode dinNode;
													  //JsonNode dinNode =data.get(i).get("din"); 
													  if(innerNode.isArray()) {
														  dinNode =innerNode.get("din");
														  }else {
															  dinNode=innerNode.get("din");
														  }
													  if(dinNode!=null) {
														  din=dinNode.asText(); 
													  } 
													  //break;
												  	}else {
												  		if(Utility.checkContains(name, personalInfoSearch.getName())) {
												  			  manual = true;
															  manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
															  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
															  nameFlag=true;
												  		}else {
												  			/*
															 * Split Name in First name, Middle Name and last Name
															 */
															String [] names=personalInfoSearch.getName().split(" ");
															logger.info("Value of String After Split"+names);
															String firstName=null,lastName=null,middleName=null;
															if(names.length==3) {
																firstName=names[0];
																lastName=names[2];
																middleName=names[1];
																logger.info("Value of Split Names"+firstName+lastName+middleName+name);
																if(Utility.checkContains(name,firstName) &&
																		Utility.checkContains(name,lastName)) {
																	   recordFound = true;
																	  recordFoundStr = "Auto Tag Cross directorship as Record found with Date of Birth Match";
																	  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
																	  nameFlag=true;
																	  JsonNode dinNode;
																	  //JsonNode dinNode =data.get(i).get("din"); 
																	  if(innerNode.isArray()) {
																		  dinNode =innerNode.get("din");
																		  }else {
																			  dinNode=innerNode.get("din");
																		  }
																	  if(dinNode!=null) {
																		  din=dinNode.asText(); 
																	  }
																	  
																}else if(Utility.checkContains(name,firstName) ||
																		Utility.checkContains(name,lastName)) {
																	  manual = true;
																	  manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
																	  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
																	  nameFlag=true;
																}
															 }else if(names.length==2) { 
																 	firstName=names[0];
																 	lastName=names[1];
																 	logger.info("Value of Split Names"+firstName+lastName+name);
																 	if(Utility.checkContains(name,firstName) &&
																			Utility.checkContains(name,lastName)) {
																		   recordFound = true;
																		  recordFoundStr = "Auto Tag Cross directorship as Record found with Date of Birth Match";
																		  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
																		  nameFlag=true;
																		  JsonNode dinNode;
																		  //JsonNode dinNode =data.get(i).get("din"); 
																		  if(innerNode.isArray()) {
																			  dinNode =innerNode.get("din");
																			  }else {
																				  dinNode=innerNode.get("din");
																			  }
																		  if(dinNode!=null) {
																			  din=dinNode.asText(); 
																		  }
																		  
																	}else if(Utility.checkContains(name,firstName) ||
																			Utility.checkContains(name,lastName)) {
																		  manual = true;
																		  manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
																		  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
																		  nameFlag=true;
																	}
															}else{ 
																firstName=personalInfoSearch.getName(); 
																logger.info("Value of Split Names"+firstName+name);
																if(Utility.checkContains(name,firstName)) {
																	  manual = true;
																	  manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
																	  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
																	  nameFlag=true;
																}
															 }
												  		}
												  	}
											  	
										  	}
										  if(!nameFlag) {
												//resultMCA.put("MCA Output","Auto Tag Cross directorship as clear");
												clear = true;
												clearStr = "Auto Tag Cross directorship as clear";
												logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
											} 
//										}else{
//											//resultMCA.put("MCA Output","Data is Empty.Auto Tag Cross directorship as clear");
//											logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
//											clear = true;
//											clearStr = "Data is Empty.Auto Tag Cross directorship as clear";
//										}			  
								  }else if(typeNode.equalsIgnoreCase("mca_inhouse") || typeNode.equalsIgnoreCase("google_dir")){
									  //ArrayNode data=(ArrayNode)results.get(0).get("data"); 
									  //Check for data Size and iterate over it
									  //if(data!=null && data.size()>0) {
										  Boolean nameFlag=false;
										  //for(int i=0;i<data.size();i++) {
//											  JsonNode nameNode;
//											  if(data.get(i).isArray()) {
//												 nameNode =data.get(i).get(0).get("name");
//											  }else {
//												  nameNode=data.get(i).get("name");
//											  }
											 //JsonNode nameNode=data.get(i).get("name");
											  if(nameNode!=null) { 
												  //String name= data.get(i).get("name").asText();
												  String name= nameNode.asText();
												  logger.info("Value of name"+name);
												  logger.info("Value of personal Info Search"+personalInfoSearch.getName());
												  if(name.equalsIgnoreCase(personalInfoSearch.getName())) { 
													  //resultMCA.put("MCA Output","Send Cross directorship check for Manual review as Record found with only Name match");
													  manual = true;
													  manualStr = "Send Cross directorship check for Manual review as Record found with only Name match";
													  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
													  nameFlag=true;
													  JsonNode dinNode;
													  //JsonNode dinNode =data.get(i).get("din"); 
//													  if(innerNode.isArray()) {
//														  dinNode =innerNode.get("din");
//														  }else {
//															  dinNode=innerNode.get("din");
//														  }
//													  if(dinNode!=null) {
//														  din=dinNode.asText(); 
//													  } 
													  //break;
												  	}else {

												  		if(Utility.checkContains(name, personalInfoSearch.getName())) {
												  			  manual = true;
															  manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
															  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
															  nameFlag=true;
												  		}else {
												  			/*
															 * Split Name in First name, Middle Name and last Name
															 */
															String [] names=personalInfoSearch.getName().split(" ");
															logger.info("Value of String After Split"+names);
															String firstName=null,lastName=null,middleName=null;
															if(names.length==3) {
																firstName=names[0];
																lastName=names[2];
																middleName=names[1];
																logger.info("Value of Split Names"+firstName+lastName+middleName+name);
																if(Utility.checkContains(name,firstName) ||
																		Utility.checkContains(name,lastName)) {
																	  manual = true;
																	  manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
																	  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
																	  nameFlag=true;
																}
															 }else if(names.length==2) { 
																 	firstName=names[0];
																 	lastName=names[1];
																 	logger.info("Value of Split Names"+firstName+lastName+name);
																 	if(Utility.checkContains(name,firstName) ||
																			Utility.checkContains(name,lastName)) {
																		  manual = true;
																		  manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
																		  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
																		  nameFlag=true;
																	}
															}else{ 
																firstName=personalInfoSearch.getName(); 
																logger.info("Value of Split Names"+firstName+name);
																if(Utility.checkContains(name,firstName)) {
																	  manual = true;
																	  manualStr = "Send Cross directorship check for Manual review as Record found with only Name match (Contains)";
																	  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
																	  nameFlag=true;
																}
															 }
												  		}
												  	}
											  	}
										  	//}
										  if(!nameFlag) {
												//resultMCA.put("MCA Output","Auto Tag Cross directorship as clear");
												clear = true;
												clearStr = "Auto Tag Cross directorship as clear";
												logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
											} 
//										}else{
//											//resultMCA.put("MCA Output","Data is Empty.Auto Tag Cross directorship as clear");
//											clear = true;
//											clearStr = "Data is Empty.Auto Tag Cross directorship as clear";
//											logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
//										}	
								  }else if(typeNode.equalsIgnoreCase("no_data")) {
									  //resultMCA.put("MCA Output", "Auto Tag Cross directorship as clear.status fields has value no_status");
									  clear = true;
									  clearStr = "Auto Tag Cross directorship as clear.status fields has value no_status";
									  logger.info("Status is no_data");
								  }else {
									  //resultMCA.put("MCA Output", "Auto Tag Cross directorship as clear.");
									  clear = true;
									  clearStr = "Auto Tag Cross directorship as clear.";
									  logger.info("Status is no_data");			  
								  }
							  
								  /////////////////////////////////////////////////////////////
							  }
							  
						  }else {
							  clear = true;
							  clearStr = "Auto Tag Cross directorship as clear.";
							  logger.info("data is Empty");	
						  }
						  
						  String status =results.get(0).get("status").asText();
						  logger.info("Value of Status is"+status);
						  /*
						   * Logic for Status no_data
						   */
						  if(status.equalsIgnoreCase("no_data")) {
							  clear = true;
							  clearStr = "Auto Tag Cross directorship as clear.";
							  logger.info("Status is no_data");	
						  }
						  List<String> statusList=new ArrayList<String>();
						  if(status.contains(",")) {
							  statusList=Arrays.asList(status.split(","));
						  }else {
							  statusList.add(status);
						  }
						  for(String status1 : statusList ) {
							 /* if(status1.equalsIgnoreCase("mca_dob")) {
								  ArrayNode data=(ArrayNode)results.get(0).get("data"); 
								  //Check for data Size and iterate over it
								  if(data!=null && data.size()>0) {
									  Boolean nameFlag=false;
									  for(int i=0;i<data.size();i++) {
										  JsonNode nameNode;
										  if(data.get(i).isArray()) {
											 nameNode =data.get(i).get(0).get("name");
										  }else {
											  nameNode=data.get(i).get("name");
										  }
										  //JsonNode nameNode=data.get(i).get("name");
										  if(nameNode!=null) { 
											  //String name= data.get(i).get("name").asText();
											  String name= nameNode.asText();
											  logger.info("Value of name"+name);
											  logger.info("Value of personal Info Search"+personalInfoSearch.getName());
											  
											  if(name.equalsIgnoreCase(personalInfoSearch.getName())) { 
												  //resultMCA.put("MCA Output","Auto Tag Cross directorship as Record found with Date of Birth Match");
												  recordFound = true;
												  recordFoundStr = "Auto Tag Cross directorship as Record found with Date of Birth Match";
												  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
												  nameFlag=true;
												  JsonNode dinNode;
												  //JsonNode dinNode =data.get(i).get("din"); 
												  if(data.get(i).isArray()) {
													  dinNode =data.get(i).get(0).get("din");
													  }else {
														  dinNode=data.get(i).get("din");
													  }
												  if(dinNode!=null) {
													  din=dinNode.asText(); 
												  } 
												  //break;
											  	}
										  	}
									  	}
									  if(!nameFlag) {
											//resultMCA.put("MCA Output","Auto Tag Cross directorship as clear");
											clear = true;
											clearStr = "Auto Tag Cross directorship as clear";
											logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
										} 
									}else{
										//resultMCA.put("MCA Output","Data is Empty.Auto Tag Cross directorship as clear");
										logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
										clear = true;
										clearStr = "Data is Empty.Auto Tag Cross directorship as clear";
									}			  
							  }else if(status1.equalsIgnoreCase("mca_inhouse") || status1.equalsIgnoreCase("google_dir")){
								  ArrayNode data=(ArrayNode)results.get(0).get("data"); 
								  //Check for data Size and iterate over it
								  if(data!=null && data.size()>0) {
									  Boolean nameFlag=false;
									  for(int i=0;i<data.size();i++) {
										  JsonNode nameNode;
										  if(data.get(i).isArray()) {
											 nameNode =data.get(i).get(0).get("name");
										  }else {
											  nameNode=data.get(i).get("name");
										  }
										 //JsonNode nameNode=data.get(i).get("name");
										  if(nameNode!=null) { 
											  //String name= data.get(i).get("name").asText();
											  String name= nameNode.asText();
											  logger.info("Value of name"+name);
											  logger.info("Value of personal Info Search"+personalInfoSearch.getName());
											  if(name.equalsIgnoreCase(personalInfoSearch.getName())) { 
												  //resultMCA.put("MCA Output","Send Cross directorship check for Manual review as Record found with only Name match");
												  manual = true;
												  manualStr = "Send Cross directorship check for Manual review as Record found with only Name match";
												  logger.info("Auto Tag Cross directorship as Record found"+resultMCA); 
												  nameFlag=true;
												  JsonNode dinNode;
												  //JsonNode dinNode =data.get(i).get("din"); 
												  if(data.get(i).isArray()) {
													  dinNode =data.get(i).get(0).get("din");
													  }else {
														  dinNode=data.get(i).get("din");
													  }
												  if(dinNode!=null) {
													  din=dinNode.asText(); 
												  } 
												  //break;
											  	}
										  	}
									  	}
									  if(!nameFlag) {
											//resultMCA.put("MCA Output","Auto Tag Cross directorship as clear");
											clear = true;
											clearStr = "Auto Tag Cross directorship as clear";
											logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
										} 
									}else{
										//resultMCA.put("MCA Output","Data is Empty.Auto Tag Cross directorship as clear");
										clear = true;
										clearStr = "Data is Empty.Auto Tag Cross directorship as clear";
										logger.info("Auto Tag Cross directorship as clear"+resultMCA); 
									}	
							  }else if(status1.equalsIgnoreCase("no_data")) {
								  //resultMCA.put("MCA Output", "Auto Tag Cross directorship as clear.status fields has value no_status");
								  clear = true;
								  clearStr = "Auto Tag Cross directorship as clear.status fields has value no_status";
								  logger.info("Status is no_data");
							  }else {
								  //resultMCA.put("MCA Output", "Auto Tag Cross directorship as clear.");
								  clear = true;
								  clearStr = "Auto Tag Cross directorship as clear.";
								  logger.info("Status is no_data");			  
							  }*/
						  }
					  }else {
						  //resultMCA.put("MCA Output", "Auto Tag Cross directorship as clear.Result has no status fields");
						  clear = true;
						  clearStr = "Auto Tag Cross directorship as clear.Result has no status fields";
						  logger.info("Result has no status fields");
					  }
					 }else {
						 //resultMCA.put("MCA Output", "Auto Tag Cross directorship as clear.MCA Response has no Result fields");
						 clear = true;
						 clearStr = "Auto Tag Cross directorship as clear.MCA Response has no Result fields";
						 logger.info("MCA Response has no Result fields");
					 }
				  }
		}
		String finalResult = "";
		String finalStatus = "";
		if(pending) {
			finalResult = pendingStr;
			finalStatus = "Pending Request";
		}
		else if(recordFound) {
			finalResult = recordFoundStr;
			finalStatus = "Record Found";
		}else if(manual) {
			finalResult = manualStr;
			finalStatus = "Manual";
		} else if(clear) {
			finalResult = clearStr;
			finalStatus = "Clear";
		}
		
		resultMCA.put("MCA Output", finalResult);
		resultMCA.put("status", finalStatus);
		//resultMCA.put("verify_id", finalStatus);
		
		return din;
	}

	private void callback(String postStr) {
		try {

			logger.debug("postStr\n" + postStr);
			URL url = new URL(env.getProperty("online.router.callback.url"));
			logger.debug("Using callback URL\n" + url);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			// Creating the ObjectMapper object
			String authToken = JsonParser.parseString(postStr).getAsJsonObject().get("metadata").getAsJsonObject()
					.get("requestAuthToken").getAsString();

			logger.debug("Auth Token\n" + authToken);
			con.setRequestProperty("tokenId", authToken);

			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write(postStr.getBytes());
			os.flush();
			os.close();

			int responseCode = con.getResponseCode();
			logger.debug("Callback POST Response Code: " + responseCode + " : " + con.getResponseMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
