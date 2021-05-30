package sillygit.util;

import app.AppConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FileUtils {

    /**
     * Checks if the given path is a directory.
     * @param rootDirectory Directory where to look for the path.
     * @param path Path that needs to be checked.
     * @return true if the path is a directory, false if it is a file.
     */
    public static boolean isPathDirectory(String rootDirectory, String path) {

        File f = new File(rootDirectory + "\\" + path);

        return f.isDirectory();

    }

    /**
     * Checks if the given path is a file.
     * @param rootDirectory Directory where to look for the path.
     * @param path Path that needs to be checked.
     * @return true if the path is a file, false if it is a dirctory.
     */
    public static boolean isPathFile(String rootDirectory, String path) {

        File f = new File(rootDirectory + "\\" + path);

        return f.isFile();

    }

    /**
     * Reads the file and returns all of the needed information.
     * @param rootDirectory Directory where to look for the file.
     * @param path Path to the file relative to it's root directory.
     * @return FileInfo object for the specified file if it exists, null if it doesn't or
     * if an error has been encountered.
     */
    public static FileInfo getFileInfoFromPath(String rootDirectory, String path) {

        path = rootDirectory + "\\" + path;
        File f = new File(path);
        if (!f.exists()) {
            AppConfig.timestampedErrorPrint("File " + path + " doesn't exist.");
            return null;
        }

        if (f.isDirectory()) {
            AppConfig.timestampedErrorPrint(path + " is a directory and not a file.");
            return null;
        }

        try {
            String filePath = path.replace(rootDirectory + "\\", "");

            BufferedReader reader = new BufferedReader(new FileReader(f));
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line + "\n");
            }
            reader.close();

            if (!fileContent.isEmpty())
                fileContent.deleteCharAt(fileContent.length() - 1);

            return new FileInfo(filePath, fileContent.toString(), 0);
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint("Couldn't read " + path + ".");
        }

        return null;

    }

    /**
     * Returns a list of FileInfo objects for the specified directory and all of it's
     * sub-directories and files within.
     * @param rootDirectory Directory where to look for the file.
     * @param path Path of the directory relative to it's root directory.
     * @return List containing all of the FileInfo objects. If none were found or
     * couldn't be read, the list will be empty.
     */
    public static List<FileInfo> getDirectoryInfoFromPath(String rootDirectory, String path) {

        List<FileInfo> fileInfoList = new ArrayList<>();

        path = rootDirectory + "\\" + path;
        File f = new File(path);
        if (!f.exists()) {
            AppConfig.timestampedErrorPrint("Directory " + path + " doesn't exist.");
            return fileInfoList;
        }

        if (f.isFile()) {
            AppConfig.timestampedErrorPrint(path + " is a file and not a directory.");
            return fileInfoList;
        }

        Queue<String> dirs = new LinkedList<>();
        dirs.add(path);

        while (!dirs.isEmpty()) {
            String dirPath = dirs.poll();
            List<String> subFiles = new ArrayList<>();

            File dir = new File(dirPath);
            for (File file : dir.listFiles()) {
                String filePath = file.getPath().replace(rootDirectory + "\\", "");
                subFiles.add(filePath);

                if (file.isFile()) {
                    FileInfo fileInfo = getFileInfoFromPath(rootDirectory, filePath);
                    if (fileInfo != null) {
                        fileInfoList.add(fileInfo);
                    }
                } else {
                    dirs.add(file.getPath());
                }
            }

            dirPath = dirPath.replace(rootDirectory + "\\", "");
            fileInfoList.add(new FileInfo(dirPath, subFiles));
        }

        return fileInfoList;

    }

    /**
     * Stores a file in the specified root directory.
     * @param rootDirectory Directory where to store the actual file.
     * @param fileInfo All relevant information about the file.
     * @param storeVersion Specifies whether the version needs to be stored as well.
     *                     Should be true when storing files in permanent storage and
     *                     false when retrieving them into the working directory.
     * @return true if the file was stored successfully, false otherwise.
     */
    public static boolean storeFile(String rootDirectory, FileInfo fileInfo, boolean storeVersion) {

        //Prvo napravimo sve direktorijume iznad ovog fajla ako ih ima
        if (fileInfo.getPath().contains("\\")) {
            String dirPath = rootDirectory + "\\" + fileInfo.getPath().substring(0, fileInfo.getPath().lastIndexOf('\\'));
            File dir = new File(dirPath);
            dir.mkdirs();
        }

        String filePath = rootDirectory + "\\" + fileInfo.getPath();
        if (storeVersion)
            filePath = filePath + "." + fileInfo.getVersion();

        File f = new File(filePath);
        try {
            f.createNewFile();

            PrintWriter fileWriter = new PrintWriter(new FileWriter(f));
            fileWriter.write(fileInfo.getContent());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    /**
     * Stores a directory and all of it's sub-directories and files within in the specified root directory.
     * @param rootDirectory Directory where to store the actual directory.
     * @param fileInfoList List containing FileInfo objects for the specified directory and all of it's
     *                     sub-directories and files within.
     * @param topDirectory Path of the top directory relative to it's root directory.
     * @return true if the directory was stored successfully, false otherwise.
     */
    public static boolean storeDirectory(String rootDirectory, List<FileInfo> fileInfoList, String topDirectory) {

        Queue<String> pendingFiles = new LinkedList<>();
        pendingFiles.add(topDirectory);

        //Vrtimo se u petlji dok ne kreiramo sve fajlove i foldere koji su prosledjeni
        while (!pendingFiles.isEmpty()) {
            String path = pendingFiles.poll();
            FileInfo fileInfo = fileInfoList.get(0);
            for (FileInfo fi : fileInfoList) {
                if (fi.getPath().equals(path)) {
                    fileInfo = fi;
                    break;
                }
            }

            if (fileInfo.isFile()) {
                if (!storeFile(rootDirectory, fileInfo, false)) {
                    return false;
                }
            } else {
                //Vec ce biti napravljeni svi potrebni folderi ja mislim
                pendingFiles.addAll(fileInfo.getSubFiles());
            }
        }

        return true;

    }

    /**
     * Deletes the file or folder with the given path from the root directory as well as
     * all of the folders above if they are empty.
     * @param rootDirectory Directory where to perform the deletion.
     * @param path Path to the file or directory relative to it's root directory.
     */
    public static void removeFile(String rootDirectory, String path) {

        //Brisemo fajl
        String filePath = rootDirectory + "\\" + path;
        File f = new File(filePath);
        f.delete();

        //Prodjemo kroz sve njegove nad-direktorijume i obrisemo ih ako su prazni
        if (path.contains("\\")){
            path = path.substring(0, path.lastIndexOf('\\'));
            String[] split = path.split("\\\\");
            for (int i = split.length - 1; i >= 0; i--) {
                String dirPath = split[i];
                for (int j = i - 1; j >=0; j--) {
                    dirPath = split[j] + "\\" + dirPath;
                }
                dirPath = rootDirectory + "\\" + dirPath;
                File dir = new File(dirPath);
                if (dir.listFiles().length == 0) {
                    dir.delete();
                }
            }
        }

    }

}
