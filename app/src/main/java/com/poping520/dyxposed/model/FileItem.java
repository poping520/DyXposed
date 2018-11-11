package com.poping520.dyxposed.model;

import java.io.File;
import java.io.Serializable;

/**
 * Created by WangKZ on 18/11/11.
 *
 * @author poping520
 * @version 1.0.0
 */
public class FileItem implements Serializable {

    public FileType type;

    public String name;

    public File file;

    public FileItem(FileType type, String name, File file) {
        this.type = type;
        this.name = name;
        this.file = file;
    }
}
