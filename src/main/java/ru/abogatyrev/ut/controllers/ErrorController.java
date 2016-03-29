package ru.abogatyrev.ut.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.abogatyrev.ut.exceptions.LogItemValidationRestException;

import java.security.Principal;

/**
 * Created by Hamster on 28.03.2016.
 */
@RestController
@RequestMapping("/WEB-INF/error")
public class ErrorController {
    @RequestMapping(path = "/401")
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public LogItemValidationRestException.Error unauthorizedError() {
        return new LogItemValidationRestException.Error("Access denied");
    }

    @RequestMapping(path = "/403")
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public LogItemValidationRestException.Error forbiddenError(Principal principal) {
        return new LogItemValidationRestException.Error(String.format("User '%s' does not have access",
                principal != null ? principal.getName() : "UNKNOWN"));
    }
}
