package com.gic.fadv.verification.mapping.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "vendor_component_master")
public class VendorComponentMaster implements Serializable {
	/**
	 * /**
	 * 
	 */
	private static final long serialVersionUID = -5947310472718236350L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "component_name",columnDefinition = "text")
	private String componentName;


}
