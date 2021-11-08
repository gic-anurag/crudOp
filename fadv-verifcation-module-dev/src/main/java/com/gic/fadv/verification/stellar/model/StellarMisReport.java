package com.gic.fadv.verification.stellar.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class StellarMisReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// check id creation date
	private String checkIdCreationDate;

	// Component
	private String componentName;

	// Check ID
	private String checkId;

	// Client Name
	private String clientName;

	// CRN
	private String crnNo;

	// Candidate Name
	private String candidateName;

	// College Name
	private String collegeName;

	// University Name
	private String universityName;

	// Qualification
	private String qualification;

	// Major
	private String major;

	// Number Type1
	private String numberType1;

	// Unique1
	private String unique1;

	// Number Type1
	private String numberType2;

	// Unique2
	private String unique2;

	// Month & Year
	private String monthYear;

	// Year of Graduation
	private String yearOfGrad;

	// Class Obtained
	private String classObtained;

	// Document Sent
	private String documentSent;

	// Special Notes / Comments (re-verification / additional cost / any other
	// special note)
	private String specialNotes;

	// Stellar Availability Yes/No
	private String stellarAvailability;

	// QR Code
	private String qrCode;

	private Date createdDate;

	private Date updatedDate;

}
