package fadv.verification.workflow.pojo;

import java.util.concurrent.Future;

import lombok.Data;

@Data
public class FutureResultPOJO {
	private Future<String> onlineFuture;
	private Future<String> spocFuture;
	private Future<String> cbvUtvFuture;
	private Future<String> vendorInputFuture;
	private Future<String> suspectFuture;
	private Future<String> wellknownFuture;
	private Future<String> passportFuture;
}
