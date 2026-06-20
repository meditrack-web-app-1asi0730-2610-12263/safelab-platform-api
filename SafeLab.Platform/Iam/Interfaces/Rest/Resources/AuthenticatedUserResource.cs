using System.Text.Json.Nodes;

namespace SafeLab.Platform.Iam.Interfaces.Rest.Resources;

public record AuthenticatedUserResource(JsonNode User, string Token, string TokenType = "Bearer");
