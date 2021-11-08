package com.gic.fadv.suspect.service;

import org.springframework.stereotype.Service;

@Service
public interface SuspectApiService {

	String sendDataToSuspectRest(String jsonData);

	String sendDataToUniSuspectRest(String jsonData);

	// String sendDataToCbvUtvI4vRest(String jsonData);
}
