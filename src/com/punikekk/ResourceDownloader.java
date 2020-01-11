package com.punikekk;

import javax.xml.bind.JAXBContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class ResourceDownloader {
    private static final String MAIN_LINK = "http://test2.darkorbit.bigpoint.com/spacemap/";
    private static final String RESOURCES_2D = MAIN_LINK + "xml/resources.xml";
    private static final String RESOURCES_3D = MAIN_LINK + "xml/resources_3d.xml";
    private static final String PARTICLES_3D = MAIN_LINK + "xml/resources_3d_particles.xml";

    public static void main(String[] args) throws Exception {
        chooseResources();
        System.out.println("Download complete!");
    }

    private static void chooseResources() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Type `1` to download 2D resources.");
        System.out.println("Type `2` to download 3D resources.");
        System.out.println("Type `3` to download 2D & 3D resources.");
        if (scanner.hasNextInt()) chooseResources(scanner.nextInt());
        else {
            System.out.println("Type correct number!");
            chooseResources();
        }
    }

    private static void chooseResources(int input) throws Exception {
        switch (input) {
            case 1:
                getResources(RESOURCES_2D, "resources");
                break;
            case 2:
                getResources(RESOURCES_3D, "resources_3d");
                getResources(PARTICLES_3D, "resources_3d");
                break;
            case 3:
                getResources(RESOURCES_2D, "resources");
                getResources(RESOURCES_3D, "resources_3d");
                getResources(PARTICLES_3D, "resources_3d");
                break;
            default:
                System.out.println("Choose proper number!");
                chooseResources();
        }
    }

    private static void getResources(String link, String directory) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(FileCollection.class);
        FileCollection fileCollection = (FileCollection) jaxbContext.createUnmarshaller().unmarshal(new URL(link).openStream());

        Path path = new File(directory).toPath();
        if (Files.notExists(path)) Files.createDirectory(path);

        fileCollection.files.forEach(file -> fileCollection.locations.stream()
                .filter(location -> location.id.equals(file.location)).findFirst()
                .ifPresent(location -> downloadAndCreateFile(file, location.path, path)));
    }

    private static void downloadAndCreateFile(FileCollection.File resourceFile, String urlPath, Path path) {
        Path directory = path.resolve(resourceFile.location);

        if (resourceFile.isDownloadedAndMatchHash(directory)) {
            System.out.println(resourceFile.getFileName() + " skipped, is already downloaded and up to date!");
            return;
        }

        try (InputStream in = new URL(MAIN_LINK + urlPath + resourceFile.getFileName()).openStream()) {
            if (Files.notExists(directory)) Files.createDirectory(directory);
            Files.copy(in, directory.resolve(resourceFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            System.out.println(resourceFile.getFileName() + " downloaded!");
        } catch (IOException e) {
            System.out.println("ERROR! with downloading file: " + resourceFile.getFileName());
            e.printStackTrace();
        }
    }
}