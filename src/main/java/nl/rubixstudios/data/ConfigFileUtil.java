package nl.rubixstudios.data;

import lombok.Getter;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.nio.file.FileSystems;

/**
 * @author Djorr
 * @created 16/08/2022 - 13:31
 * @project TicketsRMC
 */
public class ConfigFileUtil extends YamlFile {

    @Getter private final YamlFile file;

    public ConfigFileUtil(String name) {
        this.file = new YamlFile(name);

        try {
            if (!this.file.exists()) {
                this.file.createNewFile();
                this.file.options().copyDefaults();
                System.out.println("Succesfully created the file " + this.file.getFilePath() + "\n");
            } else {
                System.out.println(this.file.getFilePath() + " already exists, loading configuration..\n");
            }
            this.file.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
