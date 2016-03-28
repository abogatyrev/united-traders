package ru.abogatyrev.ut.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.abogatyrev.ut.exceptions.LogItemValidationRestException;

import java.security.Principal;

/**
 * Created by Hamster on 28.03.2016.
 */
@RestController
@RequestMapping("/error")
public class ErrorController {

    @RequestMapping(path = "/401", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public LogItemValidationRestException.Error unauthorizedError() {
        return new LogItemValidationRestException.Error("Access denied");
    }

    @RequestMapping(path = "/403", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    public LogItemValidationRestException.Error forbiddenError(Principal principal) {
        return new LogItemValidationRestException.Error(String.format("User %s does not have access",
                principal != null ? principal.getName() : "UNKNOWN"));
    }

}
