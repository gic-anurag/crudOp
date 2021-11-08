package com.gic.fadv.spoc.model;

import java.util.List;

import lombok.Data;

@Data
public class SPOCReq {
	
	private MetaData metadata;

	public List<Datum> data;
    
    public SPOCReq() {
		super();
	}

}
