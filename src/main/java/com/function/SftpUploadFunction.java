package com.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.*;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

/**
 * Azure Function that demonstrates how to use Azure Key Vault secrets
 * to authenticate with an SFTP server using a private SSH key.
 */
public class SftpUploadFunction {

    // Configuration values (Replace these with your actual values or parameterize as needed)
    // Hardcoded configuration values for proof of concept
    private static final String KEY_VAULT_URL = "https://your-key-vault-name.vault.azure.net/";
    private static final String SSH_KEY_SECRET_NAME = "your-ssh-key-secret-name";
    private static final String SFTP_USERNAME = "your-sftp-username";
    private static final String SFTP_HOST = "your-sftp-host";
    private static final String LOCAL_FILE_PATH = "C:\\path\\to\\your\\local\\file.txt";
    private static final String REMOTE_FILE_PATH = "/remote/path/file.txt";

    /**
     * This function is triggered by an HTTP request.
     * It uploads a local file to an SFTP server using credentials stored in Azure Key Vault.
     */
    @FunctionName("SftpUploadFunction")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Starting SFTP upload function.");

        // Set up the Azure Key Vault SecretClient
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl(KEY_VAULT_URL)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Retrieve the base64-encoded SSH private key from Azure Key Vault
        KeyVaultSecret secret = secretClient.getSecret(SSH_KEY_SECRET_NAME);
        String encodedPrivateKey = secret.getValue();

        // Log retrieval success (Do not log sensitive information!)
        context.getLogger().info("Retrieved SSH private key from Azure Key Vault.");

        try {
            // Initialize JSch and configure the SSH session
            JSch jsch = new JSch();

            // Decode the base64-encoded private key
            byte[] privateKeyBytes = Base64.getDecoder().decode(encodedPrivateKey);

            // Add the private key identity to JSch (assuming no passphrase; add one if needed)
            jsch.addIdentity("sftp-private-key", privateKeyBytes, null, null);

            // Create and configure the SSH session
            Session session = jsch.getSession(SFTP_USERNAME, SFTP_HOST, 22);

            // Set session configuration properties
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no"); // Disable host key checking for demo purposes
            session.setConfig(config);

            // Connect to the SFTP server
            session.connect();
            context.getLogger().info("SSH session connected.");

            // Open the SFTP channel
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            context.getLogger().info("SFTP channel connected.");

            // Upload the local file to the remote SFTP server
            try (FileInputStream fis = new FileInputStream(new File(LOCAL_FILE_PATH))) {
                sftpChannel.put(fis, REMOTE_FILE_PATH);
                context.getLogger().info("File uploaded successfully to the SFTP server.");
            }

            // Disconnect the SFTP channel and session
            sftpChannel.disconnect();
            session.disconnect();
            context.getLogger().info("SFTP session disconnected.");

            // Return a successful HTTP response
            return request.createResponseBuilder(HttpStatus.OK)
                .body("File uploaded successfully.")
                .build();

        } catch (Exception e) {
            // Log the exception and return an error response
            context.getLogger().severe("SFTP upload failed: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("File upload failed: " + e.getMessage())
                .build();
        }
    }
}
