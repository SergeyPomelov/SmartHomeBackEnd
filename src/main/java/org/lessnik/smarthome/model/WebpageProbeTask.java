package org.lessnik.smarthome.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.StringJoiner;

import static org.lessnik.smarthome.model.ClientBuilder.getClient;
import static org.lessnik.smarthome.service.SecretsHolder.ADDRESS;
import static org.lessnik.smarthome.service.SecretsHolder.CODE;


@Slf4j
@RequiredArgsConstructor
public final class WebpageProbeTask implements IAsyncTask {

    private static final HttpClient client = getClient();

    public final long domoticzIndex;
    public final int timeout;

    private Optional<Boolean> alive = Optional.empty();
    private boolean lastFailed = false;
    private long lastRun = System.currentTimeMillis();
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
        var newIsAlive = responseCheck();
        log.info("{}: {}", this, newIsAlive);
        alive = newIsAlive;
        notifyExternalServices(lastResponseTime);
        lastRun = mls();
    }

    public Optional<Boolean> isAlive() throws IOException {

        var request = HttpRequest.newBuilder()
                .uri(URI.create(("https://alwind.net/ui/1")))
                .timeout(Duration.ofMillis(timeout))
                .build();

        HttpResponse<String> response;
        try {
            var start = mls();
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            lastResponseTime = mls() - start;
            return Optional.of(response.statusCode() == HttpStatus.OK.value());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private Optional<Boolean> responseCheck() {
        var start = mls();
        try {
            return isAlive();
        } catch (IOException e) {
            log.warn("{} liveness check problem", this, e);
            return Optional.empty();
        } finally {
            lastResponseTime = mls() - start;
        }
    }

    private void notifyExternalServices(long responseTime) {

        var request = HttpRequest.newBuilder().uri(uri(responseTime)).build();
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

    private long mls() {
        return System.currentTimeMillis();
    }

    private URI uri(long responseTime) {
        return URI.create(String.format("%s/json.htm?type=command&param=udevice&idx=%s" +
                "&nvalue=%s&svalue=%s&passcode=%s", ADDRESS, domoticzIndex, responseTime, responseTime, CODE));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", WebpageProbeTask.class.getSimpleName() + "[", "]")
                .add("alive=" + alive)
                .add("lastRun=" + Instant.ofEpochMilli(lastRun))
                .add("lastResponseTime=" + lastResponseTime + "ms")
                .add("lastFailed=" + lastFailed)
                .toString();
    }
}
