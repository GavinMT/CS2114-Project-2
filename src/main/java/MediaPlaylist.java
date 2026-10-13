import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MediaPlaylist {
    private static String DIR_NAME;


    public MediaPlaylist(String name) {
        DIR_NAME = name;
        makeDir(name);
    }

    public String makeDir(String name) {
        String userHome = System.getProperty("user.home");
        Path playlistDirectory = Paths.get(userHome, name);

        try {
            Files.createDirectory(playlistDirectory);
            return "Created directory at " + playlistDirectory;
        } catch (IOException e) {
            return "Failed to create directory: " + e.getMessage();
        }
    }


}
