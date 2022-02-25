package org.lessnik.smarthome.model;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;

@Slf4j
@ParametersAreNonnullByDefault
public final class PingTask extends LivenessProbeTask {

    protected final String ipAddress;

    public PingTask(String device, String ipAddress, long domoticzIndex, int timeout) {
        super(device, domoticzIndex, timeout);
        this.ipAddress = ipAddress;
    }

    @Override
    public Optional<Boolean> isAlive() throws IOException {
        return Optional.of(InetAddress.getByName(ipAddress).isReachable(timeout));
    }
}
