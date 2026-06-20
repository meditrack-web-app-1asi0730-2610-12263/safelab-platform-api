using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.DashboardOverview.Interfaces.Rest.Controllers;

[Route("api/v1/dashboard-overview")]
[Route("dashboardOverview")]
public class DashboardOverviewController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "dashboardOverview";
    protected override bool ReturnSingleDocument => true;
}
