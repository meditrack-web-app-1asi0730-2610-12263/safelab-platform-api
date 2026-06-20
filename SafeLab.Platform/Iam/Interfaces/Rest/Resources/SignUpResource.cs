namespace SafeLab.Platform.Iam.Interfaces.Rest.Resources;

public record SignUpResource(
    string FirstName,
    string LastName,
    string Username,
    string Email,
    string Phone,
    string Password,
    string Role,
    string Timezone);
