# Mods Memory Viewer

Клиентский мод 1.21.1 и 26.3 для NeoForge и Fabric. 26.3 — отдельный Gradle-проект `versions/26.3`. Общие правила — `../AGENTS.md`.

Свой код, лицензия MPL-2.0. MemoryCatcher не декомпилировать и не копировать: у него All Rights Reserved и нет публичного исходника. Замер — гистограмма классов JVM и индекс `.class` в jar модов. Вложенный jar Fabric с `ModOrigin.Kind.NESTED` относится к своему mod id; `origin.paths` у такого мода бросает `kind NESTED doesn't have paths`. Невостребованный `META-INF/jars` остаётся у родителя. Один уровень, потолок 32 МБ.

Экран — таблица в настройках мода: кнопка Mods. Другого способа открыть её нет: ни команды, ни клавиши. Fabric открывает её через Mod Menu (`modmenu` entrypoint). NeoForge — через `IConfigScreenFactory` на `ModContainer`, только при `FMLEnvironment.getDist() == Dist.CLIENT` на 26.3 и `FMLEnvironment.dist` на 1.21.1. Крестик справа сверху вызывает `onClose`. Скроллбар — `TableScroll`: перетаскивание ползунка и клик по дорожке. Строки отсортированы по байтам, в строке МиБ и процент. `-Dmodsmemoryviewer.open=true` открывает тот же экран один раз на заголовке, это только запуск для проверки.

Fabric: `environment: client`, точки входа `client` и `modmenu`. NeoForge на выделенном сервере загружается и не открывает экран. McnCDN из этого репозитория не публикуется.

## Modrinth

На Modrinth у каждой пары версии Minecraft и загрузчика одна версия: самая новая. Релиз снимает прежние версии этого слота и ставит на их место текущий jar.
