package com.gic.fadv.verification.checks.pojo;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class RootPOJO {
	public MetadataPOJO metadata;
	public List<DatumPOJO> data;
}