package nl.inholland.codegen.bankingapp.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("example")
public class ExampleRoute {
    public ExampleRoute() {}

    @GetMapping
    public ResponseEntity<String> get() {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
