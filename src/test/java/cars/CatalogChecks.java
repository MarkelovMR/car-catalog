package cars;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public final class CatalogChecks {
    private static int checks;

    private CatalogChecks() {
    }

    public static void main(String[] args) throws Exception {
        checkList();
        checkCarsAndReport();
        checkImport();
        checkConsole();
        System.out.println("Проверок пройдено: " + checks);
    }

    private static void checkList() {
        ChainList<Integer> actual = new ChainList<>();
        List<Integer> expected = new ArrayList<>();
        Random random = new Random(10825);
        for (int step = 0; step < 1500; step++) {
            int operation = random.nextInt(5);
            if (expected.isEmpty() || operation < 2) {
                int index = random.nextInt(expected.size() + 1);
                Integer value = step % 13 == 0 ? null : random.nextInt(1000);
                actual.add(index, value);
                expected.add(index, value);
            } else if (operation == 2) {
                int index = random.nextInt(expected.size());
                equal(actual.remove(index), expected.remove(index));
            } else if (operation == 3) {
                int index = random.nextInt(expected.size());
                equal(actual.set(index, step), expected.set(index, step));
            } else {
                equal(actual.get(expected.size() - 1), expected.get(expected.size() - 1));
            }
            equal(actual.size(), expected.size());
            equal(actual, expected);
            equal(actual.stream().toList(), expected);
        }
        Iterator<Integer> iterator = actual.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        equal(actual.size(), 0);
        fails(NoSuchElementException.class, iterator::next);
        fails(IllegalStateException.class, iterator::remove);
        fails(IndexOutOfBoundsException.class, () -> actual.get(0));
        fails(IndexOutOfBoundsException.class, () -> actual.get(-1));
        fails(IndexOutOfBoundsException.class, () -> actual.remove(0));
        fails(IndexOutOfBoundsException.class, () -> actual.add(1, 5));
        actual.add(7);
        equal(actual.get(0), 7);
        equal(actual.remove(0), 7);
        actual.add(8);
        actual.add(9);
        Iterator<Integer> stale = actual.iterator();
        actual.add(10);
        fails(ConcurrentModificationException.class, stale::next);
        actual.clear();
        actual.add(11);
        equal(actual.get(0), 11);
        equal(actual.size(), 1);
        Iterator<Integer> single = actual.iterator();
        equal(single.next(), 11);
        single.remove();
        equal(actual.size(), 0);
        fails(IllegalStateException.class, single::remove);

        actual.addAll(List.of(1, 2, 3));
        var cursor = actual.listIterator(1);
        equal(cursor.next(), 2);
        cursor.set(20);
        cursor.add(21);
        equal(cursor.previous(), 21);
        cursor.remove();
        equal(actual, List.of(1, 20, 3));
    }

    private static void checkCarsAndReport() {
        equal(new Car("  Ford Focus  ", 125, 2016).model(), "Ford Focus");
        fails(IllegalArgumentException.class, () -> new Car(" ", 100, 2020));
        fails(IllegalArgumentException.class, () -> new Car(null, 100, 2020));
        fails(IllegalArgumentException.class, () -> new Car("A;B", 100, 2020));
        fails(IllegalArgumentException.class, () -> new Car("A\nB", 100, 2020));
        fails(IllegalArgumentException.class, () -> new Car("A".repeat(81), 100, 2020));
        fails(IllegalArgumentException.class, () -> new Car("Test", 0, 2020));
        fails(IllegalArgumentException.class, () -> new Car("Test", 2001, 2020));
        fails(IllegalArgumentException.class, () -> new Car("Test", 100, 1885));
        fails(IllegalArgumentException.class,
                () -> new Car("Test", 100, Year.now().getValue() + 2));
        Garage garage = new Garage();
        Car newer = new Car("Skoda Octavia", 150, 2020);
        garage.add(newer);
        garage.add(new Car("Lada Vesta", 106, 2021));
        garage.add(new Car("Ford Focus", 125, 2016));
        garage.add(new Car("Toyota Corolla", 122, 2019));
        equal(garage.report(120), List.of("Ford Focus | 125 л.с. | 2016 г.",
                "Toyota Corolla | 122 л.с. | 2019 г.", "Skoda Octavia | 150 л.с. | 2020 г."));
        equal(garage.get(0), newer);
        equal(garage.report(150), List.of("Skoda Octavia | 150 л.с. | 2020 г."));
        equal(garage.report(151), List.of());
        fails(IllegalArgumentException.class, () -> garage.report(-10));
        equal(garage.remove(0), newer);
        equal(garage.size(), 3);
    }

    private static void checkImport() throws Exception {
        Path directory = Files.createTempDirectory("cars-check-");
        Path file = directory.resolve("данные с пробелами.txt");
        try {
            Files.writeString(file, "\uFEFFmodel;power;year\n\nМодель А;100;2000\n"
                    + ";100;2000\nМодель Б;abc;2020\nЛишнее;1;2;3\n"
                    + "Модель В;125;2018\n", StandardCharsets.UTF_8);
            Garage garage = new Garage();
            CarImporter.Result result = CarImporter.load(file, garage);
            equal(result.added(), 2);
            equal(result.errors().size(), 3);
            equal(result.errors().get(0).startsWith("Строка 4:"), true);
            equal(garage.get(1).model(), "Модель В");
            Files.writeString(file, "wrong;header\nTest;100;2020\n");
            fails(java.io.IOException.class, () -> CarImporter.load(file, garage));
            equal(garage.size(), 2);
            Files.writeString(file, "");
            fails(java.io.IOException.class, () -> CarImporter.load(file, garage));
            fails(java.io.IOException.class,
                    () -> CarImporter.load(directory.resolve("missing.csv"), garage));
            equal(garage.size(), 2);
            Garage sample = new Garage();
            equal(CarImporter.load(Path.of("data/cars.csv"), sample).added(), 5);
            equal(sample.size(), 5);
            equal(CarImporter.load(Path.of("data/cars-with-errors.csv"), sample).errors().size(), 5);
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(directory);
        }
    }

    private static void checkConsole() throws Exception {
        Garage garage = new Garage();
        String all = console(garage,
                "1\nFord Focus\n125\n2016\n4\n5\n1\n3\n120\n6\n1\n4\n0\n");
        contains(all, "Автомобиль добавлен");
        contains(all, "Ford Focus | 125 л.с. | 2016 г.");
        contains(all, "Подборка по возрастанию года выпуска");
        contains(all, "Удалён:");
        contains(all, "Список пуст.");
        equal(garage.size(), 0);
        String invalid = console(new Garage(),
                "no\n1\nCar\nabc\n5\n1\n3\n0\n2\nmissing.csv\n0\n");
        contains(invalid, "Такого пункта нет.");
        contains(invalid, "нужно ввести целое число");
        contains(invalid, "Записи с таким номером нет.");
        contains(invalid, "Порог мощности");
        contains(invalid, "Не удалось загрузить файл");
        contains(console(new Garage(), "1\nSome car\n"), "Ввод завершён.");
        Garage imported = new Garage();
        contains(console(imported, "2\ndata/cars.csv\n0\n"), "Загружено: 5");
        equal(imported.size(), 5);
    }

    private static String console(Garage garage, String input) throws Exception {
        StringWriter result = new StringWriter();
        new CarConsole(garage, new BufferedReader(new StringReader(input)),
                new PrintWriter(result, true)).run();
        return result.toString();
    }

    private static void contains(String text, String fragment) {
        equal(text.contains(fragment), true);
    }

    private static void equal(Object actual, Object expected) {
        checks++;
        if (!java.util.Objects.equals(actual, expected)) {
            throw new AssertionError("Ожидалось: " + expected + ", получено: " + actual);
        }
    }

    private static void fails(Class<? extends Throwable> type, Action action) {
        checks++;
        try {
            action.run();
        } catch (Throwable error) {
            if (type.isInstance(error)) {
                return;
            }
            throw new AssertionError("Ожидалось исключение " + type.getSimpleName(), error);
        }
        throw new AssertionError("Нет ожидаемого исключения " + type.getSimpleName());
    }

    @FunctionalInterface
    private interface Action {
        void run() throws Exception;
    }
}
