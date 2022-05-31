package com.appEventos.controllers;

import javax.validation.Valid;

import com.appEventos.models.Convidado;
import com.appEventos.models.Evento;
import com.appEventos.models.Usuario;
import com.appEventos.repository.ConvidadoRepository;
import com.appEventos.repository.EventoRepository;
import com.appEventos.repository.UsuarioRepository;

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

@Controller
public class ConvidadoController {
    @Autowired
	private EventoRepository er;

	@Autowired
	private ConvidadoRepository cr;

	@Autowired
	private UsuarioRepository ur;

    /* =======================ConvidadoController==================== */
	@RequestMapping(value = "/evento/{codigo}", method = RequestMethod.POST)
	public String EventoAddParticipante(@PathVariable("codigo") long codigo, @Valid Convidado convidado,
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
	@RequestMapping(value = "/evento/busca/{busca}", method = RequestMethod.GET)
	public ModelAndView buscaParticipante(@PathVariable("busca") long busca, String nomeConvidado, String codigo) {
		Evento evento = er.findByCodigo(busca);
		ModelAndView mv = new ModelAndView("evento/detalhesEvento");
		mv.addObject("evento", evento);
		mv.addObject("user", getUser().getNome());
		Iterable<Convidado> convidados = cr.findByNomeConvidadoContainingAndEvento(nomeConvidado, evento);
		mv.addObject("convidados", convidados);
		mv.addObject("termoDeBusca", nomeConvidado);
		if (getUser().getRole().equals("ADMIN")) {
			mv.addObject("link", "/usuarios");
			mv.addObject("texto", "Listar usuários");
		}
		return mv;
	}

	@RequestMapping(value = "/evento/{codigo}/editarParticipante/{id}", method = RequestMethod.GET)
	public ModelAndView editarParticipante(@PathVariable("id") long id, @PathVariable("codigo") long codigo) {

		Convidado convidado = cr.findById(id);
		Evento evento = convidado.getEvento();

		ModelAndView mv;

		if (evento.getUsuario().getEmail() == getUser().getEmail()) {

			mv = new ModelAndView("evento/editarParticipante");
			mv.addObject("convidado", convidado);
			mv.addObject("codigo", codigo);
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
		Evento evento = convidado.getEvento();

		if (evento.getUsuario().getEmail() == getUser().getEmail()) {

			cr.delete(convidado);
			return "redirect:/evento/" + codigo;
		} else {
			return "error404";
		}
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
