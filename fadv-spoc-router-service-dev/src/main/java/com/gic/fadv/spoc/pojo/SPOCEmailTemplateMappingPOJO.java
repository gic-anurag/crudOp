package com.gic.fadv.spoc.pojo;

import java.util.Date;

import lombok.Data;

@Data
public class SPOCEmailTemplateMappingPOJO
{
	private long id;
    private String contactCardName;
    private String templateNumber;
    private String componentName;
    private String emailOrExcel;
    //private String status;
    //private Date createdDateTime;
    //private Date updatedDateTime;
}