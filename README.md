# Biblioteca Mobile

Trabalho T2 (NP2) — Desenvolvimento para Dispositivos Móveis · 2026.2

**Integrante:** _Barbara de Abreu Tavares (Mat.: 25.1.000509): _

## Problema e público-alvo

Estudantes do ensino médio e de cursinhos precisam encontrar rapidamente informações sobre os livros de leitura obrigatória (autor, ano, sinopse, temas). O **Biblioteca Mobile** permite pesquisar qualquer livro por título ou autor e ver os detalhes da obra, consultando o catálogo aberto da Open Library.

**Público-alvo:** estudantes e professores que querem localizar e conhecer livros para estudo.

## Funcionalidades

1. **Busca:** campo de título/autor com validação (vazio ou com menos de 3 caracteres mostra erro no campo) e sugestões de leitura em *chips*.
2. **Resultados:** lista de livros (capa, título, autores, ano e nº de edições) obtida da API, com estados de **carregando**, **sucesso**, **lista vazia** e **erro com "Tentar novamente"**.
3. **Detalhes:** mostra na hora os dados recebidos da tela anterior e busca na API a **descrição** e os **assuntos** da obra (também com carregando/erro/tentar novamente).
4. Ações: **compartilhar** o livro (Intent `ACTION_SEND`) e **abrir a página da obra** no navegador.

Fluxo principal: `Busca → Resultados → Detalhes` (com voltar pela seta da barra superior ou pelo botão voltar).

## API utilizada

[Open Library API](https://openlibrary.org/developers/api): pública, gratuita e **não exige chave**.

| Uso | Endpoint | Campos JSON usados |
|---|---|---|
| Buscar livros | `GET https://openlibrary.org/search.json?q={termo}&fields=key,title,author_name,first_publish_year,cover_i,edition_count&limit=20` | `numFound`, `docs[].key`, `title`, `author_name`, `first_publish_year`, `cover_i`, `edition_count` |
| Detalhes da obra | `GET https://openlibrary.org/works/{id}.json` (ex.: `OL1003040W`) | `title`, `description` (texto **ou** objeto `{type, value}`), `subjects` |
| Capas (imagem) | `https://covers.openlibrary.org/b/id/{cover_i}-M.jpg` | — |

Mapeamento JSON → Kotlin: `BuscaResponse`/`LivroResumo` e `ObraDetalhe` (pacote `data.model`), com `@SerializedName` ligando o nome do campo JSON ao nome da propriedade em português.

## Tecnologias e bibliotecas

- Kotlin, Android Studio, Activities + layouts XML, **ViewBinding**, RecyclerView, Material Components 3
- **Retrofit 2** + **Gson converter**: requisições REST e conversão do JSON
- OkHttp Logging Interceptor: registra as requisições no Logcat
- Coroutines (`lifecycleScope`): executa a requisição fora da thread principal
- Coil: carrega as imagens das capas a partir da URL

## Organização do projeto

```
app/src/main/java/com/escola/bibliotecamobile/
├── data/
│   ├── remote/RetrofitClient.kt   # configuração do Retrofit (URL base, timeouts, Gson)
│   ├── remote/OpenLibraryApi.kt   # endpoints (@GET, @Query, @Path)
│   ├── model/                     # classes que representam o JSON + Resultado (Sucesso/Erro)
│   └── LivroRepository.kt         # executa as chamadas e converte falhas em mensagens
└── ui/
    ├── busca/MainActivity.kt              # tela 1: busca e validação
    ├── resultados/ResultadosActivity.kt   # tela 2: lista + estados
    ├── resultados/LivroAdapter.kt         # adapter do RecyclerView
    └── detalhes/DetalhesActivity.kt       # tela 3: detalhes + estados + compartilhar
```

## Como executar

1. Abrir a pasta `biblioteca-mobile` no **Android Studio** (File → Open). Na primeira abertura ele baixa o Gradle e as dependências (precisa de internet).
2. Executar em um emulador ou aparelho com **Android 8.0 (API 26)** ou superior, com internet.
3. Gerar o APK: **Build → Build App Bundle(s) / APK(s) → Build APK(s)**. O arquivo fica em `app/build/outputs/apk/debug/app-debug.apk`.

Não há credenciais, tokens ou configurações secretas.

**Para testar o estado de erro:** ative o modo avião e faça uma busca → aparece a mensagem de "sem conexão" e o botão *Tentar novamente*.

## Limitações conhecidas

- Os dados vêm da Open Library: muitas descrições e assuntos estão em inglês e alguns livros não têm capa ou sinopse.
- Ao girar a tela, a busca é refeita (o projeto não usa ViewModel, que não faz parte do conteúdo deste trabalho).
- Não há armazenamento local nem favoritos: tudo depende de conexão com a internet.
- A busca traz no máximo 20 resultados (sem paginação).

## Uso de Inteligência Artificial

Usamos o assistente Claude (Anthropic) para gerar a estrutura inicial do projeto, das classes de Retrofit/modelos JSON e dos layouts. O código foi revisado, testado e ajustado (ver `EXPLICACAO.md`).
