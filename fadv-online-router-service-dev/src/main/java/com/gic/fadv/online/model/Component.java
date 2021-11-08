package com.gic.fadv.online.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class Component {
	
	@JsonProperty("CANDIDATE_DE_COMPLETED_DT") 
    private String cANDIDATE_DE_COMPLETED_DT;
    @JsonProperty("PRODUCT") 
    private String pRODUCT;
    @JsonProperty("Component name") 
    private String componentname;
    private Object componentName;
    private boolean dbComponent;
    @JsonProperty("Records") 
    public List<JsonNode> records;

}
