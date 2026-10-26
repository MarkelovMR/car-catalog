package cars;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

public class CarConsole {
    private final Garage garage;
    private final BufferedReader input;
    private final PrintWriter output;

    public CarConsole(Garage garage, BufferedReader input, PrintWriter output) {
        this.garage = garage;
        this.input = input;
        this.output = output;
    }

    public void run() throws IOException {
        while (true) {
            output.println("""

                    Каталог автомобилей
                    1. Добавить автомобиль
                    2. Загрузить файл
                    3. Подобрать по мощности
                    4. Показать весь список
                    5. Получить запись по номеру
                    6. Удалить запись по номеру
                    0. Выход""");
            try {
                switch (read("Ваш выбор: ").strip()) {
                    case "1" -> addCar();
                    case "2" -> loadFile();
                    case "3" -> showReport();
                    case "4" -> showAll();
                    case "5" -> output.println(Garage.describe(garage.get(readIndex())));
                    case "6" -> output.println("Удалён: " + Garage.describe(garage.remove(readIndex())));
                    case "0" -> {
                        output.println("До свидания!");
                        output.flush();
                        return;
                    }
                    default -> output.println("Такого пункта нет.");
                }
            } catch (EOFException e) {
                output.println("\nВвод завершён.");
                output.flush();
                return;
            } catch (NumberFormatException e) {
                output.println("Ошибка: нужно ввести целое число.");
            } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
                output.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private String read(String prompt) throws IOException {
        output.print(prompt);
        output.flush();
        String value = input.readLine();
        if (value == null) {
            throw new EOFException();
        }
        return value;
    }

    private void addCar() throws IOException {
        String model = read("Модель: ");
        int power = Integer.parseInt(read("Мощность (л.с.): ").strip());
        int year = Integer.parseInt(read("Год выпуска: ").strip());
        garage.add(new Car(model, power, year));
        output.println("Автомобиль добавлен. Всего записей: " + garage.size());
    }

    private void loadFile() throws IOException {
        String filename = read("Путь к CSV или TXT: ");
        try {
            CarImporter.Result result = CarImporter.load(Path.of(filename), garage);
            result.errors().forEach(output::println);
            output.println("Загружено: " + result.added());
        } catch (IOException e) {
            output.println("Не удалось загрузить файл: " + e.getMessage());
        }
    }

    private int readIndex() throws IOException {
        int number = Integer.parseInt(read("Номер записи (от 1 до " + garage.size() + "): ").strip());
        if (number < 1 || number > garage.size()) {
            throw new IllegalArgumentException("Записи с таким номером нет.");
        }
        return number - 1;
    }

    private void showAll() {
        if (garage.size() == 0) {
            output.println("Список пуст.");
        }
        for (int i = 0; i < garage.size(); i++) {
            output.println((i + 1) + ". " + Garage.describe(garage.get(i)));
        }
    }

    private void showReport() throws IOException {
        int minimum = Integer.parseInt(read("Минимальная мощность (л.с.): ").strip());
        List<String> result = garage.report(minimum);
        if (result.isEmpty()) {
            output.println("Подходящих автомобилей нет.");
        } else {
            output.println("Подборка по возрастанию года выпуска:");
            result.forEach(output::println);
        }
    }
}
