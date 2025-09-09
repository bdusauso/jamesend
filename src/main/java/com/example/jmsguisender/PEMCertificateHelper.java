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

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PEMCertificateHelper {
    
    private static final Pattern CERT_PATTERN = Pattern.compile(
        "-----BEGIN CERTIFICATE-----\\s*([\\s\\S]*?)\\s*-----END CERTIFICATE-----"
    );
    
    public static boolean isPEMFile(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        
        String lowerPath = path.toLowerCase();
        return lowerPath.endsWith(".pem") || lowerPath.endsWith(".crt") || 
               lowerPath.endsWith(".cer") || lowerPath.endsWith(".cert");
    }
    
    public static TrustManager[] createTrustManagersFromPEM(String pemFilePath) throws Exception {
        List<X509Certificate> certificates = loadCertificatesFromPEM(pemFilePath);
        
        if (certificates.isEmpty()) {
            throw new Exception("No certificates found in PEM file: " + pemFilePath);
        }
        
        // Create a temporary keystore to hold the certificates
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        
        // Add all certificates to the trust store
        for (int i = 0; i < certificates.size(); i++) {
            X509Certificate cert = certificates.get(i);
            String alias = "cert-" + i;
            trustStore.setCertificateEntry(alias, cert);
        }
        
        // Create trust managers from the keystore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        
        return tmf.getTrustManagers();
    }
    
    public static List<X509Certificate> loadCertificatesFromPEM(String pemFilePath) throws Exception {
        List<X509Certificate> certificates = new ArrayList<>();
        
        try {
            String pemContent = Files.readString(Paths.get(pemFilePath));
            certificates.addAll(parsePEMCertificates(pemContent));
        } catch (IOException e) {
            throw new Exception("Failed to read PEM file: " + pemFilePath, e);
        }
        
        return certificates;
    }
    
    public static List<X509Certificate> parsePEMCertificates(String pemContent) throws Exception {
        List<X509Certificate> certificates = new ArrayList<>();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        
        Matcher matcher = CERT_PATTERN.matcher(pemContent);
        
        while (matcher.find()) {
            String base64Cert = matcher.group(1).replaceAll("\\s", "");
            
            try {
                byte[] certBytes = Base64.getDecoder().decode(base64Cert);
                Certificate cert = cf.generateCertificate(new ByteArrayInputStream(certBytes));
                
                if (cert instanceof X509Certificate) {
                    certificates.add((X509Certificate) cert);
                }
            } catch (Exception e) {
                throw new Exception("Failed to parse certificate in PEM content", e);
            }
        }
        
        return certificates;
    }
    
    public static String getCertificateInfo(X509Certificate cert) {
        StringBuilder info = new StringBuilder();
        info.append("Subject: ").append(cert.getSubjectX500Principal().getName()).append("\n");
        info.append("Issuer: ").append(cert.getIssuerX500Principal().getName()).append("\n");
        info.append("Valid From: ").append(cert.getNotBefore()).append("\n");
        info.append("Valid Until: ").append(cert.getNotAfter()).append("\n");
        info.append("Serial Number: ").append(cert.getSerialNumber().toString(16).toUpperCase());
        return info.toString();
    }
    
    public static void validatePEMFile(String pemFilePath) throws Exception {
        if (!Files.exists(Paths.get(pemFilePath))) {
            throw new Exception("PEM file does not exist: " + pemFilePath);
        }
        
        List<X509Certificate> certificates = loadCertificatesFromPEM(pemFilePath);
        
        if (certificates.isEmpty()) {
            throw new Exception("No valid certificates found in PEM file: " + pemFilePath);
        }
        
        // Check if any certificates are expired
        java.util.Date now = new java.util.Date();
        for (X509Certificate cert : certificates) {
            if (cert.getNotAfter().before(now)) {
                System.err.println("Warning: Certificate expired: " + cert.getSubjectX500Principal().getName());
            }
        }
    }
}