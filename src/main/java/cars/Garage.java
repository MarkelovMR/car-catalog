package cars;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Garage {
    private final List<Car> cars = new ChainList<>();

    public void add(Car car) {
        cars.add(Objects.requireNonNull(car));
    }

    public Car get(int index) {
        return cars.get(index);
    }

    public Car remove(int index) {
        return cars.remove(index);
    }

    public int size() {
        return cars.size();
    }

    public List<String> report(int minimumPower) {
        if (minimumPower < 1 || minimumPower > 2000) {
            throw new IllegalArgumentException("Порог мощности: от 1 до 2000 л.с.");
        }
        return cars.stream()
                .filter(car -> car.power() >= minimumPower)
                .sorted(Comparator.comparingInt(Car::year)
                        .thenComparing(Car::model)
                        .thenComparingInt(Car::power))
                .map(Garage::describe)
                .toList();
    }

    public static String describe(Car car) {
        return car.model() + " | " + car.power() + " л.с. | " + car.year() + " г.";
    }
}
