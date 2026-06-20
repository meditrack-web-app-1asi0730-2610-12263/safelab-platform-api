using System.Text.Json.Nodes;
using SafeLab.Platform.Iam.Application.CommandServices;
using SafeLab.Platform.Iam.Interfaces.Rest.Resources;
using SafeLab.Platform.Shared.Domain.Repositories;

namespace SafeLab.Platform.Iam.Application.Internal.CommandServices;

public class AuthenticationCommandService(IJsonDocumentRepository repository) : IAuthenticationCommandService
{
    private static readonly Dictionary<string, string[]> RoleAccessMap = new()
    {
        ["safeLabAdministrator"] = ["dashboard-overview", "asset-inventory", "sensor-monitoring", "environmental-compliance", "alerts-notifications", "remote-control", "reports-analytics", "incident-management", "audit-traceability", "subscription-billing"],
        ["bioclinicalCoordinator"] = ["dashboard-overview", "asset-inventory", "sensor-monitoring", "environmental-compliance", "alerts-notifications", "reports-analytics", "incident-management", "audit-traceability"],
        ["hospitalPharmacyCoordinator"] = ["dashboard-overview", "asset-inventory", "sensor-monitoring", "environmental-compliance", "alerts-notifications", "reports-analytics", "incident-management", "audit-traceability"],
        ["labTechnician"] = ["dashboard-overview", "asset-inventory", "sensor-monitoring", "alerts-notifications", "incident-management"],
        ["pharmacyTechnician"] = ["dashboard-overview", "asset-inventory", "sensor-monitoring", "alerts-notifications", "incident-management"],
        ["complianceOfficer"] = ["dashboard-overview", "environmental-compliance", "alerts-notifications", "reports-analytics", "incident-management", "audit-traceability"],
        ["maintenanceOperator"] = ["dashboard-overview", "sensor-monitoring", "alerts-notifications", "remote-control", "incident-management"],
        ["billingManager"] = ["dashboard-overview", "subscription-billing", "reports-analytics"]
    };

    private static readonly Dictionary<string, string> RoleLabels = new()
    {
        ["safeLabAdministrator"] = "SafeLab Administrator",
        ["bioclinicalCoordinator"] = "Bioclinical Operations Coordinator",
        ["hospitalPharmacyCoordinator"] = "Hospital Pharmacy Coordinator",
        ["labTechnician"] = "Laboratory Technician",
        ["pharmacyTechnician"] = "Pharmacy Technician",
        ["complianceOfficer"] = "Compliance Officer",
        ["maintenanceOperator"] = "Maintenance Operator",
        ["billingManager"] = "Billing Manager"
    };

    public async Task<AuthenticatedUserResource?> SignInAsync(SignInResource resource, CancellationToken cancellationToken = default)
    {
        var identifier = resource.Identifier ?? resource.Email ?? resource.Username ?? string.Empty;
        var users = await repository.ListAsync("users", cancellationToken);

        var user = users.FirstOrDefault(item =>
        {
            var username = item?["username"]?.GetValue<string>() ?? string.Empty;
            var email = item?["email"]?.GetValue<string>() ?? string.Empty;
            var password = item?["password"]?.GetValue<string>() ?? string.Empty;
            var status = item?["status"]?.GetValue<string>() ?? "active";

            return status == "active" &&
                   password == resource.Password &&
                   (username.Equals(identifier, StringComparison.OrdinalIgnoreCase) || email.Equals(identifier, StringComparison.OrdinalIgnoreCase));
        });

        return user is null ? null : CreateAuthenticatedUser(user);
    }

    public async Task<AuthenticatedUserResource?> SignUpAsync(SignUpResource resource, CancellationToken cancellationToken = default)
    {
        var users = await repository.ListAsync("users", cancellationToken);
        var exists = users.Any(item =>
        {
            var username = item?["username"]?.GetValue<string>() ?? string.Empty;
            var email = item?["email"]?.GetValue<string>() ?? string.Empty;
            return username.Equals(resource.Username, StringComparison.OrdinalIgnoreCase) || email.Equals(resource.Email, StringComparison.OrdinalIgnoreCase);
        });

        if (exists) return null;

        var fullName = $"{resource.FirstName} {resource.LastName}".Trim();
        var initials = $"{resource.FirstName.FirstOrDefault()}{resource.LastName.FirstOrDefault()}".ToUpperInvariant();
        var role = string.IsNullOrWhiteSpace(resource.Role) ? "bioclinicalCoordinator" : resource.Role;
        var position = RoleLabels.GetValueOrDefault(role, role);

        var allowedContexts = new JsonArray();
        foreach (var context in RoleAccessMap.GetValueOrDefault(role, []))
            allowedContexts.Add(context);

        var user = new JsonObject
        {
            ["username"] = resource.Username,
            ["email"] = resource.Email,
            ["password"] = resource.Password,
            ["firstName"] = resource.FirstName,
            ["lastName"] = resource.LastName,
            ["fullName"] = fullName,
            ["initials"] = initials,
            ["phone"] = resource.Phone,
            ["role"] = role,
            ["position"] = position,
            ["status"] = "active",
            ["organization"] = $"{fullName} Workspace",
            ["laboratory"] = "Main laboratory",
            ["timezone"] = resource.Timezone,
            ["segment"] = role.Contains("pharmacy", StringComparison.OrdinalIgnoreCase) ? "hospital-pharmacy" : "clinical-laboratory",
            ["facilityId"] = "FAC-DEMO",
            ["facilityName"] = "Demo SafeLab Facility",
            ["allowedContexts"] = allowedContexts,
            ["createdAt"] = DateTimeOffset.UtcNow.ToString("O"),
            ["updatedAt"] = DateTimeOffset.UtcNow.ToString("O")
        };

        var createdUser = await repository.AddAsync("users", user, cancellationToken);
        var createdUserId = createdUser["id"]?.GetValue<int>() ?? 0;

        var profile = new JsonObject
        {
            ["accountId"] = createdUserId,
            ["userId"] = $"USR-{createdUserId:000}",
            ["firstName"] = resource.FirstName,
            ["lastName"] = resource.LastName,
            ["fullName"] = fullName,
            ["initials"] = initials,
            ["email"] = resource.Email,
            ["phone"] = resource.Phone,
            ["role"] = role,
            ["status"] = "active",
            ["position"] = position,
            ["organization"] = $"{fullName} Workspace",
            ["laboratory"] = "Main laboratory",
            ["language"] = "en",
            ["timezone"] = resource.Timezone,
            ["notificationPreference"] = "critical-and-daily",
            ["lastAccess"] = DateTimeOffset.UtcNow.ToString("O"),
            ["lastPasswordChange"] = DateTimeOffset.UtcNow.ToString("O"),
            ["updatedAt"] = DateTimeOffset.UtcNow.ToString("O")
        };

        await repository.AddAsync("userProfiles", profile, cancellationToken);

        return CreateAuthenticatedUser(createdUser);
    }

    private static AuthenticatedUserResource CreateAuthenticatedUser(JsonNode user)
    {
        var userId = user["id"]?.GetValue<int>() ?? 0;
        var token = Convert.ToBase64String(Guid.NewGuid().ToByteArray()).Replace("=", string.Empty);
        return new AuthenticatedUserResource(user, $"safelab-demo-token-{userId}-{token}");
    }
}
