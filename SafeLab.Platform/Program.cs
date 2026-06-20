using Microsoft.EntityFrameworkCore;
using Microsoft.OpenApi;
using SafeLab.Platform.Iam.Application.CommandServices;
using SafeLab.Platform.Iam.Application.Internal.CommandServices;
using SafeLab.Platform.Shared.Domain.Repositories;
using SafeLab.Platform.Shared.Infrastructure.Persistence.EntityFrameworkCore.Configuration;
using SafeLab.Platform.Shared.Infrastructure.Persistence.EntityFrameworkCore.Extensions;
using SafeLab.Platform.Shared.Infrastructure.Persistence.EntityFrameworkCore.Repositories;

var builder = WebApplication.CreateBuilder(args);

var port = Environment.GetEnvironmentVariable("PORT");
if (!string.IsNullOrWhiteSpace(port))
{
    builder.WebHost.UseUrls($"http://0.0.0.0:{port}");
}
else
{
    builder.WebHost.UseUrls("http://0.0.0.0:8080");
}

builder.Services.AddRouting(options => options.LowercaseUrls = true);
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();

builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowSafeLabClients", policy =>
        policy.AllowAnyOrigin()
            .AllowAnyMethod()
            .AllowAnyHeader());
});

builder.Services.AddDbContext<AppDbContext>((serviceProvider, options) =>
{
    var connectionString = Environment.GetEnvironmentVariable("MYSQL_CONNECTION_STRING")
                           ?? builder.Configuration.GetConnectionString("DefaultConnection");

    if (string.IsNullOrWhiteSpace(connectionString))
        throw new InvalidOperationException("MySQL connection string is not configured.");

    options.UseMySQL(connectionString)
        .UseLoggerFactory(serviceProvider.GetRequiredService<ILoggerFactory>())
        .EnableDetailedErrors();

    if (builder.Environment.IsDevelopment())
        options.EnableSensitiveDataLogging();
});

builder.Services.AddScoped<IJsonDocumentRepository, JsonDocumentRepository>();
builder.Services.AddScoped<IAuthenticationCommandService, AuthenticationCommandService>();

builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "SafeLab Platform API",
        Version = "v1",
        Description = "RESTful API for SafeLab laboratory monitoring, identity access, assets, sensors, alerts, incidents, reports and traceability.",
        Contact = new OpenApiContact
        {
            Name = "SafeLab Team",
            Email = "contact@safelab.local"
        },
        License = new OpenApiLicense
        {
            Name = "MIT",
            Url = new Uri("https://opensource.org/licenses/MIT")
        }
    });
});

var app = builder.Build();

using (var scope = app.Services.CreateScope())
{
    var context = scope.ServiceProvider.GetRequiredService<AppDbContext>();
    var logger = scope.ServiceProvider.GetRequiredService<ILoggerFactory>().CreateLogger("SafeLab.DatabaseInitializer");
    await DatabaseInitializer.InitializeAsync(context, app.Environment, logger);
}

app.UseSwagger();
app.UseSwaggerUI(options =>
{
    options.SwaggerEndpoint("/swagger/v1/swagger.json", "SafeLab Platform API v1");
    options.DocumentTitle = "SafeLab Platform API";
});

app.UseCors("AllowSafeLabClients");
app.UseRouting();
app.UseAuthorization();

app.MapControllers();
app.MapGet("/health", () => Results.Ok(new
{
    status = "Healthy",
    service = "SafeLab Platform API",
    timestamp = DateTimeOffset.UtcNow
}));

app.Run();
