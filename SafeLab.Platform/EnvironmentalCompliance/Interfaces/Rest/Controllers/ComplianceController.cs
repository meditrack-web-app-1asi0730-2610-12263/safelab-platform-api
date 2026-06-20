using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.EnvironmentalCompliance.Interfaces.Rest.Controllers;

[Route("api/v1/compliance")]
[Route("compliance")]
public class ComplianceController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "dashboardOverview";
    protected override bool ReturnSingleDocument => true;
}
