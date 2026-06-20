using SafeLab.Platform.Iam.Interfaces.Rest.Resources;

namespace SafeLab.Platform.Iam.Application.CommandServices;

public interface IAuthenticationCommandService
{
    Task<AuthenticatedUserResource?> SignInAsync(SignInResource resource, CancellationToken cancellationToken = default);
    Task<AuthenticatedUserResource?> SignUpAsync(SignUpResource resource, CancellationToken cancellationToken = default);
}
