mvn clean install

Get-Content .env | ForEach-Object {
    if ($_ -match "^\s*#") { return }
    if ($_ -match "^\s*$") { return }

    $parts = $_ -split "=", 2
    $name  = $parts[0].Trim()
    $value = $parts[1].Trim()

    Set-Item -Path "Env:$name" -Value $value
}

Write-Host "Env vars loaded from .env"

cd api
mvn spring-boot:run
