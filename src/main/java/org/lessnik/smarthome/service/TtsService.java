package org.lessnik.smarthome.service;

import com.amazonaws.services.polly.model.OutputFormat;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import lombok.extern.slf4j.Slf4j;
import org.lessnik.smarthome.model.TtsEngine;
import org.springframework.stereotype.Service;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

@Slf4j
@Service
@ParametersAreNonnullByDefault
public class TtsService {

    final TtsEngine engine = new TtsEngine();

    public void speak(String text) {
        try {
            var speechStream = engine.synthesize(text, OutputFormat.Mp3);
            var player = new AdvancedPlayer(speechStream,
                    javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());
            player.play();
        } catch (IOException | JavaLayerException e) {
            log.error("Speech synthesize error", e);
        }
    }
}
