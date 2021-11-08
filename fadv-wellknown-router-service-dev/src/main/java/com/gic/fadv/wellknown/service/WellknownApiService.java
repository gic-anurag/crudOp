package com.gic.fadv.wellknown.service;

import org.springframework.stereotype.Service;

@Service
public interface WellknownApiService {
	
	String sendDataToWellknownRest(String jsonData);
}
