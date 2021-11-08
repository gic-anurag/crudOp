package fadv.verification.workflow.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CheckPOJO {
	private String requestId;
	private String checkID;
	private String productName;
	private String akaName;
	private String componentName;
}
