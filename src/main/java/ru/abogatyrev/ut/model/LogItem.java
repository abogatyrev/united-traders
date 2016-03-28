package ru.abogatyrev.ut.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Hamster on 27.03.2016.
 */
@Entity
@JsonIgnoreProperties({"id"})
public class LogItem extends Item {

    @JsonIgnore
    @Column(nullable = false, updatable = false)
    private String author;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date dt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private LogLevel level;

    @Column(nullable = false, updatable = false)
    private String message;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDt() {
        return dt;
    }

    public void setDt(Date dt) {
        this.dt = dt;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
