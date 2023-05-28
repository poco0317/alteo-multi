package com.etterna.multi.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etterna.multi.data.LobbyMessage;

public interface LobbyMessageRepository extends JpaRepository<LobbyMessage, Long> {

}
