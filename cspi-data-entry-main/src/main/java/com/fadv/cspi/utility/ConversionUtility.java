package com.fadv.cspi.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConversionUtility {
	static ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private ConversionUtility() {
		throw new IllegalStateException("DataEntryUtility class");
	}

	public static String removeNonAlphaNumericAndSpace(String str) {
		str = str.trim().replaceAll("[^a-zA-Z0-9]+", "");
		return str.toLowerCase();
	}

	public static List<String> findKeys(JsonNode jsonValue) {
		List<String> keys = new ArrayList<>();
		Map<String, Object> treeMap = mapper.convertValue(jsonValue, new TypeReference<Map<String, Object>>() {
		});
		treeMap.forEach((key, value) -> keys.add(key));
		return keys;
	}
}
