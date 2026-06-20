using Microsoft.AspNetCore.Mvc;
using SafeLab.Platform.Iam.Application.CommandServices;
using SafeLab.Platform.Iam.Interfaces.Rest.Resources;

namespace SafeLab.Platform.Iam.Interfaces.Rest.Controllers;

[ApiController]
[Route("api/v1/authentication")]
[Route("api/auth")]
public class AuthenticationController(IAuthenticationCommandService authenticationCommandService) : ControllerBase
{
    [HttpPost("sign-in")]
    [HttpPost("login")]
    public async Task<IActionResult> SignIn([FromBody] SignInResource resource, CancellationToken cancellationToken)
    {
        var result = await authenticationCommandService.SignInAsync(resource, cancellationToken);
        return result is null ? Unauthorized(new { message = "Invalid username, email or password." }) : Ok(result);
    }

    [HttpPost("sign-up")]
    [HttpPost("register")]
    public async Task<IActionResult> SignUp([FromBody] SignUpResource resource, CancellationToken cancellationToken)
    {
        var result = await authenticationCommandService.SignUpAsync(resource, cancellationToken);
        return result is null ? Conflict(new { message = "Username or email is already registered." }) : Created(string.Empty, result);
    }
}
