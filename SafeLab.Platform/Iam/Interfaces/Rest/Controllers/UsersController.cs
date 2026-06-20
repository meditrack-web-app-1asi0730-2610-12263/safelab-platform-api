using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.Iam.Interfaces.Rest.Controllers;

[Route("api/v1/users")]
[Route("users")]
public class UsersController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "users";
    protected override bool ReturnSingleDocument => false;
}
