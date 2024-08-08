package br.com.alura.screenmatch.dto;

import br.com.alura.screenmatch.model.Categoria;

public record SerieDTO( Long id,
						String titulo,
						Integer totalTemporadas,
						Double avaliacao,
						Categoria genero,
						String atores,
						String poster,
						String sinopse) {

	public Long id() {
		return id;
	}

	public String titulo() {
		return titulo;
	}

	public Integer totalTemporadas() {
		return totalTemporadas;
	}

	public Double avaliacao() {
		return avaliacao;
	}

	public Categoria genero() {
		return genero;
	}

	public String atores() {
		return atores;
	}

	public String poster() {
		return poster;
	}

	public String sinopse() {
		return sinopse;
	}
	
	

}

