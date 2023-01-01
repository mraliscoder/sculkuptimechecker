package org.cowr.sculkbot.uptime.ssl;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

public class CompositeX509TrustManager implements X509TrustManager {
    private final List<X509TrustManager> trustManagers;

    public CompositeX509TrustManager(X509TrustManager... trustManagers) {
        this.trustManagers = Arrays.asList(trustManagers);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        new MultiException<>(new CertificateException("This certificate chain couldn't be trusted"))
                .collectFrom(trustManagers.stream(),
                        trustManagers -> trustManagers.checkClientTrusted(x509Certificates, s))
                .scream(MultiException.Mode.UNLESS_ANY_SUCCESS);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        new MultiException<>(new CertificateException("This certificate chain couldn't be trusted"))
                .collectFrom(trustManagers.stream(),
                        trustManagers -> trustManagers.checkClientTrusted(x509Certificates, s))
                .scream(MultiException.Mode.UNLESS_ANY_SUCCESS);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return trustManagers.stream()
                .map(X509TrustManager::getAcceptedIssuers)
                .flatMap(Arrays::stream)
                .toArray(X509Certificate[]::new);
    }
}
