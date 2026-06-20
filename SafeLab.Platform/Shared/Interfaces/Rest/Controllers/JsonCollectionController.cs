using System.Text.Json.Nodes;
using Microsoft.AspNetCore.Mvc;
using SafeLab.Platform.Shared.Domain.Repositories;

namespace SafeLab.Platform.Shared.Interfaces.Rest.Controllers;

[ApiController]
public abstract class JsonCollectionController(IJsonDocumentRepository repository) : ControllerBase
{
    protected abstract string CollectionName { get; }
    protected virtual bool ReturnSingleDocument => false;

    [HttpGet]
    public async Task<IActionResult> GetAll(CancellationToken cancellationToken)
    {
        var documents = await repository.ListAsync(CollectionName, cancellationToken);
        return ReturnSingleDocument ? Ok(documents.FirstOrDefault()) : Ok(documents);
    }

    [HttpGet("{id:int}")]
    public async Task<IActionResult> GetById(int id, CancellationToken cancellationToken)
    {
        var document = await repository.FindByIdAsync(CollectionName, id, cancellationToken);
        return document is null ? NotFound() : Ok(document);
    }

    [HttpPost]
    public async Task<IActionResult> Create([FromBody] JsonObject resource, CancellationToken cancellationToken)
    {
        var created = await repository.AddAsync(CollectionName, resource, cancellationToken);
        var id = created?["id"]?.GetValue<int>() ?? 0;
        return CreatedAtAction(nameof(GetById), new { id }, created);
    }

    [HttpPut("{id:int}")]
    public async Task<IActionResult> Replace(int id, [FromBody] JsonObject resource, CancellationToken cancellationToken)
    {
        resource["id"] = id;
        var updated = await repository.UpdateAsync(CollectionName, id, resource, cancellationToken);
        return updated is null ? NotFound() : Ok(updated);
    }

    [HttpPatch("{id:int}")]
    public async Task<IActionResult> Patch(int id, [FromBody] JsonObject resource, CancellationToken cancellationToken)
    {
        var updated = await repository.UpdateAsync(CollectionName, id, resource, cancellationToken);
        return updated is null ? NotFound() : Ok(updated);
    }

    [HttpDelete("{id:int}")]
    public async Task<IActionResult> Delete(int id, CancellationToken cancellationToken)
    {
        var deleted = await repository.DeleteAsync(CollectionName, id, cancellationToken);
        return deleted ? NoContent() : NotFound();
    }
}
