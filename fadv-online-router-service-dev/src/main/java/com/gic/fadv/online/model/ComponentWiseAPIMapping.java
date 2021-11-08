package com.gic.fadv.online.model;

import java.util.ArrayList;
import java.util.Date;

import lombok.Data;


@Data
public class ComponentWiseAPIMapping {
	private long id;
	private String serviceName;//service_name;
	private String componentName;//component_name;
	private String productName;//product_name; //Sub Component Name
	private Object inputParams = new ArrayList<InputParams>();
	/*
	 * inputs_params = [{"apiFieldName": "name", "ngFieldName":
	 * "name as per bvf","isPrimary":true,"namingCombination":["fn,mn,ln","ln,mn,fn"
	 * ,"fn,ln","ln,fn"]}, {"apiFieldName": "dob", "ngFieldName":
	 * "NAME AS PER PAN CARD","isPrimary":false,"namingCombination":[]}]
	 */
	private String status;
	private Date createdDateTime;
    private Date updatedDateTime;
}
