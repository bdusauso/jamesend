/*
 * Copyright 2025 Bruno Dusausoy <bruno.dusausoy@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.jmsguisender;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class SSLContextHelper {
    
    public static SSLContext createSSLContext(ServerConfiguration config) throws Exception {
        if (!config.isUseSsl()) {
            return null;
        }
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        
        // Create key manager if keystore is provided
        KeyManager[] keyManagers = null;
        if (config.getKeyStorePath() != null && !config.getKeyStorePath().trim().isEmpty()) {
            keyManagers = createKeyManagers(
                config.getKeyStorePath(),
                config.getKeyStorePassword()
            );
        }
        
        // Create trust manager
        TrustManager[] trustManagers;
        if (config.isSkipCertificateValidation()) {
            trustManagers = createTrustAllTrustManagers();
        } else if (config.getTrustStorePath() != null && !config.getTrustStorePath().trim().isEmpty()) {
            trustManagers = createTrustManagers(
                config.getTrustStorePath(),
                config.getTrustStorePassword()
            );
        } else {
            // Use default trust managers (system trust store)
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            trustManagers = tmf.getTrustManagers();
        }
        
        sslContext.init(keyManagers, trustManagers, new SecureRandom());
        return sslContext;
    }
    
    private static KeyManager[] createKeyManagers(String keyStorePath, String keyStorePassword) throws Exception {
        KeyStore keyStore = loadKeyStore(keyStorePath, keyStorePassword);
        
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword != null ? keyStorePassword.toCharArray() : null);
        
        return kmf.getKeyManagers();
    }
    
    private static TrustManager[] createTrustManagers(String trustStorePath, String trustStorePassword) throws Exception {
        // Check if it's a PEM file
        if (PEMCertificateHelper.isPEMFile(trustStorePath)) {
            return PEMCertificateHelper.createTrustManagersFromPEM(trustStorePath);
        } else {
            // Handle as keystore (JKS, PKCS12, etc.)
            KeyStore trustStore = loadKeyStore(trustStorePath, trustStorePassword);
            
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            
            return tmf.getTrustManagers();
        }
    }
    
    private static TrustManager[] createTrustAllTrustManagers() {
        return new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    // Trust all certificates - USE ONLY FOR DEVELOPMENT
                }
                
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    // Trust all certificates - USE ONLY FOR DEVELOPMENT
                }
            }
        };
    }
    
    private static KeyStore loadKeyStore(String path, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(detectKeyStoreType(path));
        
        try (FileInputStream fis = new FileInputStream(path)) {
            keyStore.load(fis, password != null ? password.toCharArray() : null);
        } catch (IOException e) {
            throw new Exception("Failed to load keystore from " + path + ": " + e.getMessage(), e);
        }
        
        return keyStore;
    }
    
    private static String detectKeyStoreType(String path) {
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith(".p12") || lowerPath.endsWith(".pfx")) {
            return "PKCS12";
        } else if (lowerPath.endsWith(".jks")) {
            return "JKS";
        } else {
            // Default to JKS for unknown extensions
            return "JKS";
        }
    }
    
    public static HostnameVerifier createHostnameVerifier(boolean skipValidation) {
        if (skipValidation) {
            return (hostname, session) -> true; // Accept all hostnames - USE ONLY FOR DEVELOPMENT
        } else {
            return HttpsURLConnection.getDefaultHostnameVerifier();
        }
    }
}