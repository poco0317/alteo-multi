package com.etterna.multi.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.etterna.multi.data.LobbyScore;

public interface LobbyScoreRepository extends JpaRepository<LobbyScore, Long> {

}
