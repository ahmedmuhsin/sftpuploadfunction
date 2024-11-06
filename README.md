# Azure Function App: SFTP Upload Using Azure Key Vault Secrets

This demo showcases an Azure Function App that uploads a file to an SFTP server using an SSH private key securely stored in Azure Key Vault. The application uses the JSch library for SFTP operations in Java.

## Overview

**Key Components**:

- **Azure Function App**: Hosts the Java function triggered via HTTP.
- **Azure Key Vault**: Stores the base64-encoded SSH private key.
- **JSch Library**: Handles the SFTP connection and file upload.

**Assumptions**:

- An Azure Key Vault contains your base64-encoded SSH private key stored as a secret.
- The Function App has permission to access the Key Vault (e.g., via Managed Identity).
- Necessary configurations (SFTP host, username, file paths) are provided in the code.

## Included Files

- `Function.java`: The main Azure Function code.
- `encode-ssh-key.ps1`: PowerShell script to base64-encode your SSH private key.
- `README.md`: This readme file.

## Configuration

Update the following variables in `SftpUploadFunction.java` with your actual details:

```java
// Configuration values
private static final String KEY_VAULT_URL = "https://your-key-vault-name.vault.azure.net/";
private static final String SSH_KEY_SECRET_NAME = "your-ssh-key-secret-name";
private static final String SFTP_USERNAME = "your-sftp-username";
private static final String SFTP_HOST = "your-sftp-host";
private static final String LOCAL_FILE_PATH = "C:\\path\\to\\your\\local\\file.txt";
private static final String REMOTE_FILE_PATH = "/remote/path/file.txt";
```

- **KEY_VAULT_URL**: The URL of your Azure Key Vault.
- **SSH_KEY_SECRET_NAME**: The name of the secret storing your SSH private key.
- **SFTP_USERNAME**: Your username for the SFTP server.
- **SFTP_HOST**: The hostname or IP address of the SFTP server.
- **LOCAL_FILE_PATH**: Path to the local file you wish to upload.
- **REMOTE_FILE_PATH**: Destination path on the SFTP server.

## Usage

1. **Base64-Encode Your SSH Private Key**:

   Use the included `encode-ssh-key.ps1` script to encode your SSH private key:

   ```powershell
   # Run the script in PowerShell
   .\encode-ssh-key.ps1 -KeyPath "C:\path\to\your\key.pem"
   ```

   This script reads your PEM-formatted SSH private key, base64-encodes it, and outputs the result. The encoded key is ready to be stored in Azure Key Vault.

2. **Store the Encoded Key in Azure Key Vault**:

   Save the base64-encoded SSH private key as a secret in your Azure Key Vault. Ensure the secret name matches `SSH_KEY_SECRET_NAME` in the configuration.

3. **Deploy the Function App**:

   Build and deploy the function using your preferred method (e.g., Azure Functions Core Tools or Azure Portal).

4. **Trigger the Function**:

   Send an HTTP GET or POST request to the function endpoint:

   ```bash
   curl https://your-function-app.azurewebsites.net/api/SftpUploadFunction
   ```

   The function will upload the specified local file to the SFTP server using the SSH private key retrieved from Azure Key Vault.

## Notes

- **Security**:
  - Avoid logging sensitive information like private keys or passphrases.
  - Ensure that access to Azure Key Vault and its secrets is appropriately restricted.
  - For production environments, enable strict host key checking by setting `StrictHostKeyChecking` to `"yes"` and managing known hosts.

- **Dependencies**:
  - **Maven**: Used for project management and building.
  - **Libraries**:
    - `azure-security-keyvault-secrets`: Azure SDK for Key Vault integration.
    - `jsch`: Library for SSH and SFTP operations.

- **Assumptions**:
  - The Function App has network access to the SFTP server.
  - The necessary permissions are in place for the Function App to access Azure Key Vault.

## Conclusion

This demo provides an example of how to securely use an SSH private key from Azure Key Vault within an Azure Function App to perform SFTP file uploads. Customize the function and configuration to suit your specific requirements.

---

Feel free to adjust the function and configurations as needed for your environment.
