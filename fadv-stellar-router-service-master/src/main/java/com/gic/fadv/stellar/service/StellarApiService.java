package com.gic.fadv.stellar.service;

import org.springframework.stereotype.Service;

@Service
public interface StellarApiService {
	
	String sendDataToStellarRest(String jsonData); 
	String callInstraStellarMRLRouter(String jsonData);
}
