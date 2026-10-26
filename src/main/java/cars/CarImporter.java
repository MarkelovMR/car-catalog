package cars;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CarImporter {
    private CarImporter() {
    }

    public record Result(int added, List<String> errors) {
    }

    public static Result load(Path path, Garage garage) throws IOException {
        List<Car> loaded = new ChainList<>();
        List<String> errors = new ChainList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header != null && header.startsWith("\uFEFF")) {
                header = header.substring(1);
            }
            if (header == null || !header.strip().equals("model;power;year")) {
                throw new IOException("Первая строка файла должна быть model;power;year.");
            }
            int number = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                number++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    String[] fields = line.split(";", -1);
                    if (fields.length != 3) {
                        throw new IllegalArgumentException("Ожидаются три поля через ;.");
                    }
                    loaded.add(new Car(fields[0], Integer.parseInt(fields[1].strip()),
                            Integer.parseInt(fields[2].strip())));
                } catch (NumberFormatException e) {
                    errors.add("Строка " + number + ": мощность и год должны быть целыми числами.");
                } catch (IllegalArgumentException e) {
                    errors.add("Строка " + number + ": " + e.getMessage());
                }
            }
        }
        // Добавляем записи только после успешного чтения и закрытия файла.
        loaded.forEach(garage::add);
        return new Result(loaded.size(), List.copyOf(errors));
    }
}
