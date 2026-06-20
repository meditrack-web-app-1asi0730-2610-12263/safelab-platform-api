using System.Text.Json.Nodes;

namespace SafeLab.Platform.Shared.Domain.Repositories;

public interface IJsonDocumentRepository
{
    Task<IReadOnlyList<JsonNode>> ListAsync(string collectionName, CancellationToken cancellationToken = default);
    Task<JsonNode?> FindByIdAsync(string collectionName, int documentId, CancellationToken cancellationToken = default);
    Task<JsonNode> AddAsync(string collectionName, JsonNode document, CancellationToken cancellationToken = default);
    Task<JsonNode?> UpdateAsync(string collectionName, int documentId, JsonNode patch, CancellationToken cancellationToken = default);
    Task<bool> DeleteAsync(string collectionName, int documentId, CancellationToken cancellationToken = default);
}
