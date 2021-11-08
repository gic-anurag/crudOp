package com.gic.fadv.suspect.model;

import java.util.List;

import lombok.Data;

@Data
public class SuspectReq {

	private MetaData metadata;
	public List<Datum> data;

}
