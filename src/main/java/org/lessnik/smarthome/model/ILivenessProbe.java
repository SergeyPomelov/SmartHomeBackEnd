package org.lessnik.smarthome.model;

import java.io.IOException;
import java.util.Optional;

public interface ILivenessProbe {

    Optional<Boolean> isAlive() throws IOException;
}
