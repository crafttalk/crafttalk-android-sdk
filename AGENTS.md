# CraftTalk Android SDK

`:chat` — Android-библиотека, которую подключают клиентские приложения через JitPack. `:app` — демо-приложение, которое использует эту же библиотеку через `project(":chat")`. Примеры публичного API приведены в [README.md](README.md).

## Где искать код

- `chat/src/main/java/com/crafttalk/chat/initialization` — точки входа SDK. При изменении публичного контракта сверяйте вызовы в README и демо. Сохраняйте совместимость с кодом и данными существующих клиентов.
- В `com.crafttalk.chat` пакет `domain` содержит модели и бизнес-логику, `data` — сетевые клиенты, хранилища и миграции Room, `presentation` — Android UI. Для изменений схемы проверяйте обновление базы с предыдущей версии.
- `app/src/main/java/com/crafttalk/sampleChat` — примеры входа, нативного чата, чата со счётчиком и WebView.

## Локальная сборка

Используйте Gradle Wrapper из корня проекта, JDK 17, Android SDK Platform 34 и Build Tools 34.0.0. Задайте путь к Android SDK через `ANDROID_HOME` или `sdk.dir` в локальном `local.properties`. Минимальная поддерживаемая версия Android — API 21.

| Задача | Команда |
| --- | --- |
| JVM-тесты SDK и демо | `./gradlew :chat:testDebugUnitTest :app:testDebugUnitTest` |
| Покрытие Kotlin-кода SDK его JVM-тестами (HTML и XML) | `./gradlew :chat:jacocoDebugUnitTestReport` |
| Один класс JVM-тестов | `./gradlew :chat:testDebugUnitTest --tests 'com.crafttalk.chat.WebchatClientHashTest'` |
| Android Lint | `./gradlew :chat:lintDebug :app:lintDebug` |
| Собрать SDK и демо | `./gradlew :chat:assembleRelease :app:assembleDebug` |
| Собрать инструментальные тесты | `./gradlew :chat:assembleDebugAndroidTest :app:assembleDebugAndroidTest` |
| Выполнить инструментальные тесты на подключённом устройстве или эмуляторе | `./gradlew :chat:connectedDebugAndroidTest :app:connectedDebugAndroidTest` |

## Как проверять изменения

Для исправления ошибки бизнес-логики воспроизведите её тестом в `src/test` соответствующего модуля и проверьте его после исправления. Для сценариев, которым нужны Android UI или устройство, используйте `src/androidTest`. При изменении способа интеграции SDK соберите демо как приложение-потребитель.

После изменения запросов истории или миграций Room запустите `./gradlew :chat:connectedDebugAndroidTest` на устройстве или эмуляторе. Тесты проверяют счётчик непрочитанных, порядок истории и обновление базы версии 10 с данными.

[Android CI](.github/workflows/ci.yml) на PR в `master` и push в `master` запускает JVM-тесты и lint, затем собирает release AAR, debug APK демо и оба тестовых APK. Выполнение инструментальных сценариев требует отдельного запуска на Android. Отчёты JVM-тестов и lint доступны в артефакте `android-test-reports` и локально в `<модуль>/build/reports/`.

Отчёт JaCoCo входит в тот же артефакт. Локально откройте `chat/build/reports/jacoco/jacocoDebugUnitTestReport/html/index.html`. Отчёт измеряет покрытие Kotlin-кода модуля `:chat` при выполнении `:chat:testDebugUnitTest`. Сгенерированные Java-классы Dagger, Room и View Binding исключены из расчёта.

## Android-тесты перед push

Один раз для клона подключите хук командой `git config --local core.hooksPath .githooks`. При push Gradle сам запускает выделенный эмулятор Pixel 4 с Android API 32, выполняет тесты SDK и демо и выключает тестовый эмулятор. Подключённые телефоны и личные эмуляторы в этом прогоне не участвуют. Для первого запуска нужны Android Emulator и системный образ AOSP API 32 для архитектуры компьютера. Их можно установить через SDK Manager.

Хук запускает тесты SDK и демо во временной копии коммита для каждой отправляемой ветки и каждого тега, указывающего на коммит. Один коммит проверяется один раз за push. Для поиска Android SDK используется `ANDROID_HOME` либо абсолютный `sdk.dir` из локального `local.properties`. Если эмулятор не запустился или тест упал, Git прерывает push. Для каждого неудачного прогона хук создаёт отдельный каталог в `build/reports/pre-push/<коммит>/` и выводит пути к HTML-отчётам, которые Gradle успел сформировать.

Чтобы проверить текущие изменения до коммита на таком же автоматически запускаемом эмуляторе, выполните из корня проекта:

```sh
./gradlew -I .githooks/android-test-device.gradle --max-workers=1 :chat:prePushApi32DebugAndroidTest --rerun :app:prePushApi32DebugAndroidTest --rerun
```

При ручном запуске `connectedDebugAndroidTest` Gradle использует подключённые устройства и удаляет демо `com.crafttalk.sampleChat` вместе с данными после тестов. Для этой команды подключайте только выделенные тестовые устройства.
