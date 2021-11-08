package com.gic.fadv.verification.online.model;

import java.util.List;

import lombok.Data;

@Data
public class VerificationStatus {
	private Metadata metadata;
	private List<Datum> data;
}