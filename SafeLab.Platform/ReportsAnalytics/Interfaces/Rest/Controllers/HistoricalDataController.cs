using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.ReportsAnalytics.Interfaces.Rest.Controllers;

[Route("api/v1/historical-data")]
[Route("historicalData")]
public class HistoricalDataController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "historicalData";
    protected override bool ReturnSingleDocument => false;
}
