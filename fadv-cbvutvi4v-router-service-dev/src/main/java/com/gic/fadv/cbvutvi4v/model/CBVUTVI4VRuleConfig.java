package com.gic.fadv.cbvutvi4v.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
@Entity
@Table(name="cbvutvi4v_rule_configs")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CBVUTVI4VRuleConfig {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	private String clientCode;
	private String sbu;
	private String packageCode;
	private String componentName;
	private String subComponentName;
	private String modelNamespace;
	private String modelName;
	private String description;
	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb")
	private String configs;
	private String status;
	private Date creationDate = new Date();
}
