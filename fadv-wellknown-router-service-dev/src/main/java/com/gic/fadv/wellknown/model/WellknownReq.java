package com.gic.fadv.wellknown.model;

import java.util.List;

import lombok.Data;

@Data
public class WellknownReq {
	
	private MetaData metadata;

	public List<Datum> data;
    
    public WellknownReq() {
		super();
	}

}
