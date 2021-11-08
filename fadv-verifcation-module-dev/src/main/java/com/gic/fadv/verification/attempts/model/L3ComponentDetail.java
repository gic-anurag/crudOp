package com.gic.fadv.verification.attempts.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class L3ComponentDetail
{
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long componentDetailId;
	private String id;
    private String componentDesc;
    private String componentName;
    private String isDatabaseComponent;
    private String dataSource;
}