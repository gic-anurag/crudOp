package com.gic.fadv.verification.docs.pojo;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ResponseAssociateDocs {
	private Boolean success;
	private String successMsg;
	private String successCode;
	private Response response;

}
