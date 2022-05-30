package com.appEventos.controllers;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.appEventos.models.Convidado;
import com.appEventos.models.Evento;
import com.appEventos.models.Usuario;
import com.appEventos.repository.ConvidadoRepository;
import com.appEventos.repository.EventoRepository;
import com.appEventos.repository.UsuarioRepository;

@Controller
public class EventoController {

	@Autowired
	private EventoRepository er;

	@Autowired
	private ConvidadoRepository cr;

	@Autowired
	private UsuarioRepository ur;

	/* =======================EventoController==================== */

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView listaEventos() {
		/*
		 * Iterable é uma interface que determina que uma lista pode ter seus elementos
		 * alcançados por uma estrutura foreach.
		 */

		Iterable<Evento> eventos = er.findByUsuario(getUser());

		// informar o local em que os dados vai ser renderizado
		ModelAndView mv = new ModelAndView("index");
		mv.addObject("eventos", eventos);
		mv.addObject("nome", getUser().getNome());

		if (getUser().getRole().equals("ADMIN")) {
			mv.addObject("link", "/usuarios");
			mv.addObject("texto", "Listar usuários");
		}

		return mv;

	}

	// Busca eventos
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ModelAndView bucaEventos(String nome) {

		Iterable<Evento> eventos = er.findByNomeContainingAndUsuario(nome, getUser());

		// informar o local em que os dados vai ser renderizado
		ModelAndView mv = new ModelAndView("index");
		mv.addObject("eventos", eventos);
		mv.addObject("nome", getUser().getNome());
		mv.addObject("termoDeBusca", nome);

		if (getUser().getRole().equals("ADMIN")) {
			mv.addObject("link", "/usuarios");
			mv.addObject("texto", "Listar usuários");
		}

		return mv;

	}

	@RequestMapping(value = "/cadastrarEvento", method = RequestMethod.GET)
	public ModelAndView form() {
		ModelAndView mv = new ModelAndView("evento/formEvento");
		mv.addObject("nome", getUser().getNome());

		if (getUser().getRole().equals("ADMIN")) {
			mv.addObject("link", "/usuarios");
			mv.addObject("texto", "Listar usuários");

		}

		return mv;
	}

	@RequestMapping(value = "/cadastrarEvento", method = RequestMethod.POST)
	public String form(@Valid Evento evento, BindingResult result, RedirectAttributes attributes) {

		if (result.hasErrors()) {
			attributes.addFlashAttribute("mensagem", "Verifique os campos!");
			return "redirect:/cadastrarEvento";
		}

		evento.setUsuario(getUser());

		er.save(evento);
		attributes.addFlashAttribute("mensagem", "Evento salvo com sucesso!");
		return "redirect:/cadastrarEvento";
	}

	@RequestMapping(value = "/editaEvento/{codigo}", method = RequestMethod.GET)
	public ModelAndView editarEvento(@PathVariable("codigo") long codigo) {
		Evento evento = er.findByCodigo(codigo);
		ModelAndView mv;

		if (evento.getUsuario().getEmail() == getUser().getEmail()) {

			mv = new ModelAndView("evento/editaEvento");

			mv.addObject("evento", evento);
			mv.addObject("nome", getUser().getNome());

			if (getUser().getRole().equals("ADMIN")) {
				mv.addObject("link", "/usuarios");
				mv.addObject("texto", "Listar usuários");
			}
			return mv;
		} else {
			mv = new ModelAndView("error404");
			return mv;
		}
	}

	@RequestMapping(value = "/editaEvento/{codigo}", method = RequestMethod.POST)
	public String salvaEditarEvento(@Valid Evento evento, BindingResult result, RedirectAttributes attributes) {
		if (result.hasErrors()) {
			attributes.addFlashAttribute("mensagem", "Verifique os campos!");
			return "redirect:/editaEvento/{codigo}";
		}

		ModelAndView mv = new ModelAndView("evento/editaEvento");
		mv.addObject("evento", evento);
		evento.setUsuario(getUser());
		er.save(evento);
		attributes.addFlashAttribute("mensagem", "Evento editado com sucesso!");
		return "redirect:/editaEvento/{codigo}";

	}

	@RequestMapping("/deletarEvento")
	public String delataEvento(long codigo) {
		Evento evento = er.findByCodigo(codigo);

		if (evento.getUsuario().getEmail() == getUser().getEmail()) {

			Iterable<Convidado> convidados = cr.findByEvento(evento);

			for (Convidado convi : convidados) {
				cr.delete(convi);
			}

			er.delete(evento);
			return "redirect:/";
		} else {
			return "error404";
		}
	}

	/*
	 * @PathVariable pega o valor passado pela URL, por exemplo:
	 * http://localhost:8080/api/employees/111, 111 é valor pego
	 */
	@RequestMapping(value = "/evento/{codigo}", method = RequestMethod.GET)
	public ModelAndView detalhesEvento(@PathVariable("codigo") long codigo) {
		Evento evento = er.findByCodigo(codigo);
		ModelAndView mv;

		if (evento.getUsuario().getEmail() == getUser().getEmail()) {

			mv = new ModelAndView("evento/detalhesEvento");
			mv.addObject("evento", evento);

			Iterable<Convidado> convidados = cr.findByEvento(evento);
			mv.addObject("convidados", convidados);
			mv.addObject("nome", getUser().getNome());
			if (getUser().getRole().equals("ADMIN")) {
				mv.addObject("link", "/usuarios");
				mv.addObject("texto", "Listar usuários");
			}
			return mv;

		} else {
			mv = new ModelAndView("error404");
			return mv;

		}
	}

	@RequestMapping(value = "/usuarios/eventosdeusuario", method = RequestMethod.GET)
	public ModelAndView listaEventosDeUsuario(String usuario) {
		ModelAndView mv;

			Iterable<Evento> eventos = er.findByUsuario(ur.findByEmail(usuario));

			// informar o local em que os dados vai ser renderizado
			mv = new ModelAndView("index");
			mv.addObject("eventos", eventos);
			mv.addObject("nome", getUser().getNome());

			if (getUser().getRole().equals("ADMIN")) {
				mv.addObject("link", "/usuarios");
				mv.addObject("texto", "Listar usuários");
			}
			return mv;
			
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
