package com.gic.fadv.verification.mapping.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name="questionaire_transaction")
@Data
public class QuestionaireTransaction implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7668241096975255772L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "questionaire_mapping_id")
	private Long questionaireMappingId;

	@Column(name = "field_mapping")
	private String fieldMapping;
}
