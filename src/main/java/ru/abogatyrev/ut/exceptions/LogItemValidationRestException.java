package ru.abogatyrev.ut.exceptions;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hamster on 28.03.2016.
 */
public class LogItemValidationRestException {

    private List<FieldError> errors = new ArrayList<FieldError>();

    public LogItemValidationRestException() {
    }

    public LogItemValidationRestException(List<FieldError> errors) {
        this.errors = errors;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldError> errors) {
        this.errors = errors;
    }

    @JsonPropertyOrder({"field", "message"})
    public static class FieldError extends Error {
        /**
         * название поля
         */
        private String field;

        public FieldError() {
        }

        public FieldError(String field, String message) {
            this.field = field;
            super.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

    }

    public static class Error {
        /**
         * описание ошибки
         */
        private String message;

        public Error() {
        }

        public Error(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
