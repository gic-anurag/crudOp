package com.gic.fadv.verification.ae.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseSpecificCheckAllocation
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long caseCheckAllocationId;
    private String checkId;
    private long userId;
    @Builder.Default
    private Date checkCreatedDate = new Date();
}
