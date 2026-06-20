using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.SubscriptionBilling.Interfaces.Rest.Controllers;

[Route("api/v1/user-subscriptions")]
[Route("userSubscriptions")]
public class UserSubscriptionsController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "userSubscriptions";
    protected override bool ReturnSingleDocument => false;
}
