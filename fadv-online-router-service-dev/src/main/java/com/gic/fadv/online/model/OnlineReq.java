package com.gic.fadv.online.model;

import java.util.List;

import lombok.Data;

@Data
public class OnlineReq {
	
	private MetaData metadata;

	public List<Datum> data;
    
    public OnlineReq() {
		super();
	}

}
