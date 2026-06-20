using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.SubscriptionBilling.Interfaces.Rest.Controllers;

[Route("api/v1/subscriptions")]
[Route("subscriptions")]
public class SubscriptionsController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "subscriptions";
    protected override bool ReturnSingleDocument => false;
}
