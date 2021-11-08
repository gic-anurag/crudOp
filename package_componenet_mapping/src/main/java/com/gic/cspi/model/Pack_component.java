package com.gic.cspi.model;



import javax.persistence.Column;



import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import com.gic.cspi.entity.AuditDetails;
import com.gic.cspi.entity.ProjectDetails;
import com.gic.cspi.entity.TenantDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Document(collection  = "package_component_mapping_master")
@AllArgsConstructor
@NoArgsConstructor

public class Pack_component {


	@Id
	

	@Column(name = "_id")
	private String id; 
	 
	
    private String packageName;
    private String packageId; 
    
    
    private boolean active;  
    
    private TenantDetails tenantDetails;
   
    private ProjectDetails projectDetails;
   
    private AuditDetails auditDetails;
    
    private String[] component;
}