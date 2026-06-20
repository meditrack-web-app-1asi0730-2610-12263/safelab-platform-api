namespace SafeLab.Platform.Iam.Interfaces.Rest.Resources;

public record SignInResource(string? Username, string? Email, string? Identifier, string Password);
