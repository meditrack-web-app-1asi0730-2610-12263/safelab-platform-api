using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.AlertsNotifications.Interfaces.Rest.Controllers;

[Route("api/v1/notifications")]
[Route("notifications")]
public class NotificationsController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "notifications";
    protected override bool ReturnSingleDocument => false;
}
