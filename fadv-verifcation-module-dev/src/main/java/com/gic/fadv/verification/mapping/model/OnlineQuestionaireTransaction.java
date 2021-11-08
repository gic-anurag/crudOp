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
@Table(name="online_questionaire_transaction")
public class OnlineQuestionaireTransaction implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1291921774296651833L;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "db_questionaire_mapping_id")
	private Long dbQuestionaireMappingId;

	@Column(name = "field_mapping")
	private String fieldMapping;

}
