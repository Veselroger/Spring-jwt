package com.github.veselroger.jwt.repositories;

import com.github.veselroger.jwt.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to work with {@link User} entities.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findByName(String name);
}
