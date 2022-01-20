package com.appEventos.security;


import java.util.HashSet;
import java.util.Set;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.appEventos.models.Usuario;
import com.appEventos.repository.UsuarioRepository;

@Repository
@Service("implementsUserDetailsService")
public class ImplementsUserDetailsService implements UserDetailsService
{

	@Autowired
	private UsuarioRepository ur;
	
	
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
		Usuario usuario = ur.findByEmail(login);
		
		if(usuario == null){
			throw new UsernameNotFoundException("Usuario não encontrado!");
		}
		
		
		
		SimpleGrantedAuthority authority = new  SimpleGrantedAuthority(usuario.getRole());
		
		Set<GrantedAuthority> authorities = new HashSet<>();
		authorities.add(authority);
		
		User user = new User(usuario.getEmail(), usuario.getSenha(), true, true, true, true, authorities);
		
		return user;
	

}
	
}

