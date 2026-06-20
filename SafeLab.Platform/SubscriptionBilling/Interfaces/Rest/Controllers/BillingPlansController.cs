using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.SubscriptionBilling.Interfaces.Rest.Controllers;

[Route("api/v1/billing-plans")]
[Route("billingPlans")]
public class BillingPlansController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "billingPlans";
    protected override bool ReturnSingleDocument => false;
}
