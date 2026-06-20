using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.AssetInventoryMonitoring.Interfaces.Rest.Controllers;

[Route("api/v1/assets")]
[Route("assets")]
public class AssetsController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "assets";
    protected override bool ReturnSingleDocument => false;
}
