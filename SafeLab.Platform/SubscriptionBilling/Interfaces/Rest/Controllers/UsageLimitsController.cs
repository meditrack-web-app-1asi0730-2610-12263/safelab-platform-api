using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.SubscriptionBilling.Interfaces.Rest.Controllers;

[Route("api/v1/usage-limits")]
[Route("usageLimits")]
public class UsageLimitsController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "usageLimits";
    protected override bool ReturnSingleDocument => false;
}
