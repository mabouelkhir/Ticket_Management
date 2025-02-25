package com.example.ticketmanagement.repository;


import com.example.ticketmanagement.model.Status;
import com.example.ticketmanagement.model.Ticket;
import com.example.ticketmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreatedBy(User user);

    List<Ticket> findByCreatedById(Long userID);

    List<Ticket> findByIdAndStatus(Long ticketId, Status status);

    List<Ticket> findByStatus(Status status);


}