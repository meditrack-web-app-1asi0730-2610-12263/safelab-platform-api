using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.RemoteControlActuation.Interfaces.Rest.Controllers;

[Route("api/v1/remote-actuators")]
[Route("remoteActuators")]
public class RemoteActuatorsController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "remoteActuators";
    protected override bool ReturnSingleDocument => false;
}
