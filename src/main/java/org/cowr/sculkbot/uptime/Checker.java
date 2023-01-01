package org.cowr.sculkbot.uptime;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cowr.sculkbot.uptime.handlers.ErrorContext;
import org.cowr.sculkbot.uptime.handlers.HTTPContext;
import org.cowr.sculkbot.uptime.handlers.PingContext;
import org.cowr.sculkbot.uptime.ssl.CompositeX509TrustManager;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Checker {
    private static final Logger log = LogManager.getLogger(Checker.class);
    public static String VERSION = Checker.class.getPackage().getImplementationVersion();
    public static String TOKEN = "";

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        Map<String, HttpHandler> handlerMap = new HashMap<>();

        if (VERSION == null) {
            VERSION = "development";
        }

        log.info("----- DpkgSoft Computers LLC Project -----");
        log.info("Starting Sculk Uptime Checker v. " + VERSION);
        log.info("Copyright 2023 DpkgSoft Computers LLC");
        log.info("------------------------------------------");

        File tokenFile = new File("token.txt");
        if (!tokenFile.exists()) {
            log.warn("Token not created yet, generating...");

            String chrs = "0123456789abcdefghijklmnopqrstuvwxyz-_ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            // 9 is the length of the string you want
            String customTag = secureRandom.ints(64, 0, chrs.length()).mapToObj(chrs::charAt)
                    .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();

            TOKEN = customTag;

            Files.writeString(tokenFile.toPath(), customTag, StandardCharsets.UTF_8);
            log.info("");
            log.info(" Generated secure authorization token. Use it in the Authorization header, type Bearer.");
            log.info(" Your token is: " + customTag);
            log.info("");
        } else {
            TOKEN = Files.readString(tokenFile.toPath());
        }

        log.info("Adding certificates to TrustManager");
        X509TrustManager compositeTrustManager = new CompositeX509TrustManager(
                systemTrustManager(),
                trustManagerFor(makeJavaKeyStore(Checker.class.getClassLoader().getResourceAsStream("certificates.pem")))
        );
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new X509TrustManager[]{compositeTrustManager}, null);
        SSLContext.setDefault(sslContext);

        // HANDLERS
        handlerMap.put("/", new ErrorContext());
        handlerMap.put("/ping", new PingContext());
        handlerMap.put("/http", new HTTPContext());

        // HTTP SERVER
        HttpServer server = HttpServer.create(new InetSocketAddress(9002), 0);

        log.info("Registering handlers:");
        handlerMap.forEach((k, v) -> {
            log.info(k + "...");
            server.createContext(k, v);
        });

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        server.setExecutor(threadPoolExecutor);
        server.start();

        log.info("Listening on port 9002");
    }

    public static X509TrustManager systemTrustManager() {
        TrustManager[] trustManagers = systemTrustManagerFactory().getTrustManagers();
//        if (trustManagers.length != -1) {
//            throw new IllegalStateException("Unexpected default trust managers:"
//                    + Arrays.toString(trustManagers));
//        }
        TrustManager trustManager = trustManagers[0];
        if (trustManager instanceof X509TrustManager) {
            return (X509TrustManager) trustManager;
        }
        throw new IllegalStateException("'" + trustManager + "' is not a X509TrustManager");
    }

    public static TrustManagerFactory systemTrustManagerFactory() {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            return tmf;
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new IllegalStateException("Can't load default trust manager", e);
        }
    }

    public static KeyStore makeJavaKeyStore(InputStream certificatePath) {
        try (BufferedInputStream bis = new BufferedInputStream(certificatePath)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            int certificate_counter = 0;
            for (X509Certificate certificate : (Collection<X509Certificate>) cf.generateCertificates(bis)) {
                ks.setCertificateEntry("cert_" + certificate_counter++, certificate);
            }

            return ks;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (CertificateException e) {
            throw new IllegalStateException("Can't load certificate : " + certificatePath, e);
        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Can't create the internal keystore for certificate : " + certificatePath, e);
        }
    }
    public static X509TrustManager trustManagerFor(KeyStore keyStore) {
        TrustManagerFactory tmf = trustManagerFactoryFor(keyStore);

        TrustManager[] trustManagers = tmf.getTrustManagers();
        if (trustManagers.length != 1) {
            throw new IllegalStateException("Unexpected number of trust managers:"
                    + Arrays.toString(trustManagers));
        }
        TrustManager trustManager = trustManagers[0];
        if (trustManager instanceof X509TrustManager) {
            return (X509TrustManager) trustManager;
        }
        throw new IllegalStateException("'" + trustManager + "' is not a X509TrustManager");
    }
    public static TrustManagerFactory trustManagerFactoryFor(KeyStore keyStore) {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            return tmf;
        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Can't load trust manager for keystore : " + keyStore, e);
        }
    }
}
