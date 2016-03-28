package ru.abogatyrev.ut.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Created by Hamster on 28.03.2016.
 */
@MappedSuperclass
public class Item {
    @Id
    @GeneratedValue
    @Column(nullable = false, insertable = false, updatable = false)
    private Long id;

    public Item() {
    }

    public Item(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
