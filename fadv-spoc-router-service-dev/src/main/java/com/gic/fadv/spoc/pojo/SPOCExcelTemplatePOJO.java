package com.gic.fadv.spoc.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class SPOCExcelTemplatePOJO
{
	//private long id;
    private String contactCardName;
    private String templateNumber;
    private String componentName;
    private Object templateHeaders = new ArrayList<TemplateHeaders>();
    private String status;
    private Date createdDateTime;
    private Date updatedDateTime;
}