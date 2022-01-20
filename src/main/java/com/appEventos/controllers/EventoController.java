package com.appEventos.controllers;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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

@EnableJpaRepositories(basePackageClasses = { com.appEventos.repository.ConvidadoRepository.class,
		com.appEventos.repository.EventoRepository.class }) // pacote onde está o repository
@Controller
public class EventoController {

	@Autowired
	private EventoRepository er;

	@Autowired
	private ConvidadoRepository cr;

	@Autowired
	private UsuarioRepository ur;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView listaEventos() {
		/*
		 * Iterable é uma interface que determina que uma lista pode ter seus elementos
		 * alcançados por uma estrutura foreach.
		 */

		Iterable<Evento> eventos = er.findByUsuario(getNomeUser());

		// informar o local em que os dados vai ser renderizado
		ModelAndView mv = new ModelAndView("index");
		mv.addObject("eventos", eventos);
		mv.addObject("user", getNomeUser().getNome());

		if (getNomeUser().getRole().equals("ADMIN")) {
			mv.addObject("link", "/usuarios");
			mv.addObject("texto", "Listar usuários");
		}

		return mv;

	}

	// Busca eventos
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ModelAndView bucaEventos(String nome) {
		/*
		 * Iterable é uma interface que determina que uma lista pode ter seus elementos
		 * alcançados por uma estrutura foreach.
		 */

		Iterable<Evento> eventos = er.findByNomeContainingAndUsuario(nome, getNomeUser());

		// informar o local em que os dados vai ser renderizado
		ModelAndView mv = new ModelAndView("index");
		mv.addObject("eventos", eventos);
		mv.addObject("user", getNomeUser().getNome());
		mv.addObject("termoDeBusca", nome);
		
		if (getNomeUser().getRole().equals("ADMIN")) {
			mv.addObject("link", "/usuarios");
			mv.addObject("texto", "Listar usuários");
		}
		
		return mv;

	}

