package fadv.verification.workflow.pojo;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class RootPOJO {
	private MetadataPOJO metadata;
	private List<DatumPOJO> data;
}