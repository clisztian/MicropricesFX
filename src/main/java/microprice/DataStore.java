package microprice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataStore {

    public static ArrayList<Tick> getTickData(String fileName) throws IOException {

        ArrayList<Tick> ticks = new ArrayList<>();


        // Load the file from the resources directory
        ClassLoader classLoader = DataStore.class.getClassLoader();
        Path filePath = null;

        try {
            filePath = Paths.get(classLoader.getResource("data/bac.csv").toURI());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // Read all lines from the file
        List<String> allLines = null;
        try {
            allLines = Files.readAllLines(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }


        for(String line : allLines) {
            ticks.add(new Tick(line));
        }

        return ticks;
    }

}
