package com.gic.fadv.verification.docs.pojo;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AssociateDocs {
	private String docType;
	private String documentName;
	private String docId;
	private String sourceFolder;
	private Integer totalPages;
	private Boolean isAssociateAfterCaseReceivedFromSP;
	private List<DocsData>docsData;
}
