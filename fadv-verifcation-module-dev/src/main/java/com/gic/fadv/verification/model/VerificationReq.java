package com.gic.fadv.verification.model;

import java.util.List;

import lombok.Data;

@Data
public class VerificationReq {
	
	private MetaData metadata;

	public List<Datum> data;
    
    public VerificationReq() {
		super();
	}

}
