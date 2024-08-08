package br.com.alura.screenmatch.principal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

public class Principal {
	
	private Scanner leitura = new Scanner(System.in);
	private ConsumoApi consumo = new ConsumoApi();
	private ConverteDados conversor = new ConverteDados();
	
	private final String ENDERECO = "https://www.omdbapi.com/?t=";
	private final String API_KEY = "&apikey=6585022c";
	private List<DadosSerie> dadosSeries = new ArrayList();
	
	
	private SerieRepository repositorio;
	
	private List<Serie> series = new ArrayList();
	
	private Optional<Serie> serieBusca;

	public Principal(SerieRepository repositorio) {
		this.repositorio = repositorio;
	}

	public void exibMenu() {
		var opcao = -1;
		while(opcao != 0) {
			var menu = """
					1 - Buscar séries
					2 - Buscar episódios
					3 - Listar séries buscadas
					4 - Buscar serie por titulo
					5 - Buscar series por ator
					6 - Top 5 series
					7 - Buscar series por categoria
					8 - Maximo temporada
					9 - Buscar episodio por trecho
					10 - Top 5 episodios por serie
					11 - Buscar episodios a partir de uma data
					
					0 - Sair
					""";
			
			System.out.println(menu);
			opcao = leitura.nextInt();
			leitura.nextLine();
			
			switch(opcao) {
				case 1:
		            buscarSerieWeb();
		            break;
		        case 2:
		            buscarEpisodioPorSerie();
		            break;
		        case 3:
		        	listarSeriesBuscadas();
		        	break;
		        case 4:
		        	buscarSeriePorTitulo();
		        	break;
		        case 5:
		        	buscarSeriesPorAtor();
		        	break;
		        case 6:
		        	buscarTop5Series();
		        	break;
		        case 7:
		        	buscarSeriesPorCategoria();
		        	break;
		        case 8:
		        	maximoTemporada();
		        	break;
		        case 9:
		        	buscarEpisodioPorTrecho();
		        	break;
		        case 10:
		        	topEpisodiosPorSerie();
		        	break;
		        case 11:
		        	buscarEpisodiosDepoisDeUmaData();
		        	break;
		        case 0:
		            System.out.println("Saindo...");
		            break;
		        default:
		            System.out.println("Opção inválida");
			}		
		}
	}
	


