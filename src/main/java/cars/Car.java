package cars;

import java.time.Year;

public record Car(String model, int power, int year) {
    public Car {
        if (model == null) {
            throw new IllegalArgumentException("Модель не указана.");
        }
        model = model.strip();
        if (model.isEmpty() || model.length() > 80
                || model.indexOf(';') >= 0
                || model.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Модель: от 1 до 80 символов, без ; и управляющих символов.");
        }
        if (power < 1 || power > 2000) {
            throw new IllegalArgumentException("Мощность должна быть от 1 до 2000 л.с.");
        }
        if (year < 1886 || year > Year.now().getValue() + 1) {
            throw new IllegalArgumentException("Недопустимый год выпуска.");
        }
    }
}
