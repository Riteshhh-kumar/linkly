package com.linkly.repository;

import com.linkly.entity.Link;
import com.linkly.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    // Spring Data JPA automatically implements this method based on the name!
    Optional<Link> findByShortCode(String shortCode);


    List<Link> findByUser(User user);
}