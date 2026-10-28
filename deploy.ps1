# Build DevFlow and deploy it to Tomcat 10.1 (the manual WAR method).
# Run from the project root in the VS Code terminal:   .\deploy.ps1
# If PowerShell blocks scripts, run once:  Set-ExecutionPolicy -Scope Process Bypass
$Tomcat = "C:\tomcat10"      # change this if you extracted Tomcat somewhere else

mvn clean package
if ($LASTEXITCODE -ne 0) { Write-Host "Build failed. Fix the errors above." -ForegroundColor Red; exit 1 }

Write-Host "Stopping Tomcat..."
& "$Tomcat\bin\shutdown.bat" 2>$null
Start-Sleep -Seconds 4

Remove-Item "$Tomcat\webapps\devflow.war" -ErrorAction SilentlyContinue
Remove-Item "$Tomcat\webapps\devflow" -Recurse -Force -ErrorAction SilentlyContinue
Copy-Item target\devflow.war "$Tomcat\webapps\"

Write-Host "Starting Tomcat..."
& "$Tomcat\bin\startup.bat"
Write-Host "Open http://localhost:8080/devflow/ in about 10 seconds." -ForegroundColor Green
