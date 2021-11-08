package com.fadv.cspi.response.pojo;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class CaseAdditionalDetailsResponsePOJO {

	private long caseAdditionalDetailsId;

	private JsonNode address;

	private JsonNode contactNumber;

	private boolean singleDataEntry;

	private String akaMiddleName;

	private String akaLocalName;

	private String genderId;

	private String confidentialCase;

	private String authLetter;

	private String remarks;

	private String pointContactId;

	private String locationId;

	private String priorityId;

	private String typeOfCheckId;

	private String modeReceiptId;

	private String clientReference;

	private String localName;

	private String akaFirstName;

	private String akaLastName;

	private long caseDetailsId;
}
