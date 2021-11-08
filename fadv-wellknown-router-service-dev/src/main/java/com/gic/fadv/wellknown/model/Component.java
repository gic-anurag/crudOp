package com.gic.fadv.wellknown.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class Component {
	
	@JsonProperty("CANDIDATE_DE_COMPLETED_DT") 
    public String cANDIDATE_DE_COMPLETED_DT;
	public boolean addOnPackage;
    @JsonProperty("PRODUCT") 
    public String pRODUCT;
    @JsonProperty("Component name") 
    public String componentname;
    public Object componentName;
    public Object packageName;
    public boolean dbComponent;
    @JsonProperty("Records") 
    public List<JsonNode> records;

}
