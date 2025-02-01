package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private SerieRepository repository;
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serieBusca;

    public Principal(SerieRepository repository) {
        this.repository = repository;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por título
                    5 - Buscar séries por ator
                    6 - Top 5 séries
                    7 - Buscar séries por categoria 
                    8 - Buscar série com número máximo de temporadas
                    9 - Buscar episódio por trecho
                    10 - Buscar top 5 episódios de uma série 
                    11 - Buscar episódios a partir de uma data 
                                    
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
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
                    buscarSeriePorAtor();
                    break;

                case 6:
                    buscarTopCincoSeries();
                    break;

                case 7:
                    buscarSeriesPorCategoria();
                    break;

                case 8:
                        buscarSerieMaximoTemporadaEAvaliacao();
                        break;

                case 9:
                    buscarEpisodioPorTrecho();
                    break;

                case 10:
                    topEpisodiosPorSerie();
                    break;

                case 11:
                    buscarEpisodiosAposUmaData();
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
        repository.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()) {
            var serieEcontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEcontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEcontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEcontrada.setEpisodios(episodios);
            repository.save(serieEcontrada);

        } else {
            System.out.println("Série não encontrada");
        }

    }

    private void listarSeriesBuscadas() {
        series = repository.findAll();
        series.stream()
                .sorted(Comparator.comparing(s -> s.getGenero()))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();
        serieBusca = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()) {
            System.out.println("Dados da Série: " + serieBusca.get());
        } else {
            System.out.println("Série não encontrada.");
        }
    }

    private void buscarSeriePorAtor() {
        System.out.println("Digite o nome do ator: ");
        String nomeAtor = leitura.nextLine();
        List<Serie> seriesEncontradas = repository.findByAtoresContainingIgnoreCase(nomeAtor);
        System.out.println("Séries em que " + nomeAtor + " trabalhou:");
        seriesEncontradas.forEach(serie -> System.out.println(serie.getTitulo() + " Avaliação: " + serie.getAvaliacao()));
    }

    private void buscarTopCincoSeries() {
        List<Serie> topSeries = repository.findTop5ByOrderByAvaliacaoDesc();
        topSeries.forEach(serie -> System.out.println(serie.getTitulo() + " Avaliação: " + serie.getAvaliacao()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Digite a categoria/gênero da serie que deseja buscar: ");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriesBuscadas = repository.findByGenero(categoria);
        System.out.println("Séries da categoria " + nomeGenero);
        seriesBuscadas.forEach(System.out::println);
    }

    private void buscarSerieMaximoTemporadaEAvaliacao() {
        System.out.println("Digite o número máximo de temporadas: ");
        int numeroMaximoTemporadas = leitura.nextInt();
        leitura.nextLine();
        System.out.println("digite a nota mínima para as séries: ");
        double avaliacaoSerie = leitura.nextDouble();
        List<Serie> seriesBuscadas = repository.seriesPorTemporadaEAvaliacao(numeroMaximoTemporadas, avaliacaoSerie);

        if (!seriesBuscadas.isEmpty()) {
            System.out.println("Séries encontradas: ");
            seriesBuscadas.forEach(serie -> System.out.println(serie.getTitulo() + " Avaliação: " + serie.getAvaliacao() +
                    " Número de Temporadas: " + serie.getTotalTemporadas()));
        } else {
            System.out.println("Nenhuma série encontrada. Tente outros requisitos.");
        }
    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Digite o nome do episódio para busca");
        String trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrados = repository.episodiosPorTrecho(trechoEpisodio);

        episodiosEncontrados.forEach(episodio -> {
            System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
                    episodio.getSerie().getTitulo(), episodio.getTemporada(),
                    episodio.getNumeroEpisodio(), episodio.getTitulo());
        });
    }

    private void topEpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = repository.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(episodio -> {
                System.out.printf("Série: %s Temporada %s - Episódio %s - %s - Avaliação %s\n",
                        episodio.getSerie().getTitulo(), episodio.getTemporada(),
                        episodio.getNumeroEpisodio(), episodio.getTitulo(), episodio.getAvaliacao());
            });
        }
    }

    private void buscarEpisodiosAposUmaData() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            System.out.println("Digite o ano limite de lançamento");
            int anoLancamento = leitura.nextInt();
            leitura.nextLine();

            List<Episodio> episodiosAno = repository.episodiosPorSerieEAno(serie ,anoLancamento);
            episodiosAno.forEach(System.out::println);
        }
    }
}