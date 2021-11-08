package com.gic.fadv.spoc.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
@Data
public class ComponentScoping {
	
	@JsonProperty("SBU_NAME") 
    public String sBU_NAME;
    @JsonProperty("CRNCreationDate") 
    public String cRNCreationDate;
    @JsonProperty("Package Name") 
    public String packageName;
    public JsonNode caseReference;
    @JsonProperty("CASE_NUMBER") 
    public String cASE_NUMBER;
    @JsonProperty("CLIENT_NAME") 
    public String cLIENT_NAME;
    public JsonNode clientSpecificFields;
    @JsonProperty("Components") 
    public List<Component> components;
    @JsonProperty("CASE_UUID") 
    public String cASE_UUID;
    public JsonNode caseMoreInfo;
    @JsonProperty("Candidate_Name") 
    public String candidate_Name;
    @JsonProperty("CLIENT_CODE") 
    public String cLIENT_CODE;
    public JsonNode caseDetails;
    @JsonProperty("CASE_REF_NUMBER") 
    public String cASE_REF_NUMBER;
    @JsonProperty("TYPE") 
    public String tYPE;
    @JsonProperty("BaseEJCCountryList") 
    public Object baseEJCCountryList;

}
