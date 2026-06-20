using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.ReportsAnalytics.Interfaces.Rest.Controllers;

[Route("api/v1/reports")]
[Route("reports")]
public class ReportsController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "reports";
    protected override bool ReturnSingleDocument => false;
}
