package com.gic.fadv.verification.mapping.pojo;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class QuestionaireRequestPojo {
	
	@NotNull
	private String component;
	@NotNull
	private String productName;
	
	private String type;

}
