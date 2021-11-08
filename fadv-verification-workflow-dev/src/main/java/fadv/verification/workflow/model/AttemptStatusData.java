package fadv.verification.workflow.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;


@Entity
@Data
public class AttemptStatusData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long statusId;
	private long attemptId;
	private long endstatusId;
	private long modeId;
	private long depositionId;
	private Date dateOfCreation = new Date();
}
