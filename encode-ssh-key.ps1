# Handle with Care: Private keys are sensitive information. Ensure that the encoded key and the output file are handled securely.
# Access Permissions: Make sure you have the necessary permissions to read the key file and write to the current directory.

[CmdletBinding()]
param(
    [Parameter(Mandatory=$true, Position=0)]
    [string]$KeyPath
)

# Read the content of your key file as a single string
$keyContent = Get-Content -Path $KeyPath -Raw

# Convert the key content to bytes using UTF8 encoding
$keyBytes = [System.Text.Encoding]::UTF8.GetBytes($keyContent)

# Encode the bytes to a base64 string
$base64Key = [System.Convert]::ToBase64String($keyBytes)

# Output the base64 string to a file in the current directory
$outputFilePath = Join-Path -Path (Get-Location) -ChildPath "encoded_key.txt"
$base64Key | Out-File -FilePath $outputFilePath -Encoding ascii

# Optionally, copy the base64 string to clipboard for easy pasting
$base64Key | Set-Clipboard

Write-Host "Base64 encoded key has been written to 'encoded_key.txt' in the current directory and copied to the clipboard."