namespace SafeLab.Platform.Shared.Domain.Model.Aggregates;

public class StoredDocument
{
    public int Id { get; set; }
    public string CollectionName { get; set; } = string.Empty;
    public int DocumentId { get; set; }
    public string Content { get; set; } = "{}";
    public DateTimeOffset CreatedAt { get; set; } = DateTimeOffset.UtcNow;
    public DateTimeOffset UpdatedAt { get; set; } = DateTimeOffset.UtcNow;
}
