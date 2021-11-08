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
@Table(name="vendor_questionaire_transaction")
public class VendorQuestionaireTransaction implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 7912390036119793248L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "questionaire_mapping_id")
	private Long questionaireMappingId;

	@Column(name = "field_mapping")
	private String fieldMapping;
	
	@Column(name = "status")
    private String status;
	
	@Column(name = "verified_data")
	private String verifiedData;
	
	private String componentName;
	
	private String type;
	
}
