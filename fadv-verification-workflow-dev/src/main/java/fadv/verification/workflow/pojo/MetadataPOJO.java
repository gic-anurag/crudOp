package fadv.verification.workflow.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MetadataPOJO {
	private String botId;
	private String responseId;
	private String responseType;
	private String responseTime;
	private StatusPOJO status;
	private String processName;
	private String processId;
	private String stageId;
	private String task;
	private String taskGroupId;
	private String requestDate;
	private String requestType;
	private String requestId;
	private String version;
	private int attempt;
	private String multiTask;
	private String requestAuthToken;
}
