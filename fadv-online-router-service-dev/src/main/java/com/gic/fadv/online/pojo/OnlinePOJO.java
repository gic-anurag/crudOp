package com.gic.fadv.online.pojo;
import lombok.Data;

@Data
public class OnlinePOJO {

	
	private long id;
	private Long orgId;
	private String companyName;
	private String companyAkaName;
	private String productName;
	private String reason;
	private String remarks;
	private String flag;
		
	public OnlinePOJO() {
		super();
	}
	
	
}

