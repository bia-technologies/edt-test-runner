# Здесь собраны заметки связанные с разработкой плагинов под Eclipse и EDT

## Особенности EDT

1. Документация вендора https://edt.1c.ru/dev/ru/
2. Если необходимо расширить функциональность связанную с внутренней (internal) реализацией необходимо создавать проекты-фрагменты
3. Inject
4. Активатор для 1с https://github.com/1C-Company/dt-example-plugins/blob/07c7ec1a6f461f1118079276e574dfe6fa9fcca5/bundles/org.example.ui/src/org/example/ui/Activator.java

## Плагины Eclipse

1. Интерфейсы создаются с помощью SWT и jFace, есть дизайнер для создания с помощью мыши
2. Чтоб отладить плагин его необходимо положить в каталог Dropins - https://wiki.eclipse.org/Equinox/p2/Getting_Started#Supported_dropins_formats
3. Локализация и строковые переменные

