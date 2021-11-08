package com.gic.fadv.verification.attempts.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gic.fadv.verification.attempts.model.AttemptHistory;
import com.gic.fadv.verification.online.pojo.OnlineAttemptHistoryPOJO;

@Service
public interface AttemptHistoryService {

	AttemptHistory getAttemptHistory(ObjectMapper mapper, OnlineAttemptHistoryPOJO attemptHistoryPOJO);

}