	@RequestMapping(value = "/cadastrarEvento", method = RequestMethod.GET)
	public ModelAndView form() {
		ModelAndView mv = new ModelAndView("evento/formEvento");
		mv.addObject("user", getNomeUser().getNome());

		if (getNomeUser().getRole().equals("ADMIN")) {
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

		evento.setUsuario(getNomeUser());

		er.save(evento);
		attributes.addFlashAttribute("mensagem", "Evento salvo com sucesso!");
		return "redirect:/cadastrarEvento";
	}

	@RequestMapping(value = "/editaEvento/{codigo}", method = RequestMethod.GET)
	public ModelAndView editarEvento(@PathVariable("codigo") long codigo) {
		Evento evento = er.findByCodigo(codigo);
		ModelAndView mv = new ModelAndView("evento/editaEvento");
		mv.addObject("evento", evento);
		mv.addObject("user", getNomeUser().getNome());
		if (getNomeUser().getRole().equals("ADMIN")) {
			mv.addObject("link", "/usuarios");
			mv.addObject("texto", "Listar usuários");
		}
		return mv;

	}

	@RequestMapping(value = "/editaEvento/{codigo}", method = RequestMethod.POST)
	public String salvaEditarEvento(@Valid Evento evento, BindingResult result, RedirectAttributes attributes) {
		if (result.hasErrors()) {
			attributes.addFlashAttribute("mensagem", "Verifique os campos!");
			return "redirect:/editaEvento/{codigo}";
		}

		ModelAndView mv = new ModelAndView("evento/editaEvento");
		mv.addObject("evento", evento);
		evento.setUsuario(getNomeUser());
		er.save(evento);
		attributes.addFlashAttribute("mensagem", "Evento editado com sucesso!");
		return "redirect:/editaEvento/{codigo}";

	}

	@RequestMapping("/deletarEvento")
	public String delataEvento(long codigo) {
		Evento evento = er.findByCodigo(codigo);

		Iterable<Convidado> convidados = cr.findByEvento(evento);

		for (Convidado convi : convidados) {
			cr.delete(convi);
		}

		er.delete(evento);
		return "redirect:/";
	}

	/*
	 * @PathVariable pega o valor passado pela URL, por exemplo:
	 * http://localhost:8080/api/employees/111, 111 é valor pego
	 */
	@RequestMapping(value = "/evento/{codigo}", method = RequestMethod.GET)
	public ModelAndView detalhesEvento(@PathVariable("codigo") long codigo) {
		Evento evento = er.findByCodigo(codigo);
		ModelAndView mv = new ModelAndView("evento/detalhesEvento");
		mv.addObject("evento", evento);

		Iterable<Convidado> convidados = cr.findByEvento(evento);
		mv.addObject("convidados", convidados);
		mv.addObject("user", getNomeUser().getNome());
		if (getNomeUser().getRole().equals("ADMIN")) {
			mv.addObject("link", "/usuarios");
			mv.addObject("texto", "Listar usuários");
		}
		return mv;
	}

	@RequestMapping(value = "/evento/{codigo}", method = RequestMethod.POST)
	public String detalhesEventoAddParticipante(@PathVariable("codigo") long codigo, @Valid Convidado convidado,
			BindingResult result, RedirectAttributes attributes) {

		if (result.hasErrors()) {
			attributes.addFlashAttribute("mensagem", "Verifique os campos!");
			return "redirect:/evento/{codigo}";
		}
		Evento evento = er.findByCodigo(codigo);
		Convidado convVerifica = cr.findByCpfAndEvento(convidado.getCpf(), evento);

		if (convVerifica != null && convVerifica.getEvento().getCodigo() == codigo) {
			attributes.addFlashAttribute("mensagem", "já existe um participante cadastrado com esse CPF!");
			return "redirect:/evento/{codigo}";
		}

		convVerifica = cr.findByEmailAndEvento(convidado.getEmail(), evento);

		if (convVerifica != null && convVerifica.getEvento().getCodigo() == codigo) {
			attributes.addFlashAttribute("mensagem", "já existe um participante cadastrado com esse E-mail!");
			return "redirect:/evento/{codigo}";
		}

		convidado.setEvento(evento);

		cr.save(convidado);
		attributes.addFlashAttribute("mensagem", "Participante adicionado com sucesso!");
		return "redirect:/evento/{codigo}";

	}

	// Busca participantes
	@RequestMapping(value = "/evento/busca/{busca}", method = RequestMethod.POST)
	public ModelAndView buscaParticipante(@PathVariable("busca") long busca, String nomeConvidado, String codigo) {
		Evento evento = er.findByCodigo(busca);
		ModelAndView mv = new ModelAndView("evento/detalhesEvento");
		mv.addObject("evento", evento);
		mv.addObject("user", getNomeUser().getNome());
		Iterable<Convidado> convidados = cr.findByNomeConvidadoContainingAndEvento(nomeConvidado, evento);
		mv.addObject("convidados", convidados);
		mv.addObject("termoDeBusca", nomeConvidado);
		if (getNomeUser().getRole().equals("ADMIN")) {
			mv.addObject("link", "/usuarios");
			mv.addObject("texto", "Listar usuários");
		}
		return mv;
	}

	@RequestMapping(value = "/evento/{codigo}/editarParticipante/{id}", method = RequestMethod.GET)
	public ModelAndView editarParticipante(@PathVariable("id") long id, @PathVariable("codigo") long codigo) {
		Convidado convidado = cr.findById(id);

		ModelAndView mv = new ModelAndView("evento/editarParticipante");
		mv.addObject("convidado", convidado);
		mv.addObject("codigo", codigo);
		mv.addObject("user", getNomeUser().getNome());
		if (getNomeUser().getRole().equals("ADMIN")) {
			mv.addObject("link", "/usuarios");
			mv.addObject("texto", "Listar usuários");
		}
		return mv;

	}

	@RequestMapping(value = "/evento/{codigo}/editarParticipante/{id}", method = RequestMethod.POST)
	public String salvaEditarParticipante(@Valid Convidado convidado, BindingResult result,
			RedirectAttributes attributes, @PathVariable("codigo") long codigo, @PathVariable("id") long id) {

		if (result.hasErrors()) {
			attributes.addFlashAttribute("mensagem", "Verifique os campos!");
			return "redirect:/evento/{codigo}/editarParticipante/{id}";
		}

		Evento evento = er.findByCodigo(codigo);
		Convidado convVerifica = cr.findByCpfAndEvento(convidado.getCpf(), evento);

		if (convVerifica != null && convVerifica.getEvento().getCodigo() == codigo && convVerifica.getId() != id) {
			attributes.addFlashAttribute("mensagem", "já existe um participante cadastrado com esse CPF!");
			return "redirect:/evento/{codigo}/editarParticipante/{id}";
		}

		convVerifica = cr.findByEmailAndEvento(convidado.getEmail(), evento);

		if (convVerifica != null && convVerifica.getEvento().getCodigo() == codigo && convVerifica.getId() != id) {
			attributes.addFlashAttribute("mensagem", "já existe um participante cadastrado com esse E-mail!");
			return "redirect:/evento/{codigo}/editarParticipante/{id}";
		}

		convidado.setEvento(er.findByCodigo(codigo));
		cr.save(convidado);
		attributes.addFlashAttribute("mensagem", "Convidado editado com sucesso!");
		return "redirect:/evento/{codigo}/editarParticipante/{id}";

	}

	@RequestMapping("/deletarConvidado")
	public String delataConvidado(long id, long codigo) {
		Convidado convidado = cr.findById(id);

		cr.delete(convidado);
		return "redirect:/evento/" + codigo;
	}

	@RequestMapping("/usuarios")
	public ModelAndView listaTodosUsuarios() {
		ModelAndView mv = new ModelAndView("listaUsuarios");
		Iterable<Usuario> users = ur.findByRole("USER");
		mv.addObject("users", users);
		mv.addObject("user", getNomeUser().getNome());
		if (getNomeUser().getRole().equals("ADMIN")) {
			mv.addObject("link", "/usuarios");
			mv.addObject("texto", "Listar usuários");
		}
	
		return mv;
	}

	

	@RequestMapping(value = "/deletarUsuario", method = RequestMethod.GET)
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

	private Usuario getNomeUser() {
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
