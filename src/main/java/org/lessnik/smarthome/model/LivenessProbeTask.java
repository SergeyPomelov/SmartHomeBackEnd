package org.lessnik.smarthome.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Optional;
import java.util.StringJoiner;

import static org.lessnik.smarthome.model.ClientBuilder.getClient;
import static org.lessnik.smarthome.service.SecretsHolder.ADDRESS;
import static org.lessnik.smarthome.service.SecretsHolder.CODE;


@Slf4j
@RequiredArgsConstructor
public abstract class LivenessProbeTask implements ILivenessProbe, IAsyncTask {

    protected static final HttpClient client = getClient();

    protected final String device;
    protected final long domoticzIndex;
    protected final int timeout;

    protected Optional<Boolean> alive = Optional.empty();
    protected boolean lastFailed = false;
    protected long lastRun = System.currentTimeMillis();
    protected long lastResponseTime;

    public void wake() {
        if (canRun()) {
            run();
        }
    }

    private boolean canRun() {
        return (mls() - lastRun) > lastResponseTime * 2;
    }

    private void run() {
        var newIsAlive = livenessCheck();
        newIsAlive.ifPresent(liveness -> {
            final boolean isForced = isForcedRefresh() && !livenessOnServerCheck().equals(alive);
            if (!newIsAlive.equals(alive) || isForced) {
                if (!isForced) {
                    log.info("{}: {}", this, newIsAlive);
                }
                alive = newIsAlive;
                notifyExternalServices(liveness);
                lastRun = mls();

            }
        });
    }

    private Optional<Boolean> livenessOnServerCheck() {
        var request = HttpRequest.newBuilder().uri(infoUri()).build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (!response.body().contains("\"status\" : \"OK\"")) {
                log.warn("{}: {}", this, response.body());
            } else {
                return Optional.of(response.body().contains("\"Status\" : \"On\""));
            }
        } catch (IOException | InterruptedException e) {
            log.warn("{}: {}", this, e);
        }
        return Optional.empty();
    }

    private boolean isForcedRefresh() {
        return lastFailed || ((mls() - lastRun) > 3_600_000);
    }

    private Optional<Boolean> livenessCheck() {
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

    private void notifyExternalServices(boolean isAlive) {

        var request = HttpRequest.newBuilder()
                .uri(switchUri(isAlive))
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            lastFailed = false;
            if (!response.body().contains("\"status\" : \"OK\"")) {
                lastFailed = true;
                log.warn("{}: {}", this, response.body());
            }
        } catch (IOException | InterruptedException e) {
            lastFailed = true;
            log.warn("{}: {}", this, e);
        }
    }

    private URI infoUri() {
        return URI.create(String.format("%s/json.htm?type=devices&rid=%s", ADDRESS, domoticzIndex));
    }

    private URI switchUri(boolean isAlive) {
        return URI.create(String.format("%s/json.htm?type=command&param=switchlight&idx=%s&switchcmd" +
                "=%s&passcode=%s", ADDRESS, domoticzIndex, isAlive ? "On" : "Off", CODE));
    }

    private long mls() {
        return System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LivenessProbeTask.class.getSimpleName() + "[", "]")
                .add("device='" + device + "'")
                .add("alive=" + alive)
                .add("lastRun=" + Instant.ofEpochMilli(lastRun))
                .add("lastResponseTime=" + lastResponseTime + "ms")
                .toString();
    }
}
