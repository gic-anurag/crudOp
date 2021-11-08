package com.gic.fadv.stellar.model;

import java.util.List;

import lombok.Data;

@Data
public class StellarRes {
	
	private MetaData metadata;

	public List<DatumRes> data;
    
    public StellarRes() {
		super();
	}

}
