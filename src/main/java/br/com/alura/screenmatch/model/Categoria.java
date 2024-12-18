package br.com.alura.screenmatch.model;

public enum Categoria {
    ACAO("Action"),

    ROMANCE("Romance"),

    COMEDIA("Comedy"),

    DRAMA("Drama"),

    CRIME("Crime");

    private String categoriaOmdb;

    Categoria(String categoriaOmdb){
        this.categoriaOmdb = categoriaOmdb;
    }

    /**
     * Busca e retorna a constante do enum correspondente ao texto fornecido.
     *
     * @param text o texto a ser comparado com os valores associados às constantes do enum
     * @return a constante correspondente ao texto fornecido
     * @throws IllegalArgumentException se nenhuma correspondência for encontrada
     */
    public static Categoria fromString(String text) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.categoriaOmdb.equalsIgnoreCase(text)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada para a string fornecida: " + text);
    }

}
