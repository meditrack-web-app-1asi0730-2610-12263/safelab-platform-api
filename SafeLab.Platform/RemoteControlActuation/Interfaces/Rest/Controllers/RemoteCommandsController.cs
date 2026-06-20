using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.RemoteControlActuation.Interfaces.Rest.Controllers;

[Route("api/v1/remote-commands")]
[Route("remoteCommands")]
public class RemoteCommandsController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "remoteCommands";
    protected override bool ReturnSingleDocument => false;
}
