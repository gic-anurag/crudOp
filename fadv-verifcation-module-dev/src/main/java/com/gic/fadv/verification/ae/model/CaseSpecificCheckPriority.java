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
public class CaseSpecificCheckPriority
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long caseSpecificPriorityId;
    private String checkId;
    private String priority;
    @Builder.Default
    private Date priorityDate = new Date();
    @Builder.Default
    private boolean isAllocated = false;
}
