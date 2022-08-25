package microprice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataStore {

    public static ArrayList<Tick> getTickData(String fileName) throws IOException {

        ArrayList<Tick> ticks = new ArrayList<>();

        List<String> allLines = Files.readAllLines(Paths.get(fileName));

        for(String line : allLines) {
            ticks.add(new Tick(line));
        }

        return ticks;
    }

}
