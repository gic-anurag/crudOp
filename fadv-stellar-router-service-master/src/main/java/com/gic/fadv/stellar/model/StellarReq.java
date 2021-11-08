package com.gic.fadv.stellar.model;

import java.util.List;

import lombok.Data;

@Data
public class StellarReq {
	
	private MetaData metadata;

	public List<Datum> data;
    
    public StellarReq() {
		super();
	}

}
