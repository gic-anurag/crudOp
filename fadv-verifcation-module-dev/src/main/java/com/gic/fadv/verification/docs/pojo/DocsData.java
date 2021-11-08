package com.gic.fadv.verification.docs.pojo;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class DocsData {
	
	private String rowId;
	private String documentName;
	private String documentId;
	private String startPage;
	private String endPage;
	private String filePath;
	private List<CheckIds> checkIds;
	private Boolean isFullfillment;

}
