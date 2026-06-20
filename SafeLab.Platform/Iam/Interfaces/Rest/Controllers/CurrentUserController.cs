using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.Iam.Interfaces.Rest.Controllers;

[Route("api/v1/current-user")]
[Route("currentUser")]
public class CurrentUserController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "currentUser";
    protected override bool ReturnSingleDocument => true;
}
