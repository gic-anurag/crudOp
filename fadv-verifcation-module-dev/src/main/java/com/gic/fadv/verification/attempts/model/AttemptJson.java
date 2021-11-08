package com.gic.fadv.verification.attempts.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import lombok.Data;

@Entity
@Data
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class AttemptJson {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private long attemptId;
	@Type(type = "jsonb")
	@Column(columnDefinition = "jsonb",name="json_data")
    private String jsonData;
	private String messageId;
	private String status;// DEFAULT 'Pending',
	private Date createDate;
	private Date updateDate;

}
