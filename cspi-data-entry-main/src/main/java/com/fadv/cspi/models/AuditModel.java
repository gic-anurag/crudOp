package com.fadv.cspi.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value= {"createdDate", "updatedDate", "createdByUser", "updatedByUser"}, allowGetters = false)
public class AuditModel implements Serializable {

	private static final long serialVersionUID = 1L;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT Now()")
	@CreatedDate
	private Date createdDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT Now()")
	@LastModifiedDate
	private Date updatedDate;
	
	@Column(columnDefinition = "VARCHAR(255) DEFAULT ''")
	private String createdByUser;
	
	@Column(columnDefinition = "VARCHAR(255) DEFAULT ''")
	private String updatedByUser;
}
