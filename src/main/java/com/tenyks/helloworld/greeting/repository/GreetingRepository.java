package com.tenyks.helloworld.greeting.repository;

import com.tenyks.helloworld.greeting.domain.Greeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GreetingRepository extends JpaRepository<Greeting, Long> {
}
