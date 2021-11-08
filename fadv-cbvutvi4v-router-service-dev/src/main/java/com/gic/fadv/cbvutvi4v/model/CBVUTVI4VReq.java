package com.gic.fadv.cbvutvi4v.model;

import java.util.List;

import lombok.Data;

@Data
public class CBVUTVI4VReq {
	
	private MetaData metadata;

	public List<Datum> data;
    
    public CBVUTVI4VReq() {
		super();
	}

}
