package ru.abogatyrev.ut.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import ru.abogatyrev.ut.model.LogItem;

/**
 * Created by Hamster on 27.03.2016.
 */
@RepositoryRestResource(exported = false)
public interface LogItemRepository extends PagingAndSortingRepository<LogItem, Long> {

    LogItem save(LogItem s);

    Page<LogItem> findAll(Pageable pageable);
}
