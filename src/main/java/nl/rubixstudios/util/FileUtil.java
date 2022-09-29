package nl.rubixstudios.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Djorr
 * @created 15/08/2022 - 11:42
 * @project TicketsRMC
 */
public class FileUtil {

    public static File getOrCreateFile(File parent, String name) {
        File file = new File(parent, name);

        if(!parent.exists()) parent.mkdir();

        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    public static File[] getAllFiles(final File parent) {
        List<File> files = new ArrayList<>();

        for (String s : parent.list()) {
            File file = new File(parent, s);

            if(!parent.exists()) parent.mkdir();

            if(!file.exists()) {
                try {
                    file.createNewFile();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            files.add(file);
        }
        return files.toArray(new File[files.size()]);
    }

    public static void writeString(File file, String content) {
        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            writer.write(content);

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static String readWholeFile(File file) {
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            StringBuilder builder = new StringBuilder();

            String line;
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }

            return builder.length() != 0 ? builder.toString() : null;

        } catch(IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Instant creationDate(File file) {
        try {

            BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return attributes.creationTime().toInstant();

        } catch(IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
