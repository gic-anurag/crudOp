package com.gic.fadv.verification.docs.pojo;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Response {
	private String id;
	private String caseId;
	private List<AssociateDocs> associateDocs;

}
