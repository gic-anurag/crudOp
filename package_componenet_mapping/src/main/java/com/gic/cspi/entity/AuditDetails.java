package com.gic.cspi.entity;

import lombok.Data;

@Data
public class AuditDetails {

	private UpadatedDate ud;
	private String updatedBy;
	private createdDate cd;
	private String createdBy;
	private int version;
	
}
