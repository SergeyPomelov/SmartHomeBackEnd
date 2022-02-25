package org.lessnik.smarthome.model;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@ParametersAreNonnullByDefault
public final class ISpyLivenessTask extends LivenessProbeTask {

    public ISpyLivenessTask(String device, long domoticzIndex, int timeout) {
        super(device, domoticzIndex, timeout);
    }

    @Override
    public Optional<Boolean> isAlive() throws IOException {

        var request = HttpRequest.newBuilder()
                .uri(URI.create(("http://192.168.1.37:18086/ping")))
                .timeout(Duration.ofMillis(timeout))
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(response.body().contains("OK"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
}
