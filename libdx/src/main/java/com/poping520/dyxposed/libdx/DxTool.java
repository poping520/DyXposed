package com.poping520.dyxposed.libdx;

import com.poping520.dyxposed.libdx.dex.util.FileUtils;
import com.poping520.dyxposed.libdx.dx.Version;
import com.poping520.dyxposed.libdx.dx.command.dexer.Main;
import com.poping520.dyxposed.libdx.dx.command.dexer.Main.Arguments;

import java.io.File;
import java.io.IOException;

/**
 * Created by WangKZ on 18/11/07.
 *
 * @author poping520
 * @version 1.0.0
 */
public class DxTool {

    /**
     * @return dx tool version
     */
    public static String getVersion() {
        return Version.VERSION;
    }

    private Arguments mArgs;

    private DxTool(Arguments args) {
        mArgs = args;
    }

    public boolean start() {
        int result = -1;
        try {
            result = new Main(mArgs.context).runDx(mArgs);
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result == 0;
    }


    public static class Builder {

        private Arguments args;

        public Builder() {
            args = new Arguments();
        }

        public Builder inputs(String... inputs) {
            args.fileNames = inputs;
            return this;
        }

        /**
         * Output name must end with one of: .dex .jar .zip .apk or be a directory.
         */
        public Builder output(String output) {
            args.outName = output;
            if (new File(output).isDirectory()) {
                args.jarOutput = false;
                args.outputIsDirectory = true;
            } else if (FileUtils.hasArchiveSuffix(output)) {
                args.jarOutput = true;
            } else if (output.endsWith(".dex") || output.endsWith("-")) {
                args.jarOutput = false;
                args.outputIsDirectDex = true;
            }
            return this;
        }

        public Builder minSdkVersion(int minSdkVersion) {
            args.minSdkVersion = minSdkVersion;
            return this;
        }

        public Builder verbose(boolean verbose) {
            args.verbose = verbose;
            return this;
        }

        public DxTool build() {
            return new DxTool(args);
        }
    }


    public static void main(String[] args) {

        final DxTool tool = new Builder()
                .inputs("D:\\Users\\WangKZ\\Desktop\\src")
                .output("D:\\Users\\WangKZ\\Desktop\\out.jar")
                .verbose(true)
                .build();
        tool.start();
    }
}