	private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        //dadosSeries.add(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterdados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
    	listarSeriesBuscadas();
    	System.out.println("Escolha uma serie pelo nome:");
    	var nomeSerie = leitura.nextLine();
    	
    	Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);
    	
    	if(serie.isPresent()) {
    		
    		var serieEncontrada = serie.get();
	        List<DadosTemporada> temporadas = new ArrayList<>();        
	
	        for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
	            var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
	            DadosTemporada dadosTemporada = conversor.obterdados(json, DadosTemporada.class);
	            temporadas.add(dadosTemporada);
	        }
	        temporadas.forEach(System.out::println);
	        
	        List<Episodio> episodios = temporadas.stream()
	        		.flatMap(d -> d.episodios().stream()
	        				.map(e -> new Episodio(d.numero(), e)))
				.collect(Collectors.toList());
	        serieEncontrada.setEpisodios(episodios);
	        repositorio.save(serieEncontrada);
	        	
	    }else {
	    		System.out.println("Serie não encontrada");
	    	}	
    }
    	
    
    private void listarSeriesBuscadas() {
    	series = repositorio.findAll();
    	series.stream()
    		.sorted(Comparator.comparing(Serie::getGenero))
    		.forEach(System.out::println);
    }
    
    private void buscarSeriePorTitulo() {
    	System.out.println("Escolha uma serie pelo nome:");
    	var nomeSerie = leitura.nextLine();
    	serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);
    	
    	if(serieBusca.isPresent()) {
    		System.out.println("Dados da serie: " + serieBusca.get());
    	}else {
    		System.out.println("Serie não encontrada");
    	}
		
	}
    
    private void buscarSeriesPorAtor() {
    	System.out.println("Qual o nome para busca?");
    	var nomeAtor = leitura.nextLine();
    	System.out.println("Avaliações a partir de qual valor: ");
    	var avaliacao = leitura.nextDouble();
		List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
		System.out.println("Series em que " + nomeAtor + "trabalhou: ");
		seriesEncontradas.forEach(s -> System.out.println(s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
		
	}
    
    private void buscarTop5Series() {
		List<Serie> serieTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
		serieTop.forEach(s -> 
						System.out.println(s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
	}
    
// com ENUM
	private void buscarSeriesPorCategoria() {
		System.out.println("Deseja buscar a serie de que categoria/genero? ");
		var nomeGenero = leitura.nextLine();
		Categoria categoria = Categoria.fromPortugues(nomeGenero);
		List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
		System.out.println("Series da categoria " + nomeGenero);
		seriesPorCategoria.forEach(System.out::println);
	}
    

	private void maximoTemporada() {
		System.out.println("Qual o número de temporadas?");
    	var numeroTemporadas = leitura.nextInt();
    	System.out.println("Avaliações a partir de qual valor? ");
    	var avaliacao = leitura.nextDouble();
		List<Serie> maxTemporada = repositorio.seriesPorTemporadaEAvaliacao(numeroTemporadas, avaliacao);
		System.out.println("Series com o maximo de " + numeroTemporadas + "temporadas e avaliação superior a: " + avaliacao);
		maxTemporada.forEach(System.out::println);
	}
	
	private void buscarEpisodioPorTrecho() {
		System.out.println("Qual o nome do episodio para busca?");
    	var trechoEpisodio = leitura.nextLine();
    	List<Episodio> episodiosEncontrados = repositorio.episodiosPorTrecho(trechoEpisodio);
    	episodiosEncontrados.forEach(e ->
        System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
                e.getSerie().getTitulo(), e.getTemporada(),
                e.getNumeroEpisodio(), e.getTitulo()));
		
	}
	
	private void topEpisodiosPorSerie() {
		buscarSeriePorTitulo();
		if(serieBusca.isPresent()) {
			Serie serie = serieBusca.get();
			List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);
			topEpisodios.forEach(e ->
        System.out.printf("Série: %s Temporada %s - Episódio %s - %s - Avaliacao %s\n",
                e.getSerie().getTitulo(), e.getTemporada(),
                e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao()));
		}
		
	}
	

	private void buscarEpisodiosDepoisDeUmaData() {
		buscarSeriePorTitulo();
		if(serieBusca.isPresent()) {
			Serie serie = serieBusca.get();
			System.out.println("Digite o ano limite de lançamento: ");
			var anoLancamento = leitura.nextInt();
			leitura.nextLine();
			
			List<Episodio> episodiosAno = repositorio.episodioPorSerieEAno(serie, anoLancamento);
			episodiosAno.forEach(System.out::println);
			
		}
		
	}
	
	
}















//		System.out.println("Digite o nome da série para busca.");
//		var nomeSerie = leitura.nextLine();
//        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);        
//		DadosSerie dados = conversor.obterdados(json, DadosSerie.class);
//		System.out.println(dados);
//		
//		List<DadosTemporada> temporadas = new ArrayList<>();
//		
//			for (int i = 1; i<=dados.totalTemporadas(); i++) {
//			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
//			DadosTemporada dadosTemporada = conversor.obterdados(json, DadosTemporada.class);
//			temporadas.add(dadosTemporada);
//		}
//		temporadas.forEach(System.out::println);
//		
//		for(int i = 0; i < dados.totalTemporadas(); i++) {
//			List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//			for(int j = 0; j < episodiosTemporada.size(); j++) {
//				System.out.println(episodiosTemporada.get(j).titulo());
//			}
//		}		
//		temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo()))); // lambda
//		
//		List<String> nomes = Arrays.asList("Jacque", "Iasmin", "Paulo", "Rodrigo", "Nico");
//				
//		// streams e lambdas
//		nomes.stream()
//			 .sorted()
//			 .limit(3)
//			 .filter(n->n.startsWith("N"))
//			 .map(n->n.toUpperCase())
//			 .forEach(System.out::println);
//		
//		List<DadosEpisodio> dadosEpisodios = temporadas.stream()
//				.flatMap(t-> t.episodios().stream())
//				.collect(Collectors.toList());
//		
//		System.out.println("\nTop 10 episodios");
//		dadosEpisodios.stream()
//			.filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//			.peek(e-> System.out.println("Primeiro filtro(N/A) " + e)) //espiar depuração
//			.sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
//			.peek(e-> System.out.println("Ordenção " + e))
//			.limit(10)
//			.peek(e-> System.out.println("Limite " + e))
//			.map(e-> e.titulo().toUpperCase())
//			.peek(e-> System.out.println("Mapeamento " + e))
//			.forEach(System.out::println);
//		
//		List<Episodio> episodios = temporadas.stream()
//		        .flatMap(t -> t.episodios().stream()
//		            .map(d -> new Episodio(t.numero(), d))
//		        ).collect(Collectors.toList());
//		episodios.forEach(System.out::println);
//		
//		System.out.println("Digite um trecho do titulo do episodio:");
//		var trechoTitulo = leitura.nextLine();
//		Optional<Episodio> episodioBuscado = episodios.stream()
//			.filter(e-> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
//			.findFirst();
//		if(episodioBuscado.isPresent()) {
//			System.out.println("Episodio encontrado: " + episodioBuscado.get().getTitulo());
//			System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
//		}else {
//			System.out.println("Episodio não encontrado.");
//		}
//				
//		
//		System.out.println("A partir de que ano você deseja ver os episodios?");
//		var ano = leitura.nextInt();
//		leitura.nextLine();
//		
//		DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//		LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//		episodios.stream()
//		.filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
//		.forEach(e -> System.out.println(
//				"Temporada: " + e.getTemporada() +
//		        " Episódio: " + e.getTitulo() +
//		        " Data lançamento: " + e.getDataLancamento().format(formatador)
//				));
//				
//		Map<Integer, Double> avaliacaoPorTemporada = episodios.stream()
//				.filter(e-> e.getAvaliacao() > 0.0)
//				.collect(Collectors.groupingBy(Episodio::getTemporada,
//						Collectors.averagingDouble(Episodio::getAvaliacao)));
//		System.out.println(avaliacaoPorTemporada);
//		
//		DoubleSummaryStatistics est = episodios.stream()
//			.filter(e-> e.getAvaliacao() > 0.0)
//			.collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
//		System.out.println("Media: " + est.getAverage());
//		System.out.println("Melhor episodio: " + est.getMax());
//		System.out.println("Pior Episodio: " + est.getMin());
//		System.out.println("Quantidade: " + est.getCount());
//	}

