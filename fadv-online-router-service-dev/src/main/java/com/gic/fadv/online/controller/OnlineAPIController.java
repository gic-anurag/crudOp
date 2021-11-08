package com.gic.fadv.online.controller;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import com.gic.fadv.online.task.AdverseMediaTask;
import com.gic.fadv.online.task.LoanDefaulterTask;
import com.gic.fadv.online.task.MCATask;
import com.gic.fadv.online.task.ManupatraTask;
import com.gic.fadv.online.task.WorldCheckTask;
import com.gic.fadv.online.utility.Utility;
import com.gic.fadv.online.exception.ResourceNotFoundException;
import com.google.gson.JsonParser;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/india")
@Api(value = "Cross Reference", description = "Operations pertaining to Online API Controller (Cross Reference master data)")
public class OnlineAPIController {

	@Autowired
	private Environment env;
	@Autowired
	private OnlineApiService onlineApiService;
	@Autowired
	private ParseAPIResponseService parseAPIResponseService;

	private static final Logger logger = LoggerFactory.getLogger(OnlineAPIController.class);

	@ApiOperation(value = "This service is used to process Records at Online API and return the result ", response = List.class)
	@PostMapping(path = "/async/onlineapi", consumes = "application/json", produces = "application/json")
	public DeferredResult<ResponseEntity<String>> doAsyncProcess(@RequestBody String inStr)
			throws ResourceNotFoundException {
		DeferredResult<ResponseEntity<String>> ret = new DeferredResult<>();
		ForkJoinPool.commonPool().submit(() -> {
			logger.info("Got async Request:\n" + inStr);
			try {
				processRequest(inStr, true);
			} catch (ResourceNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ret.setResult(ResponseEntity.ok("ok"));
		});
		ret.onCompletion(() -> logger.info("async process request done"));
		return ret;
	}

	@ApiOperation(value = "This service is used to process Records at Online API and return the result ", response = List.class)
	@PostMapping(path = "/onlineapi", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> doProcess(@RequestBody String inStr) throws ResourceNotFoundException {
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

	private String processRequest(String inStr, boolean asyncStatus) throws ResourceNotFoundException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode finalResult = mapper.createObjectNode();
		ExecutorService threadPool = Executors.newFixedThreadPool(5);
		final CountDownLatch latch = new CountDownLatch(5);
		Future<ObjectNode> f1 = threadPool.submit(new MCATask(inStr, latch, onlineApiService, parseAPIResponseService));
		Future<ObjectNode> f2 = threadPool
				.submit(new LoanDefaulterTask(inStr, latch, onlineApiService, parseAPIResponseService));
		Future<ObjectNode> f3 = threadPool
				.submit(new WorldCheckTask(inStr, latch, onlineApiService, parseAPIResponseService));
		Future<ObjectNode> f4 = threadPool
				.submit(new ManupatraTask(inStr, latch, onlineApiService, parseAPIResponseService));
		Future<ObjectNode> f5 = threadPool
				.submit(new AdverseMediaTask(inStr, latch, onlineApiService, parseAPIResponseService));

		try {
			latch.await();
			// main thread is waiting on CountDownLatch to finish
			callAPIResponse(finalResult, f1, f2, f3, f4, f5);
			logger.info("All services are completed, Application is starting now");
		} catch (InterruptedException ie) {
			logger.info("Exception Occured on latch await:{}", ie.getMessage());
			finalResult.put("Result", "Some Exception occured on waiting timeout.Please try again.");
		} finally {
			threadPool.shutdown();
		}
		String returnStr = finalResult.toString();
		if (asyncStatus)
			callback(returnStr);
		return returnStr;
	}

	private void callAPIResponse(ObjectNode finalResult, Future<ObjectNode> f1, Future<ObjectNode> f2,
			Future<ObjectNode> f3, Future<ObjectNode> f4, Future<ObjectNode> f5) throws InterruptedException {
		try {
//				System.out.println("Value of F1"+f1.get()); 
//				System.out.println("Value of F2"+f2.get()); 
//				System.out.println("Value of F3"+f3.get()); 
//				System.out.println("Value of F4"+f4.get()); 
//				System.out.println("Value of F5"+f5.get()); 
			buildAPIResponse(finalResult, f1, f2, f3, f4, f5);
		} catch (ExecutionException e) {
			logger.info("Exception Occured while making the result" + e.getMessage());
			e.printStackTrace();
			finalResult.put("Result", "Some Exception Occured.Please try again.");
		}
	}

	private void buildAPIResponse(ObjectNode finalResult, Future<ObjectNode> f1, Future<ObjectNode> f2,
			Future<ObjectNode> f3, Future<ObjectNode> f4, Future<ObjectNode> f5)
			throws InterruptedException, ExecutionException {
		if (f1.get() != null) {
			finalResult.set("MCA", f1.get().get("MCA"));
		} else {
			finalResult.set("MCA", f1.get());
		}
		if (f1.get() != null) {
			finalResult.set("WatchOut", f1.get().get("WatchOut"));
		} else {
			finalResult.set("WatchOut", f1.get());
		}
		if (f2.get() != null) {
			finalResult.set("Loan Defaulter", f2.get().get("Loan Defaulter"));
		} else {
			finalResult.set("Loan Defaulter", f2.get());
		}
		if (f3.get() != null) {
			finalResult.set("WorldCheck", f3.get().get("WorldCheck"));
		} else {
			finalResult.set("WorldCheck", f3.get());
		}
		if (f4.get() != null) {
			finalResult.set("Manupatra", f4.get().get("Manupatra"));
		} else {
			finalResult.set("Manupatra", f4.get());
		}
		if (f5.get() != null) {
			finalResult.set("Adverse Media", f5.get().get("Adverse Media"));
		} else {
			finalResult.set("Adverse Media", f5.get());
		}
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
