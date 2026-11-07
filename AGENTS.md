# Mods Memory Viewer

Клиентский мод 1.21.1 и 26.3 для NeoForge и Fabric. 26.3 — отдельный Gradle-проект `versions/26.3`. Общие правила — `../AGENTS.md`.

Свой код, лицензия MPL-2.0. MemoryCatcher не декомпилировать и не копировать: у него All Rights Reserved и нет публичного исходника. Замер — гистограмма классов JVM и индекс `.class` в jar модов. Вложенный jar Fabric с `ModOrigin.Kind.NESTED` относится к своему mod id; `origin.paths` у такого мода бросает `kind NESTED doesn't have paths`. Невостребованный `META-INF/jars` остаётся у родителя. Один уровень, потолок 32 МБ.

Экран — таблица в настройках мода: кнопка Mods. Fabric открывает её через Mod Menu (`modmenu` entrypoint, `modCompileOnly` и `modLocalRuntime` 11.0.4). NeoForge — через `IConfigScreenFactory` на `ModContainer`, только при `Dist.CLIENT`. Строки отсортированы по байтам, в строке МиБ и процент.

Fabric: `environment: client`, точки входа `client` и `modmenu`. NeoForge на выделенном сервере загружается и не открывает экран. McnCDN из этого репозитория не публикуется.
