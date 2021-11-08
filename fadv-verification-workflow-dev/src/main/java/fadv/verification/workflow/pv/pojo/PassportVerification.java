package fadv.verification.workflow.pv.pojo;
import lombok.Data;
@Data
public class PassportVerification
{
    private String sharedPath;

    private String upperLine;

    private String lowerLine;
    
    //Used sending data to bot
    private String checkID;
    private String dateOfBirth;
    private String givenName;
    private String lastName;
    private String gender;
    private String issuingState;
    private String nationality;
    private String passportIdNumber;
    private String dateOfExpiry;

}
