package com.gic.fadv.suspect.model;

import java.util.List;

import lombok.Data;

@Data
public class StellarRes {
	
	private MetaData metadata;
	public List<DatumRes> data;


}
