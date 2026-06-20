using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.DashboardOverview.Interfaces.Rest.Controllers;

[Route("api/v1/bounded-contexts")]
[Route("boundedContexts")]
public class BoundedContextsController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "boundedContexts";
    protected override bool ReturnSingleDocument => false;
}
