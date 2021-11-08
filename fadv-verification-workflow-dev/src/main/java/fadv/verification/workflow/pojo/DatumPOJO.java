package fadv.verification.workflow.pojo;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DatumPOJO {
	private String taskName;
	private String taskId;
	private int taskSerialNo;
	private ResultPOJO result;
	private MetricsPOJO metrics;
	private JsonNode logs;
	private boolean conditional;
}
