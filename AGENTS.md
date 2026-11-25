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
