package fadv.verification.workflow.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class StatusPOJO {
	private boolean success;
	private String message;
	private String statusCode;
}
