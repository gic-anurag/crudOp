package com.gic.fadv.spoc.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class SPOCEmailTemplatePOJO
{
	private long id;
    private String templateNumber;
    private String templateName;
    private String emailTemplate;
    private String templateDescription;
    private String componentName;
    private String status;
    private Date createdDateTime;
    private Date updatedDateTime;
}