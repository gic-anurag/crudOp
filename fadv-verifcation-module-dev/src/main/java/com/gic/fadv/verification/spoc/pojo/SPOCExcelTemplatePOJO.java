package com.gic.fadv.verification.spoc.pojo;

import java.util.ArrayList;
import java.util.Date;
import lombok.Data;

@Data
public class SPOCExcelTemplatePOJO {
	private String contactCardName;
	private String templateNumber;
	private String componentName;
	private Object templateHeaders = new ArrayList<TemplateHeadersPOJO>();
	private String status;
	private Date createdDateTime;
	private Date updatedDateTime;
}