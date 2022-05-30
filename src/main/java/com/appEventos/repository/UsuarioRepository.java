package com.appEventos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.appEventos.models.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String>{

	Usuario findByEmail(String login);
	List<Usuario> findByRole(String role);
	List<Usuario> findByNomeContainingAndRole(String nome, String role);
	
}
