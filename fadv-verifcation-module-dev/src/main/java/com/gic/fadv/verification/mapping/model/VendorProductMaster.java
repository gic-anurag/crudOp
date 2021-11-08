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
@Table(name = "vendor_product_master")
public class VendorProductMaster implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -653246302821041416L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "product_name",columnDefinition = "text")
	private String productName;

	@Column(name = "componentid_fk",nullable = false)
	private Long componentidFk;
}
