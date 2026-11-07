# Ghibli App - Catálogo y Búsqueda de Películas de Studio Ghibli

## Vista Previa

https://github.com/user-attachments/assets/1f270fdf-d151-49b3-b944-6c2a78bfa059

## Introducción

App Android (Kotlin) que consume la API pública [Studio Ghibli API](https://ghibliapi.vercel.app/) para explorar las películas del estudio, buscarlas por título en tiempo real y guardar favoritas en una base de datos local SQLite que se puede consultar sin internet.

Segundo parcial de **Desarrollo de Aplicaciones Móviles 2** Fecha: Martes 17/11/2026 (2C 2026).

> **Sobre la API:** el enunciado del Tema 1 menciona Jikan, pero esa API quedó discontinuada. La app usa **Studio Ghibli API** (`https://ghibliapi.vercel.app/`), que es gratuita y no necesita API key. Como esta API no tiene géneros, episodios ni estado de emisión, el detalle muestra sus datos equivalentes: puntaje, año, duración, director, productor y sinopsis.

## Estructura de la app 
```text
GhibliApp/
├── app/
│   ├── build.gradle.kts  ← dependencias, viewBinding = true y compose = true
│   └── src/main/
│       ├── AndroidManifest.xml  ← permiso INTERNET + MainActivity + DetailActivity
│       ├── java/com/example/ghibliapp/
│       │   ├── data/  (CAPA DE DATOS - "Model")
│       │   │   ├── local/
│       │   │   │   └── FilmDbHelper.kt  SQLite: solo la tabla "favorites" (onCreate/onUpgrade)
│       │   │   ├── model/
│       │   │   │   └── FilmModel.kt  Modelo de una película (@SerializedName)
│       │   │   ├── remote/
│       │   │   │   ├── ApiService.kt  Endpoints de Retrofit (lista y detalle)
│       │   │   │   └── RetrofitClient.kt  Singleton de Retrofit (BASE_URL)
│       │   │   └── repository/
│       │   │       └── FilmRepository.kt  API + CRUD de favoritos, todo en Dispatchers.IO
│       │   ├── ui/  (CAPA DE PRESENTACIÓN - "View" y "ViewModel")
│       │   │   ├── adapter/
│       │   │   │   └── FilmAdapter.kt  Adapter del RecyclerView (ambas pantallas)
│       │   │   ├── fragment/
│       │   │   │   ├── FilmListFragment.kt  Pantalla 1: películas + buscador
│       │   │   │   └── FilmFavoritesFragment.kt  Pantalla 2: Favoritos
│       │   │   ├── detail/  (JETPACK COMPOSE)
│       │   │   │   ├── DetailActivity.kt  setContent { } - recibe solo el id
│       │   │   │   ├── FilmDetailScreen.kt  Pantalla de detalle
│       │   │   │   └── components/
│       │   │   │       └── FilmDetailComponents.kt  PicassoImage, FilmInfoCard, DetailSection + @Preview
│       │   │   ├── theme/
│       │   │   │   ├── Color.kt
│       │   │   │   ├── Type.kt
│       │   │   │   └── Theme.kt  (tema de Compose)
│       │   │   └── viewmodel/
│       │   │       ├── MainViewModel.kt  Lógica de la lista, búsqueda y favoritos (LiveData)
│       │   │       └── DetailViewModel.kt  Lógica del detalle (StateFlow)
│       │   └── MainActivity.kt  Contenedor + navegación inferior
│       └── res/
│           ├── drawable/
│           │   └── ic_film_placeholder.xml  (mientras carga el póster)
│           ├── layout/
│           │   ├── activity_main.xml
│           │   ├── fragment_film_list.xml
│           │   ├── fragment_film_favorites.xml
│           │   └── item_film.xml
│           ├── menu/
│           │   └── bottom_nav_menu.xml
│           ├── mipmap-*/  íconos de la app
│           ├── values/
│           │   ├── strings.xml
│           │   ├── colors.xml
│           │   └── themes.xml
│           └── xml/  reglas de backup
├── gradle/
│   └── libs.versions.toml  ← catálogo de versiones
└── settings.gradle.kts
```

## Diagrama general

### Arquitectura general de la aplicación

```text
                   ┌──────────────────────────────┐
                   │       CAPA DE PRESENTACIÓN   │
                   └──────────────────────────────┘

       ┌────────────────────────┐  ┌────────────────────────┐
       │       VIEW (XML)       │  │    VIEW (COMPOSE)      │
       │                        │  │                        │
       │     MainActivity       │  │     DetailActivity     │
       │     Fragments          │  │     FilmDetailScreen   │
       │     FilmAdapter        │  │                        │
       │     XML Layouts        │  │     • collectAsState() │
       │                        │  │       (StateFlow)      │
       │     • Observan         │  │                        │
       │       LiveData         │  │                        │
       └───────────┬────────────┘  └───────────┬────────────┘
                   │                           │
                   │                           │
                   ▼                           ▼
       ┌────────────────────────┐  ┌────────────────────────┐
       │     MainViewModel      │  │     DetailViewModel    │
       │                        │  │                        │
       │ • Descarga y filtra    │  │ • Solicita el detalle  │
       │   la lista             │  │   a la API             │
       │ • Gestiona errores     │  │ • Gestiona film        │
       │   y mensajes           │  │   isLoading            │
       │ • Inserta y elimina    │  │   errorMessage         │
       │   favoritos            │  │                        │
       └───────────┬────────────┘  └───────────┬────────────┘
                   │                           │
                   └─────────────┬─────────────┘
                                 │
                                 │ Funciones suspend
                                 ▼
                   ┌──────────────────────────────┐
                   │          REPOSITORY          │
                   │       FilmRepository         │
                   │                              │
                   │ • Centraliza el acceso       │
                   │   a los datos                │
                   │ • Comunica con la API        │
                   │ • Gestiona favoritos         │
                   └──────────────┬───────────────┘
                                  │
                                  │ withContext(Dispatchers.IO)
                                  │
                    ┌─────────────┴─────────────┐
                    │                           │
                    ▼                           ▼
         ┌────────────────────┐     ┌────────────────────┐
         │    RETROFIT        │     │    FILMDBHELPER    │
         │                    │     │                    │
         │    ApiService      │     │    SQLite local    │
         │    RetrofitClient  │     │    Tabla favorites │
         │                    │     │                    │
         │    API REMOTA      │     │    BASE DE DATOS   │
         └────────────────────┘     └────────────────────┘
```

## Cómo funciona

1. **Listado:** al crearse, `MainViewModel` pide `GET /films` (todas las películas en una sola respuesta, sin paginación) y las guarda en `_apiFilms`.
2. **Buscador en tiempo real:** cada tecla avisa al ViewModel, que guarda el texto y filtra la lista ya descargada por título (sin consultar la API de nuevo).
3. **Detalle (Compose):** al tocar una película, se abre `DetailActivity` pasando solo el **id** por `Intent`. `DetailViewModel` pide `GET /films/{id}` y expone `film`, `isLoading` y `errorMessage` como `StateFlow`, que `FilmDetailScreen` observa con `collectAsState()`.
4. **Favoritos (SQLite):** tocar la estrella hace `INSERT` (`db.insert`) o `DELETE` por `id` en la tabla `favorites` de `ghibli_app.db`. Antes de insertar se consulta `isFavorite()`, así nunca se guarda dos veces la misma película. La pantalla de Favoritos funciona sin internet.
