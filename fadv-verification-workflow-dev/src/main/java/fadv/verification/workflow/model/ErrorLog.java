package fadv.verification.workflow.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.TypeDef;

import lombok.Data;

@Entity
@Data
public class ErrorLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long errorLogId;
	private String checkId;
	private String engineName;
	private String createdDate;
	private String engineCurrentStatus;
	private String  remarks;// (Multiple Entries)
	private Long eventStatusId; //Take from Event status table
}
