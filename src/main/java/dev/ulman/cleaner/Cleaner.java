package dev.ulman.cleaner;

import java.io.*;
import java.nio.file.NotDirectoryException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.sun.jna.platform.FileUtils;

public class Cleaner {

    private String mainPath;
    private String fileToSave = "app.servers.config";
    private List<String> foldersNamesToSave;
    private FileUtils fileUtils = FileUtils.getInstance();
    private char mode;

    public void mainMethod() throws NotDirectoryException{
        loadConfig();

        if (!mainPath.endsWith("\\"))
            mainPath += "\\";

        System.out.print("podaj nazwe folderu do wyczyszcenia " + mainPath);
        Scanner scanner = new Scanner(System.in);

    File directoryToClean = new File(mainPath + scanner.nextLine());

        if (!directoryToClean.exists())
                throw new NotDirectoryException("Directory does not exist");
        if (!directoryToClean.isDirectory())
                throw new NotDirectoryException("This is not directory");

        if (mode == 'd') {
        System.out.println("mode: permanent");
        fileDelete(directoryToClean);
    } else if (mode == 's') {
        System.out.println("mode: save");
        saveFileDelete(directoryToClean);
    } else {
        System.out.println("Error: Unknown mode");
    }
}

    private void fileDelete(File directoryToClean){

        Arrays.stream(directoryToClean.listFiles())
                .forEach(file -> {
                    //delete files
                    if (file.isFile() && !file.getName().equals(fileToSave)) {
                        System.out.println("DELETE: " + file);
                        file.delete();
                    }
                    //enter to folder
                    if (file.isDirectory()) fileDelete(file);

                    //delete folders
                    if (file.isDirectory() && file.listFiles().length == 0) {
                        if(foldersNamesToSave
                                .stream()
                                .parallel()
                                .noneMatch(folder -> folder.equals(file.getName()))) {
                            System.out.println("DELETE DIRECTORY: " + file);
                            file.delete();
                        }
                    }
                });
    }

    private void saveFileDelete(File directoryToClean) {
        Arrays.stream(directoryToClean.listFiles())
                .forEach(file -> {
                            //delete files
                            if (file.isFile() && !file.getName().equals(fileToSave)) {
                                System.out.println("DELETE: " + file);
                                try {
                                    fileUtils.moveToTrash(file);
                                } catch (IOException e) {
                                    System.out.println("ERROR :/:/");
                                    e.printStackTrace();
                                }
                            }
                    //enter to folder
                    if (file.isDirectory()) fileDelete(file);

                    //delete folders
                    if (file.isDirectory() && file.listFiles().length == 0) {

                      if(foldersNamesToSave
                                .stream()
                                .noneMatch(folder -> folder.equals(file.getName()))) {
                          System.out.println("DELETE DIRECTORY: " + file);
                          try {
                              fileUtils.moveToTrash(file);
                          } catch (IOException e) {
                              System.out.println("Error :(:(");
                              e.printStackTrace();
                          }
                      }
                    }
                });
    }

    private void loadConfig() {
        try {
            InputStream in = getClass().getResourceAsStream("/config");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                if (lineNumber == 1)
                    mainPath = line.toLowerCase();
                if (lineNumber == 3)
                    mode = line.toLowerCase().trim().charAt(0);
                if (lineNumber == 5)
                    foldersNamesToSave = Arrays.asList(line.split(","))
                            .stream()
                            .map(name -> name.trim())
                            .collect(Collectors.toList());
                lineNumber++;
            }
            reader.close();
            in.close();
        } catch (Exception e) {
            System.out.println("Config file is not exist or has bad syntax!");
        }
    }
}
