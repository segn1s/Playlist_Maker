# 🎵 Playlist Maker

> Учебный Android-проект, написанный с нуля в рамках курса [Яндекс.Практикум](https://practicum.yandex.ru/) — мобильная разработка на Android.

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Min%20SDK-29-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge"/>
</p>

---

## 📲 Скачать

Актуальный APK доступен на странице [**Releases**](../../releases/latest).

---

## 📖 О проекте

Playlist Maker — приложение для поиска треков через iTunes Search API, прослушивания превью и управления собственными плейлистами. Проект охватывает полный цикл разработки Android-приложения: от UI до работы с базой данных и сетью.

---

## 📸 Скриншоты

<p align="center">
  <img src="screenshots/search.png" width="200"/>
  <img src="screenshots/player.png" width="200"/>
  <img src="screenshots/playlists.png" width="200"/>
  <img src="screenshots/playlist_detail.png" width="200"/>
</p>

> Положи скриншоты в папку `screenshots/` в корне репозитория с именами выше.

---

## ✨ Функциональность

### 🔍 Поиск треков
- Поиск по iTunes Search API в реальном времени
- История поиска с возможностью очистки
- Отображение обложки, названия, исполнителя и длительности

### 🎧 Аудиоплеер
- Воспроизведение 30-секундного превью трека
- Прогресс-бар с таймером
- Кнопки «Добавить в избранное» и «Добавить в плейлист»

### 📋 Плейлисты
- Создание плейлиста с названием, описанием и обложкой
- Редактирование существующего плейлиста
- Добавление треков в плейлист прямо из плеера
- Удаление треков и плейлистов
- Шаринг плейлиста текстом в другие приложения
- Суммарная продолжительность треков

---

## 🛠️ Стек технологий

| Слой | Технологии |
|------|-----------|
| **Язык** | Kotlin |
| **Архитектура** | MVVM + Clean Architecture (Data / Domain / Presentation) |
| **DI** | Koin |
| **База данных** | Room (SQLite) |
| **Сеть** | Retrofit + Gson |
| **Навигация** | Navigation Component (Single Activity) |
| **Изображения** | Glide |
| **Асинхронность** | Coroutines + Flow |
| **UI** | ViewBinding, Material Design 3, ConstraintLayout, BottomSheet |
| **Хранилище** | SharedPreferences (настройки), FileSystem (обложки плейлистов) |

---

## 🏗️ Архитектура

```
app/
├── data/
│   ├── db/                  # Room entities, DAOs, AppDatabase
│   ├── network/             # Retrofit, ITunes API
│   └── repository/          # Реализации репозиториев
├── domain/
│   ├── api/                 # Интерфейсы репозиториев и интеракторов
│   ├── impl/                # Реализации интеракторов
│   └── model/               # Доменные модели (Track, Playlist, ...)
├── presentation/
│   ├── media/               # Медиатека: Избранное, Плейлисты
│   │   └── playlists/       # Список, создание, редактирование, экран плейлиста
│   ├── search/              # Поиск треков
│   └── settings/            # Настройки (тема)
├── ui/
│   └── player/              # Аудиоплеер
└── di/
    └── AppModules.kt        # Koin-модули
```

---

## 🚀 Запуск проекта

1. Клонируй репозиторий:
   ```bash
   git clone https://github.com/YOUR_USERNAME/playlist-maker.git
   ```
2. Открой в **Android Studio Hedgehog** или новее
3. Синхронизируй Gradle
4. Запусти на эмуляторе или устройстве с Android 10+ (API 29+)

---

## 📦 Сборка APK

```bash
./gradlew assembleRelease
```

Файл появится в `app/build/outputs/apk/release/`.

---

## 🎓 В рамках курса

Проект разрабатывался поэтапно в рамках учебных спринтов:

1. Вёрстка экранов, навигация, SharedPreferences
2. Сеть — поиск треков через iTunes API, история поиска
3. Аудиоплеер с MediaPlayer
4. Clean Architecture, Koin, рефакторинг
5. Coroutines, Room — избранные треки
6. Плейлисты: создание, добавление треков, экран плейлиста, редактирование, шаринг
