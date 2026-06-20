using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.SubscriptionBilling.Interfaces.Rest.Controllers;

[Route("api/v1/payments")]
[Route("payments")]
public class PaymentsController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "payments";
    protected override bool ReturnSingleDocument => false;
}
