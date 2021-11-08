package com.gic.fadv.verification.mapping.pojo;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class VendorQuestionaireTransactionPojo {

	@NotNull
	private Long id;
	@NotNull
	private String updatedtext;

	private String status;

	private String verifiedData;
	private String componentName;
}