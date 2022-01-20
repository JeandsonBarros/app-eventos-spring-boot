package com.appEventos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.appEventos.models.Convidado;
import com.appEventos.models.Evento;

public interface ConvidadoRepository extends JpaRepository<Convidado, String>{
	Iterable<Convidado> findByEvento(Evento evento);
	Iterable<Convidado> findByNomeConvidadoContainingAndEvento(String nomeConvidado, Evento evento);
	Convidado findById(Long id);
	Convidado findByCpfAndEvento(String cpf, Evento evento);
	Convidado findByEmailAndEvento(String email, Evento evento);
}
