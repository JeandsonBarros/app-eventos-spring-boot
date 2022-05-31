package com.appEventos.controllers;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.appEventos.models.Convidado;
import com.appEventos.models.Evento;
import com.appEventos.models.Usuario;
import com.appEventos.repository.ConvidadoRepository;
import com.appEventos.repository.EventoRepository;
import com.appEventos.repository.UsuarioRepository;
import com.appEventos.security.Role;

@Controller
public class UserController {

	@Autowired
	private UsuarioRepository ur;

	@Autowired
	private EventoRepository er;

	@Autowired
	private ConvidadoRepository cr;

	@RequestMapping("/login")
	public String logar() {
		return "login";
	}

	@RequestMapping(value = "/cadastro", method = RequestMethod.GET)
	public String cadastro() {
		return "cadastro";
	}

	@RequestMapping(value = "/cadastro", method = RequestMethod.POST)
	public String cadastrar(@Valid Usuario usuario, BindingResult result, RedirectAttributes attributes) {
		if (result.hasErrors()) {
			attributes.addFlashAttribute("mensagem", "Verifique os campos!");
			return "redirect:/cadastro";
		}
		if (ur.findByEmail(usuario.getEmail()) != null) {
			attributes.addFlashAttribute("mensagem", "Este email já foi cadastrado!");
			return "redirect:/cadastro";
		}

		if (!usuario.getSenha().equals(usuario.getConfirmaSenha())) {
			attributes.addFlashAttribute("mensagem", "Senhas não correspondem!");
			return "redirect:/cadastro";
		}

		Usuario newUser = new Usuario();

		newUser.setEmail(usuario.getEmail());
		newUser.setNome(usuario.getNome());
		newUser.setSenha(new BCryptPasswordEncoder().encode(usuario.getSenha()));
		newUser.setConfirmaSenha(new BCryptPasswordEncoder().encode(usuario.getConfirmaSenha()));

		if (!(getUser()==null) && getUser().getRole().equals("ADMIN")) {
			newUser.setRole(Role.ADMIN.getNome());
			ur.save(newUser);
			attributes.addFlashAttribute("mensagem", "Administrador cadastrado!");
			return "redirect:/usuarios";
		} else {
			newUser.setRole(Role.USER.getNome());
		}

		ur.save(newUser);

		attributes.addFlashAttribute("mensagem", "Usuário cadastrado!");
		return "redirect:/cadastro";

	}

	@RequestMapping(value = "/deletarconta", method = RequestMethod.GET)
	public String deletarConta() {

		Usuario use = getUser();
		Iterable<Evento> eventos = er.findByUsuario(use);

		for (Evento eve : eventos) {

			Iterable<Convidado> convidados = cr.findByEvento(eve);

			for (Convidado convi : convidados) {
				cr.delete(convi);
			}
			er.delete(eve);
		}
		ur.delete(use);

		return "redirect:/logout";
	}

	@RequestMapping(value = "/editanomeuser", method = RequestMethod.POST)
	public String editaNome(@RequestParam("nome") String nome) {

		if (!nome.isEmpty()) {
			Usuario use = getUser();
			use.setNome(nome);
			ur.save(use);
		}
		return "redirect:/seus-eventos";
	}

	@RequestMapping(value = "/editasenha", method = RequestMethod.POST)
	public String editaSenha(@Valid @RequestParam /* String senhaAntiga, */ String novaSenha, String confirmaNovaSenha,
			RedirectAttributes attributes) {
		if (/* senhaAntiga.isEmpty()|| */ novaSenha.isEmpty() || confirmaNovaSenha.isEmpty()) {
			attributes.addFlashAttribute("mensagem", "Verifique os campos!");
			return "redirect:/seus-eventos";
		}

		Usuario newUser = getUser();

		// if (newUser.getSenha().equals(new
		// BCryptPasswordEncoder().encode(senhaAntiga))) {
		if (novaSenha.equals(confirmaNovaSenha)) {
			newUser.setSenha(new BCryptPasswordEncoder().encode(novaSenha));
			newUser.setConfirmaSenha(new BCryptPasswordEncoder().encode(confirmaNovaSenha));
			ur.save(newUser);
			attributes.addFlashAttribute("mensagem", "Senha editada!");
		} else {
			attributes.addFlashAttribute("mensagem", "Senhas não correspondem!");
		}
		// }else{
		// attributes.addFlashAttribute("mensagem", "Senha errada!");
		// }
		return "redirect:/seus-eventos";
	}

	@RequestMapping("/usuarios")
	public ModelAndView listaTodosUsuarios() {
		ModelAndView mv;

		
			mv = new ModelAndView("listaUsuarios");
			Iterable<Usuario> users = ur.findByRole("USER");

			mv.addObject("users", users);
			mv.addObject("nome", getUser().getNome());

			if (getUser().getRole().equals("ADMIN")) {
				mv.addObject("link", "/usuarios");
				mv.addObject("texto", "Listar usuários");
			}
			return mv;
		
	}

	@RequestMapping(value="/usuarios/buscausuario", method = RequestMethod.GET)
	public ModelAndView bucaUsuarios(@RequestParam String nomeBusca) {
		ModelAndView mv;

		
			mv = new ModelAndView("listaUsuarios");
			Iterable<Usuario> users = ur.findByNomeContainingAndRole(nomeBusca, "USER");
		
			mv.addObject("users", users);
			mv.addObject("nome", getUser().getNome());

			if (getUser().getRole().equals("ADMIN")) {
				mv.addObject("link", "/usuarios");
				mv.addObject("texto", "Listar usuários");
			}
			return mv;
	
		
	}

	@RequestMapping(value = "/usuarios/deletarUsuario", method = RequestMethod.GET)
	public String deletaUsuarios(String usuario) {
		
			Usuario use = ur.findByEmail(usuario);
			Iterable<Evento> eventos = er.findByUsuario(use);

			for (Evento eve : eventos) {

				Iterable<Convidado> convidados = cr.findByEvento(eve);

				for (Convidado convi : convidados) {
					cr.delete(convi);
				}
				er.delete(eve);
			}
			ur.delete(use);

			return "redirect:/usuarios";
	
	}



	public Usuario getUser() {
		Object use = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		String nome;

		if (use instanceof UserDetails) {
			nome = ((UserDetails) use).getUsername();
		} else {
			nome = use.toString();
		}
		Usuario user = new Usuario();

		user = ur.findByEmail(nome);

		return user;
	}

}
