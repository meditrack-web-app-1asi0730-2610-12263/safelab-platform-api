using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

namespace SafeLab.Platform.RemoteControlActuation.Interfaces.Rest.Controllers;

[Route("api/v1/device-control-policies")]
[Route("deviceControlPolicies")]
public class DeviceControlPoliciesController(IJsonDocumentRepository repository) : JsonCollectionController(repository)
{
    protected override string CollectionName => "deviceControlPolicies";
    protected override bool ReturnSingleDocument => false;
}
