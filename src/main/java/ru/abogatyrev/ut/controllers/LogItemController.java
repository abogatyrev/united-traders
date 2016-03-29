package ru.abogatyrev.ut.controllers;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.abogatyrev.ut.exceptions.LogItemValidationRestException;
import ru.abogatyrev.ut.model.Item;
import ru.abogatyrev.ut.model.LogItem;
import ru.abogatyrev.ut.repositories.LogItemRepository;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

/**
 * Created by Hamster on 28.03.2016.
 */
@RestController
@RequestMapping(value = "/")
public class LogItemController {
    private static final String UNKNOWN_STR = "UNKNOWN";

    @Autowired
    private LogItemRepository logItemRepository;

    @RequestMapping(method = RequestMethod.POST)
    public Item createLogItem(@RequestBody LogItem logItem, Principal principal) {
        logItem.setAuthor(principal != null ? principal.getName() : UNKNOWN_STR);
        Item result = logItemRepository.save(logItem);
        return new Item(result.getId());
    }

    @RequestMapping(method = RequestMethod.GET)
    public Page<LogItem> getLogItems(Pageable pageRequest) {
        // согласно заданию, всегда сортируем по возрастанию поля 'dt'
        pageRequest = new PageRequest(
                pageRequest.getPageNumber(), pageRequest.getPageSize(),
                new Sort(new Sort.Order(Sort.Direction.ASC, "dt")));

        Page<LogItem> result = logItemRepository.findAll(pageRequest);
        return result;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public LogItemValidationRestException logItemValidationExceptionHandle(HttpMessageNotReadableException e){
        String fieldName;
        String errMsg;
        LogItemValidationRestException exception = new LogItemValidationRestException();
        if(e.getCause() instanceof JsonMappingException){
            JsonMappingException mappingException = (JsonMappingException)e.getCause();
            fieldName = mappingException.getPath().get(0).getFieldName();
            errMsg = mappingException.getMessage();
        } else { // по идее такой ситуации быть не должно, но на всякий сделаем обработку...
            fieldName = UNKNOWN_STR;
            errMsg = e.getMessage();
        }

        exception.getErrors().add(new LogItemValidationRestException.FieldError(fieldName, errMsg));
        return exception;
    }
}
