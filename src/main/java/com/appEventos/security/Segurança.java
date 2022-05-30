package com.appEventos.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


//Ajuda:https://www.baeldung.com/spring-boot-security-autoconfiguration

@Configuration
@EnableWebSecurity
public class Segurança extends WebSecurityConfigurerAdapter {

	@Autowired
	private ImplementsUserDetailsService implementsUserDetailsService;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(implementsUserDetailsService).passwordEncoder(new BCryptPasswordEncoder());
		
		
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		.antMatchers(HttpMethod.GET, "/cadastro").permitAll()
		.antMatchers(HttpMethod.POST, "/cadastro").permitAll()
		.antMatchers(HttpMethod.GET, "/").hasAnyAuthority("USER", "ADMIN")
		.antMatchers(HttpMethod.POST, "/").hasAnyAuthority("USER", "ADMIN")
		.antMatchers(HttpMethod.GET, "/cadastrarEvento").hasAnyAuthority("USER", "ADMIN")
		.antMatchers(HttpMethod.POST, "/cadastrarEvento").hasAnyAuthority("USER", "ADMIN")
		.antMatchers(HttpMethod.GET, "/editaEvento/**").hasAnyAuthority("USER", "ADMIN")
		.antMatchers(HttpMethod.POST, "/editaEvento/**").hasAnyAuthority("USER", "ADMIN")
		.antMatchers(HttpMethod.GET, "/editaEvento/**").hasAnyAuthority("USER", "ADMIN")
		.antMatchers(HttpMethod.POST, "/busca/**").hasAnyAuthority("USER", "ADMIN")
		.antMatchers(HttpMethod.POST, "/evento/**").hasAnyAuthority("USER", "ADMIN")
		.antMatchers(HttpMethod.GET, "/evento/**").hasAnyAuthority("USER", "ADMIN")
		.antMatchers(HttpMethod.POST, "/editaEvento/**").hasAnyAuthority("USER", "ADMIN")
		.antMatchers(HttpMethod.GET, "/usuarios/**").hasAnyAuthority("ADMIN")
		.antMatchers(HttpMethod.POST, "/usuarios/**").hasAnyAuthority("ADMIN")
		.and().formLogin().loginPage("/login")
	    .defaultSuccessUrl("/", true)
	    .failureUrl("/login?error=true")
	    .permitAll()
		.and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).and()
		.csrf().disable()
		.headers().cacheControl().disable();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("../mensagemValidacao", "../img/**", "dialog.html");
	}

}