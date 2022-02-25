package org.lessnik.smarthome.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Sergey Pomelov
 * @since 28.03.2019
 */
@RestController
public class StatusController {

    @GetMapping("/health")
    public String healthCheck() {
        return "Ok";
    }
}
