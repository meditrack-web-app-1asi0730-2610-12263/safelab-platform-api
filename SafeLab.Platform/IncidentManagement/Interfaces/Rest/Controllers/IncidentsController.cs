using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.IncidentManagement.Interfaces.Rest.Controllers;

[Route("api/v1/incidents")]
[Route("incidents")]
public class IncidentsController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "incidents";
    protected override bool ReturnSingleDocument => false;
}
