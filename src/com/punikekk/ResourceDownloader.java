package com.punikekk;

import javax.xml.bind.JAXBContext;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceDownloader {
    private static final Pattern LANG_PATTERN = Pattern.compile("lang=([^\"]*)'");
    private static final String MAIN_LINK = "http://test2.darkorbit.bigpoint.com/spacemap/";
    private static final String RESOURCES_2D = MAIN_LINK + "xml/resources.xml";
    private static final String RESOURCES_3D = MAIN_LINK + "xml/resources_3d.xml";
    private static final String PARTICLES_3D = MAIN_LINK + "xml/resources_3d_particles.xml";
    private static final String LANG_RESOURCES = MAIN_LINK + "templates/language_{0}.xml";
    private static final String DARKORBIT_URL = "https://www.darkorbit.com";

    public static void main(String[] args) throws Exception {
        chooseResources();
        System.out.println("Download complete!");
    }

    private static void chooseResources() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Type `1` to download 2D resources.");
        System.out.println("Type `2` to download 3D resources.");
        System.out.println("Type `3` to download language resources.");
        System.out.println("Type `4` to download 2D, 3D & lang resources.");
        if (scanner.hasNextInt()) chooseResources(scanner.nextInt());
        else {
            System.out.println("Type correct number!");
            chooseResources();
        }
    }

    private static void chooseResources(int option) throws Exception {
        switch (option) {
            case 1:
                getResources(RESOURCES_2D, "resources");
                break;
            case 2:
                getResources(RESOURCES_3D, "resources_3d");
                getResources(PARTICLES_3D, "resources_3d");
                break;
            case 3:
                getResourcesByLang();
                break;
            case 4:
                getResources(RESOURCES_2D, "resources");
                getResources(RESOURCES_3D, "resources_3d");
                getResources(PARTICLES_3D, "resources_3d");
                getResourcesByLang();
                break;
            default:
                System.out.println("Choose proper number!");
                chooseResources();
        }
    }

    private static void getResourcesByLang() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(DARKORBIT_URL).openStream()))) {
            br.lines()
                    .map(LANG_PATTERN::matcher)
                    .filter(Matcher::find)
                    .map(m -> m.group(1))
                    .forEach(lang -> {
                        try {
                            getResources(MessageFormat.format(LANG_RESOURCES, lang), "resources_lang");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void getResources(String link, String directory) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(FileCollection.class);
        FileCollection fileCollection = (FileCollection) jaxbContext.createUnmarshaller().unmarshal(new URL(link).openStream());

        Path path = new File(directory).toPath();
        if (Files.notExists(path)) Files.createDirectory(path);

        fileCollection.files.forEach(file -> {
            if (fileCollection.areLocationsEmpty()) downloadAndCreateFile(file, "templates/" + file.location + '/', path);
            else fileCollection.locations.stream()
                    .filter(location -> location.id.equals(file.location))
                    .findFirst().ifPresent(location -> downloadAndCreateFile(file, location.path, path));
        });
    }

    private static void downloadAndCreateFile(FileCollection.File resourceFile, String urlPath, Path path) {
        Path directory = path.resolve(resourceFile.location);
        String pathWithName = resourceFile.location + '/' + resourceFile.getFileName();

        if (resourceFile.isDownloadedAndMatchHash(directory)) {
            System.out.println(pathWithName + " skipped, is already downloaded and up to date!");
            return;
        }

        try (InputStream in = new URL(MAIN_LINK + urlPath + resourceFile.getFileName()).openStream()) {
            if (Files.notExists(directory)) Files.createDirectory(directory);
            Files.copy(in, directory.resolve(resourceFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            System.out.println(pathWithName + " downloaded!");
        } catch (IOException e) {
            System.out.println("ERROR! with downloading file: " + pathWithName);
            e.printStackTrace();
        }
    }
}