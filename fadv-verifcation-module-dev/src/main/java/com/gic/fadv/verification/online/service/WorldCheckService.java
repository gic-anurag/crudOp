package com.gic.fadv.verification.online.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ArrayNode;

@Service
public interface WorldCheckService {

	Map<String, String> updateWorldCheckStatus(ArrayNode requestArrayNode);

}
