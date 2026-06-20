workspace "SafeLab Platform" "C4 model for SafeLab backend v1" {
    model {
        user = person "SafeLab User"
        frontend = softwareSystem "SafeLab Web Application" "Vue frontend deployed on Vercel"
        backend = softwareSystem "SafeLab Platform API" "ASP.NET Core RESTful API"
        database = softwareSystem "MySQL Database" "Persistent storage"
        user -> frontend "Uses"
        frontend -> backend "Consumes REST API / Swagger documented endpoints"
        backend -> database "Reads and writes data using Entity Framework Core"
    }
    views {
        systemContext backend "SafeLab-Context" {
            include *
            autolayout lr
        }
    }
}
