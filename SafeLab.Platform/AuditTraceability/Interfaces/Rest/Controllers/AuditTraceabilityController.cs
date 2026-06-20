using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.AuditTraceability.Interfaces.Rest.Controllers;

[Route("api/v1/audit-traceability")]
[Route("auditTraceability")]
public class AuditTraceabilityController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "auditTraceability";
    protected override bool ReturnSingleDocument => true;
}
