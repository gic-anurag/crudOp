package com.gic.fadv.cbvutvi4v.pojo;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.Data;

@Data
public class CaseSpecificInfoPOJO {
	private long caseSpecificId;
	private String caseReference;
	private String caseMoreInfo;
	private String caseDetails;
	private String clientSpecificFields;
	private String caseNumber;
	private String caseRefNumber;
	private String clientCode;
	private String clientName;
	private String sbuName;
	private String packageName;
	private String candidateName;
	private List<CaseSpecificRecordDetailPOJO> caseSpecificRecordDetail;
}
