using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.UserProfiles.Interfaces.Rest.Controllers;

[Route("api/v1/user-profiles")]
[Route("userProfiles")]
public class UserProfilesController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "userProfiles";
    protected override bool ReturnSingleDocument => false;
}
