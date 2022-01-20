package com.appEventos.controllers;


import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.appEventos.models.Usuario;
import com.appEventos.repository.UsuarioRepository;
import com.appEventos.security.Role;



@Controller
public class IndexController {
	
	
	@Autowired
	private UsuarioRepository ur;
	
	@RequestMapping("/login")
	public String logar() {
		return "login";
	}
	
	@RequestMapping(value = "/cadastro", method = RequestMethod.GET)
	public String cadastro() {
		return "cadastro";
	}
	
	
	@RequestMapping(value = "/cadastro", method = RequestMethod.POST)
	public String cadastrar(@Valid Usuario usuario, BindingResult result, RedirectAttributes attributes){
		if (result.hasErrors()) {
			attributes.addFlashAttribute("mensagem", "Verifique os campos!");
			return "redirect:/cadastro";
		}
		if(ur.findByEmail(usuario.getEmail())!=null) {
			attributes.addFlashAttribute("mensagem", "Esse email já foi cadastrado!");
			return "redirect:/cadastro";
		}
		
		if (!usuario.getSenha().equals(usuario.getConfirmaSenha()))
		{
			attributes.addFlashAttribute("mensagem", "Senhas não correspondem!");
			return "redirect:/cadastro";
		}
		
		Usuario newUser = new Usuario();
		
		newUser.setEmail(usuario.getEmail());
		newUser.setNome(usuario.getNome());
		newUser.setSenha(new BCryptPasswordEncoder().encode(usuario.getSenha()));
		newUser.setConfirmaSenha(new BCryptPasswordEncoder().encode(usuario.getConfirmaSenha()));
		newUser.setRole(Role.USER.getNome());
		
		ur.save(newUser);
		
		attributes.addFlashAttribute("mensagem", "Usuário cadastrado!");
		return "redirect:/cadastro";
		
	}
	
}
