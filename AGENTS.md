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

Один раз для клона подключите хук командой `git config --local core.hooksPath .githooks`. Для инструментальных тестов используйте выделенный эмулятор API 21+ или тестовое устройство с разрешённой USB-отладкой. Выполните `adb devices` и проверьте все устройства со статусом `device`. Gradle может запустить тесты на нескольких устройствах. На каждом из них должно быть допустимо удаление демо и его данных. Gradle устанавливает тестируемую версию `com.crafttalk.sampleChat` и после прогона удаляет этот пакет вместе с локальными данными.

Хук запускает тесты SDK и демо во временной копии коммита для каждой отправляемой ветки и каждого тега, указывающего на коммит. Один коммит проверяется один раз за push. Для поиска Android SDK используется `ANDROID_HOME` либо абсолютный `sdk.dir` из локального `local.properties`. Если подключённое устройство недоступно или тест упал, Git прерывает push. Для каждого неудачного прогона хук создаёт отдельный каталог в `build/reports/pre-push/<коммит>/` и выводит пути к HTML-отчётам, которые Gradle успел сформировать.

Для проверки текущих изменений до коммита запустите `./gradlew :chat:connectedDebugAndroidTest :app:connectedDebugAndroidTest`. Среди тестов демо есть вход в нативный чат с формой регистрации и проверка форматирования номера телефона. Для этого сценария достаточно локального устройства или эмулятора.
