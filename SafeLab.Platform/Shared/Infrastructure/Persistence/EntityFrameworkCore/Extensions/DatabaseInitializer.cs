using System.Text.Json;
using System.Text.Json.Nodes;
using Microsoft.AspNetCore.Hosting;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using SafeLab.Platform.Shared.Domain.Model.Aggregates;
using SafeLab.Platform.Shared.Infrastructure.Persistence.EntityFrameworkCore.Configuration;

namespace SafeLab.Platform.Shared.Infrastructure.Persistence.EntityFrameworkCore.Extensions;

public static class DatabaseInitializer
{
    public static async Task InitializeAsync(AppDbContext context, IWebHostEnvironment environment, ILogger logger)
    {
        await context.Database.EnsureCreatedAsync();

        if (await context.Documents.AnyAsync())
        {
            logger.LogInformation("SafeLab database already contains data. Skipping seed process.");
            return;
        }

        var seedPath = Path.Combine(environment.ContentRootPath, "Data", "seed-data.json");
        if (!File.Exists(seedPath))
        {
            logger.LogWarning("Seed file was not found at {SeedPath}.", seedPath);
            return;
        }

        var json = await File.ReadAllTextAsync(seedPath);
        var root = JsonNode.Parse(json)?.AsObject();
        if (root is null) return;

        var records = new List<StoredDocument>();

        foreach (var collection in root)
        {
            if (collection.Value is JsonArray array)
            {
                var generatedId = 1;
                foreach (var item in array)
                {
                    if (item is null) continue;
                    var documentId = ExtractDocumentId(item) ?? generatedId++;
                    EnsureId(item, documentId);
                    records.Add(CreateRecord(collection.Key, documentId, item));
                }
                continue;
            }

            if (collection.Value is JsonObject obj)
            {
                EnsureId(obj, 1);
                records.Add(CreateRecord(collection.Key, 1, obj));
            }
        }

        context.Documents.AddRange(records);
        await context.SaveChangesAsync();
        logger.LogInformation("Seeded {Count} SafeLab records into MySQL.", records.Count);
    }

    private static StoredDocument CreateRecord(string collectionName, int documentId, JsonNode item)
    {
        return new StoredDocument
        {
            CollectionName = collectionName,
            DocumentId = documentId,
            Content = item.ToJsonString(new JsonSerializerOptions { WriteIndented = false }),
            CreatedAt = DateTimeOffset.UtcNow,
            UpdatedAt = DateTimeOffset.UtcNow
        };
    }

    private static int? ExtractDocumentId(JsonNode node)
    {
        if (node is not JsonObject obj) return null;
        if (!obj.TryGetPropertyValue("id", out var idNode) || idNode is null) return null;

        if (idNode is JsonValue value && value.TryGetValue<int>(out var id))
            return id;

        return null;
    }

    private static void EnsureId(JsonNode node, int id)
    {
        if (node is not JsonObject obj) return;
        if (!obj.ContainsKey("id")) obj["id"] = id;
    }
}
