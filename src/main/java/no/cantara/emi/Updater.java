package no.cantara.emi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Updater {
    private static void copyFolder(Path source, Path destination) {
        List<Path> sources = null;
        try {
            sources = Files.walk(source).collect(toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Path> destinations = sources.stream()
                .map(source::relativize)
                .map(destination::resolve)
                .collect(toList());

        for (int i = 0; i < sources.size(); i++) {
            try {
                if (!destinations.get(i).toFile().isDirectory()) {
                    Files.copy(sources.get(i), destinations.get(i), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
