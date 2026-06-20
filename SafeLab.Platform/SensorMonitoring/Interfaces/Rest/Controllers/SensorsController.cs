using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.SensorMonitoring.Interfaces.Rest.Controllers;

[Route("api/v1/sensors")]
[Route("sensors")]
public class SensorsController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "sensors";
    protected override bool ReturnSingleDocument => false;
}
