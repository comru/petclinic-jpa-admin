package io.amplicode.pja.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/")
public class RootController {

    @GetMapping
    String root() {
        return "UP";
    }
}

