using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.SubscriptionBilling.Interfaces.Rest.Controllers;

[Route("api/v1/invoices")]
[Route("invoices")]
public class InvoicesController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "invoices";
    protected override bool ReturnSingleDocument => false;
}
