package com.gic.fadv.verification.attempts.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.gic.fadv.verification.attempts.model.AttemptHistory;
import com.gic.fadv.verification.pojo.AttemptHistoryPOJO;


@Service
public interface APIService {

	String sendDataToPost(String jsonData);
	String sendDataToAttemptHistory(String jsonData);
	//public String sendDataToRest(String uri,String jsonData,Map<String,String> headerMap);
	//Used for Accessing the All Roles
	//public List<String> getAllRoles();
	//Used to add Realm level Roles
	//public String addRealmRole(String new_role_name);
	//Make Composite Role
	//public void makeComposite(String role_name);
	//Add Realm Role To User
	//public void addRealmRoleToUser(String userName, String role_name);
	String sendDataToAttempt(String requestUrl, String jsonData);
	
	String sendDataToVerificationEventStatus(String requestUrl, String jsonData);
	
	public List<AttemptHistory> getAttemptHistoryByFilter(AttemptHistoryPOJO attemptHistoryPOJO);
	String sendDataToL3ByCheckId(String requestUrl, List<String> checkIdList);
	String sendDataToget(String url, String param);
}
