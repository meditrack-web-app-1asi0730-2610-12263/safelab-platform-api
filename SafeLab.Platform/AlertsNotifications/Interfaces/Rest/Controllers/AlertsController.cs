using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.AlertsNotifications.Interfaces.Rest.Controllers;

[Route("api/v1/alerts")]
[Route("alerts")]
public class AlertsController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "alerts";
    protected override bool ReturnSingleDocument => false;
}
