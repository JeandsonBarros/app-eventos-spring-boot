package com.appEventos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.appEventos.models.Evento;
import com.appEventos.models.Usuario;



//JpaRepository tem métodos de Crud, como: salvar, deletar, selecionar e outros.
public interface EventoRepository extends JpaRepository<Evento, String>{
	Evento findByCodigo(long codigo);
	Iterable<Evento> findByUsuario(Usuario user);
	Iterable<Evento> findByNomeContainingAndUsuario(String nome, Usuario user);
	
	
}
