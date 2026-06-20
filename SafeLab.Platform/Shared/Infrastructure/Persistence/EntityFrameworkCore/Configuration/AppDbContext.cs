using Microsoft.EntityFrameworkCore;
using SafeLab.Platform.Shared.Domain.Model.Aggregates;

namespace SafeLab.Platform.Shared.Infrastructure.Persistence.EntityFrameworkCore.Configuration;

public class AppDbContext(DbContextOptions<AppDbContext> options) : DbContext(options)
{
    public DbSet<StoredDocument> Documents => Set<StoredDocument>();

    protected override void OnModelCreating(ModelBuilder builder)
    {
        base.OnModelCreating(builder);

        builder.Entity<StoredDocument>(entity =>
        {
            entity.ToTable("stored_documents");
            entity.HasKey(document => document.Id);
            entity.Property(document => document.Id).ValueGeneratedOnAdd();
            entity.Property(document => document.CollectionName).HasColumnName("collection_name").HasMaxLength(120).IsRequired();
            entity.Property(document => document.DocumentId).HasColumnName("document_id").IsRequired();
            entity.Property(document => document.Content).HasColumnName("content").HasColumnType("longtext").IsRequired();
            entity.Property(document => document.CreatedAt).HasColumnName("created_at").IsRequired();
            entity.Property(document => document.UpdatedAt).HasColumnName("updated_at").IsRequired();
            entity.HasIndex(document => new { document.CollectionName, document.DocumentId }).IsUnique();
        });
    }
}
