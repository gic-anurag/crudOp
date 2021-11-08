package fadv.verification.workflow.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MetricsPOJO {
	private String startTime;
	private String endTime;
	private long timeInMills;
	private int timeInSeconds;
	private String statusCode;
}
