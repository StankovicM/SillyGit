package sillygit.util;

import app.ChordState;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 463426265374700139L;

    private final String path;
    private final String content;
    private final int version;

    private final boolean isDirectory;
    private final List<String> subFiles;

    private FileInfo(String path, boolean isDirectory, String content, int version, List<String> subFiles) {

        this.path = path;
        this.isDirectory = isDirectory;
        this.content = content;
        this.version = version;
        this.subFiles = new ArrayList<>();
        if (subFiles != null) {
            this.subFiles.addAll(subFiles);
        }

    }

    public FileInfo(String path, String content, int version) { this(path, false, content, version, null); }

    public FileInfo(String path, List<String> subFiles) { this (path, true, "", 0, subFiles); }

    public FileInfo(FileInfo fileInfo) {

        this(fileInfo.getPath(), fileInfo.isDirectory(), fileInfo.getContent(), fileInfo.getVersion(), fileInfo.getSubFiles());

    }

    public String getPath() { return path; }

    public boolean isDirectory() { return isDirectory; }

    public boolean isFile() { return !isDirectory; }

    public String getContent() { return content; }

    public int getVersion() { return version; }

    public List<String> getSubFiles() { return subFiles; }

    @Override
    public int hashCode() { return ChordState.chordHash(getPath()); }

    @Override
    public boolean equals(Object o) {

        if (o instanceof FileInfo)
            return o.hashCode() == this.hashCode();

        return false;

    }

    @Override
    public String toString() {

        String toReturn;
        if (isDirectory)
            toReturn = "[" + getPath() + " {" + getSubFiles() + "}]";
        else
            toReturn = "[" + getPath() + " <" + getVersion() + ">]";

        return toReturn;

    }

}
