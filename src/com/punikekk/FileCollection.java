package com.punikekk;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "filecollection")
public class FileCollection {
    @XmlElements(@XmlElement(name = "location"))
    List<Location> locations;
    @XmlElements(@XmlElement(name = "file"))
    List<File> files;

    public boolean areLocationsEmpty() {
        return locations == null || locations.isEmpty();
    }

    static class Location {
        @XmlAttribute
        String id;
        @XmlAttribute
        String path;

        @Override
        public String toString() {
            return "Location{" +
                    "id='" + id + '\'' +
                    ", path='" + path + '\'' +
                    '}';
        }
    }

    static class File {
        @XmlAttribute
        String hash;
        @XmlAttribute
        String location;
        @XmlAttribute
        String name;
        @XmlAttribute
        String type;

        public boolean isDownloadedAndMatchHash(Path directory) {
            Path filePath = directory.resolve(getFileName());
            if (!filePath.toFile().exists()) return false;

            try {
                String current = DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(Files.readAllBytes(filePath)));
                return hash.substring(0, hash.length() - 2).equalsIgnoreCase(current.substring(0, current.length() - 2));
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        public String getFileName() {
            return name + '.' + type;
        }
    }
}
