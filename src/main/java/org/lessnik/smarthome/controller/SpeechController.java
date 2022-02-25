package org.lessnik.smarthome.controller;

import lombok.RequiredArgsConstructor;
import org.lessnik.smarthome.service.TtsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * @author Sergey Pomelov
 * @since 28.03.2019
 */
@RestController
@RequiredArgsConstructor
public class SpeechController {

    private final TtsService service;

    @PostMapping("/speak")
    @ResponseStatus(NO_CONTENT)
    public void speak(@RequestParam(name = "text") String text) {
        service.speak(text);
    }
}
