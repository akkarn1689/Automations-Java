import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

public class FileMover {

    private static final String SRC_DIR = "C:/Users/Ashish Karn/Downloads";
    private static final String DEST_DIR_SFX = "C:/Users/Ashish Karn/Downloads/Sound";
    private static final String DEST_DIR_MUSIC = "C:/Users/Ashish Karn/Downloads/Sound/music";
    private static final String DEST_DIR_VIDEO = "C:/Users/Ashish Karn/Downloads/Video";
    private static final String DEST_DIR_IMAGE = "C:/Users/Ashish Karn/Downloads/Images";
    private static final String DEST_DIR_DOCS = "C:/Users/Ashish Karn/Downloads/docs";
    private static final String DEST_DIR_INSTALLERS = "C:/Users/Ashish Karn/Downloads/Installers";
    private static final String DEST_DIR_ZIPS = "C:/Users/Ashish Karn/Downloads/Compressed Files";

    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff");
    private static final List<String> VIDEO_EXTENSIONS = Arrays.asList(".webm", ".mpg", ".mp4", ".avi", ".mov");
    private static final List<String> AUDIO_EXTENSIONS = Arrays.asList(".m4a", ".flac", ".mp3", ".wav", ".aac");
    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(".doc", ".docx", ".pdf", ".xls", ".xlsx", ".ppt", ".pptx");
    private static final List<String> INSTALLER_EXTENSIONS = Arrays.asList(".exe", ".msi", ".msix");
    private static final List<String> ZIP_EXTENSIONS = Arrays.asList(".zip", ".rar", ".7z", ".tar.gz");

    public static void main(String[] args) {
        try {
            // to move existing files in the source directory to the destination directories before setting up the watch service
            moveExistingFiles();

            // watch service for new files that is added to the SRC
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(SRC_DIR);
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changedFile = path.resolve((Path) event.context());
                    moveFileBasedOnType(changedFile);
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void moveExistingFiles() throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(SRC_DIR))) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    moveFileBasedOnType(entry);
                }
            }
        }
    }

    private static void moveFileBasedOnType(Path filePath) throws IOException {
        String fileName = filePath.getFileName().toString().toLowerCase();
        BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);

        if (fileNameMatches(fileName, AUDIO_EXTENSIONS)) {
            String destDir = (attr.size() < 10_000_000 || fileName.contains("sfx")) ? DEST_DIR_SFX : DEST_DIR_MUSIC;
            moveFile(filePath, destDir);
        } else if (fileNameMatches(fileName, VIDEO_EXTENSIONS)) {
            moveFile(filePath, DEST_DIR_VIDEO);
        } else if (fileNameMatches(fileName, IMAGE_EXTENSIONS)) {
            moveFile(filePath, DEST_DIR_IMAGE);
        } else if (fileNameMatches(fileName, DOCUMENT_EXTENSIONS)) {
            moveFile(filePath, DEST_DIR_DOCS);
        } else if (fileNameMatches(fileName, INSTALLER_EXTENSIONS)) {
            moveFile(filePath, DEST_DIR_INSTALLERS);
        } else if (fileNameMatches(fileName, ZIP_EXTENSIONS)) {
            moveFile(filePath, DEST_DIR_ZIPS);
        }
    }

    private static boolean fileNameMatches(String fileName, List<String> extensions) {
        String lowerCaseFileName = fileName.toLowerCase();
        return extensions.stream().anyMatch(lowerCaseFileName::endsWith);
    }

    private static void moveFile(Path source, String destinationDir) throws IOException {
        Path destinationPath = Paths.get(destinationDir, source.getFileName().toString());
        if (Files.exists(destinationPath)) {
            destinationPath = makeUnique(destinationPath);
        }
        Files.move(source, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Moved file: " + source.getFileName() + " to " + destinationDir);
    }

    private static Path makeUnique(Path path) {
        String fileName = path.getFileName().toString();
        String extension = "";
        String baseName = fileName;

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            baseName = fileName.substring(0, lastDotIndex);
            extension = fileName.substring(lastDotIndex);
        }

        int counter = 1;
        Path newPath;
        do {
            newPath = path.resolveSibling(baseName + "(" + counter + ")" + extension);
            counter++;
        } while (Files.exists(newPath));

        return newPath;
    }
}
