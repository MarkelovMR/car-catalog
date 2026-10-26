package cars;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try (BufferedReader input = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            PrintWriter output = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
            new CarConsole(new Garage(), input, output).run();
        } catch (IOException e) {
            System.err.println("Ошибка чтения: " + e.getMessage());
        }
    }
}
