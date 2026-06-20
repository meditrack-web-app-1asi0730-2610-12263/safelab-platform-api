using System.Text.Json;
using System.Text.Json.Nodes;
using Microsoft.EntityFrameworkCore;
using SafeLab.Platform.Shared.Domain.Model.Aggregates;
using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Infrastructure.Persistence.EntityFrameworkCore.Configuration;

namespace SafeLab.Platform.Shared.Infrastructure.Persistence.EntityFrameworkCore.Repositories;

public class JsonDocumentRepository(AppDbContext context) : IJsonDocumentRepository
{
    public async Task<IReadOnlyList<JsonNode>> ListAsync(string collectionName, CancellationToken cancellationToken = default)
    {
        var records = await context.Documents
            .AsNoTracking()
            .Where(document => document.CollectionName == collectionName)
            .OrderBy(document => document.DocumentId)
            .ToListAsync(cancellationToken);

        return records.Select(record => JsonNode.Parse(record.Content)!).ToList();
    }

    public async Task<JsonNode?> FindByIdAsync(string collectionName, int documentId, CancellationToken cancellationToken = default)
    {
        var record = await context.Documents
            .AsNoTracking()
            .FirstOrDefaultAsync(document => document.CollectionName == collectionName && document.DocumentId == documentId, cancellationToken);

        return record is null ? null : JsonNode.Parse(record.Content);
    }

    public async Task<JsonNode> AddAsync(string collectionName, JsonNode document, CancellationToken cancellationToken = default)
    {
        var objectDocument = document.AsObject();
        var nextId = await GetNextIdAsync(collectionName, cancellationToken);

        if (!objectDocument.TryGetPropertyValue("id", out var idNode) || idNode is null)
            objectDocument["id"] = nextId;

        var documentId = objectDocument["id"]?.GetValue<int>() ?? nextId;

        var record = new StoredDocument
        {
            CollectionName = collectionName,
            DocumentId = documentId,
            Content = objectDocument.ToJsonString(new JsonSerializerOptions { WriteIndented = false }),
            CreatedAt = DateTimeOffset.UtcNow,
            UpdatedAt = DateTimeOffset.UtcNow
        };

        context.Documents.Add(record);
        await context.SaveChangesAsync(cancellationToken);

        return JsonNode.Parse(record.Content)!;
    }

    public async Task<JsonNode?> UpdateAsync(string collectionName, int documentId, JsonNode patch, CancellationToken cancellationToken = default)
    {
        var record = await context.Documents
            .FirstOrDefaultAsync(document => document.CollectionName == collectionName && document.DocumentId == documentId, cancellationToken);

        if (record is null) return null;

        var current = JsonNode.Parse(record.Content)!.AsObject();
        Merge(current, patch.AsObject());
        current["id"] = documentId;

        record.Content = current.ToJsonString(new JsonSerializerOptions { WriteIndented = false });
        record.UpdatedAt = DateTimeOffset.UtcNow;

        await context.SaveChangesAsync(cancellationToken);
        return JsonNode.Parse(record.Content)!;
    }

    public async Task<bool> DeleteAsync(string collectionName, int documentId, CancellationToken cancellationToken = default)
    {
        var record = await context.Documents
            .FirstOrDefaultAsync(document => document.CollectionName == collectionName && document.DocumentId == documentId, cancellationToken);

        if (record is null) return false;

        context.Documents.Remove(record);
        await context.SaveChangesAsync(cancellationToken);
        return true;
    }

    private async Task<int> GetNextIdAsync(string collectionName, CancellationToken cancellationToken)
    {
        var lastId = await context.Documents
            .Where(document => document.CollectionName == collectionName)
            .MaxAsync(document => (int?)document.DocumentId, cancellationToken);

        return (lastId ?? 0) + 1;
    }

    private static void Merge(JsonObject target, JsonObject patch)
    {
        foreach (var item in patch)
        {
            if (item.Key == "id") continue;

            if (target[item.Key] is JsonObject targetObject && item.Value is JsonObject patchObject)
            {
                Merge(targetObject, patchObject);
                continue;
            }

            target[item.Key] = item.Value is null ? null : JsonNode.Parse(item.Value.ToJsonString());
        }
    }
}
