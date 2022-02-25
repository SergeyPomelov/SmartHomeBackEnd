package org.lessnik.smarthome.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.StringJoiner;

import static org.lessnik.smarthome.model.ClientBuilder.getClient;
import static org.lessnik.smarthome.service.SecretsHolder.ADDRESS;
import static org.lessnik.smarthome.service.SecretsHolder.CODE;


@Slf4j
@RequiredArgsConstructor
@ParametersAreNonnullByDefault
public final class InternetSpeedProbeTask implements IAsyncTask {

    private static final HttpClient client = getClient();

    public final long domoticzIndex;
    public final int timeout;

    private final Optional<Boolean> alive = Optional.empty();
    private boolean lastFailed = false;
    private final long lastRun = System.currentTimeMillis();
    private long lastResponseTime;

    public void wake() {
        if (canRun()) {
            run();
        }
    }

    private boolean canRun() {
        return (mls() - lastRun) > lastResponseTime * 2;
    }

    private void run() {

        try {
            var start = mls();
            final Long ping = pingHost("google.com");
            if (ping != null) {
                log.info("Ping: " + ping + " ms");
                notifyExternalServices(ping, 65L);
            }
            measureDownload();
            measureUpload();
            lastResponseTime = mls() - start;
        } catch (IOException e) {
            log.error("Internet speed measuring failed", e);
        }
    }

    private void measureDownload() throws IOException {
        long totalDownload = 0; // total bytes downloaded
        final int BUFFER_SIZE = 1024; // size of the buffer
        byte[] data = new byte[BUFFER_SIZE]; // buffer
        int dataRead = 0; // data read in each try
        long startTime = System.nanoTime(); // starting time of download
        BufferedInputStream in = new BufferedInputStream(
                new URL("http://ipv4.ikoula.testdebit.info/100M.iso").openStream());
        while ((dataRead = in.read(data, 0, 1024)) > 0) {
            totalDownload += dataRead; // adding data downloaded to total data
        }
        double downloadTime = (System.nanoTime() - startTime);
        double bytesPerSec = totalDownload / ((downloadTime) / 1000000000);
        double kbPerSec = bytesPerSec / (1024);
        double mbPerSec = kbPerSec / (1024);
        in.close();
        notifyExternalServices(mbPerSec, domoticzIndex);
        log.info("Download: " + mbPerSec + " MBps");
    }

    private void measureUpload() throws IOException {
        final int BUFFER_SIZE = 1024;
        byte[] data = new byte[BUFFER_SIZE];
        new Random().nextBytes(data);
        int totalUpload = 0;
        long startTime = System.nanoTime();
        var connection = new URL("http://ipv4.ikoula.testdebit.info").openConnection();
        connection.setDoOutput(true);
        BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
        for (int batch = 1; batch <= 100; batch++) {
            out.write(data, 0, BUFFER_SIZE);
            totalUpload = BUFFER_SIZE * batch;
        }
        double uploadTime = (System.nanoTime() - startTime);
        double bytesPerSec = totalUpload / ((uploadTime) / 1000000000);
        double kbPerSec = bytesPerSec / (1024);
        double mbPerSec = kbPerSec / (1024);
        out.close();
        notifyExternalServices(mbPerSec, 66);
        log.info("Upload: " + mbPerSec + " MBps");
    }

    private void notifyExternalServices(double speed, long idx) {
        var request = HttpRequest.newBuilder().uri(uri(speed, idx)).build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            lastFailed = false;
            if (response.statusCode() != HttpStatus.OK.value()) {
                lastFailed = true;
                log.warn("{}: {}", this, response.body());
            }
        } catch (IOException | InterruptedException e) {
            lastFailed = true;
            log.warn("{}: {}", this, e);
        }
    }

    @Nullable
    public Long pingHost(String host) throws IOException {
        final long start = mls();
        if (!InetAddress.getByName(host).isReachable(2000)) return null;
        return mls() - start;
    }

    private long mls() {
        return System.currentTimeMillis();
    }

    private URI uri(double responseTime, long idx) {
        return URI.create(String.format("%s/json.htm?type=command&param=udevice&idx=%s" +
                "&nvalue=%s&svalue=%s&passcode=%s", ADDRESS, idx, responseTime, responseTime, CODE));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InternetSpeedProbeTask.class.getSimpleName() + "[", "]")
                .add("alive=" + alive)
                .add("lastRun=" + Instant.ofEpochMilli(lastRun))
                .add("lastResponseTime=" + lastResponseTime + "ms")
                .add("lastFailed=" + lastFailed)
                .toString();
    }
}
