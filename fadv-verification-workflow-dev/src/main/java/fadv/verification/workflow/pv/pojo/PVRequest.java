package fadv.verification.workflow.pv.pojo;
import java.util.List;

import lombok.Data;
@Data
public class PVRequest
{
    private Metadata metadata;

    private List<PVData> data;

}
