package com.poping520.dyxposed.os;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/10 9:29
 */
public class Shell {

    private static final String SU = "su";
    private static final String SH = "sh";
    static final String EXIT = "exit\n";
    static final String LINE_END = "\n";

    public static Result exec(boolean isNeedRoot, boolean isNeedResultMsg, String... commands) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new Result(result, null, null);
        }

        Process process = null;
        DataOutputStream dos = null;
        BufferedReader successBr = null;
        BufferedReader errorBr = null;
        String successMsg = "";
        String errorMsg = "";

        try {
            process = new ProcessBuilder(isNeedRoot ? SU : SH).start();
            dos = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                dos.write(command.getBytes());
                dos.writeBytes(LINE_END);
                dos.flush();
            }
            dos.writeBytes(EXIT);
            dos.flush();

            result = process.waitFor();

            if (isNeedResultMsg) {
                StringBuilder successSb = new StringBuilder();
                StringBuilder errorSb = new StringBuilder();
                successBr = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorBr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s;
                while ((s = successBr.readLine()) != null) {
                    successSb.append(s).append(LINE_END);
                }
                while ((s = errorBr.readLine()) != null) {
                    errorSb.append(s).append(LINE_END);
                }
                if (successSb.length() > 0)
                    successMsg = successSb.deleteCharAt(successSb.length() - 1).toString();
                if (errorSb.length() > 0)
                    errorMsg = errorSb.deleteCharAt(errorSb.length() - 1).toString();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (successBr != null) {
                try {
                    successBr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (errorBr != null) {
                try {
                    errorBr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null)
                process.destroy();
        }
        return new Result(result, successMsg, errorMsg);
    }

    public static class Result {

        public int resultCode;
        public boolean success;
        public String successMsg;
        public String errorMsg;

        private Result(int result, String successMsg, String errorMsg) {
            resultCode = result;
            this.success = result == 0;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "resultCode=" + resultCode +
                    ", success=" + success +
                    ", successMsg='" + successMsg + '\'' +
                    ", errorMsg='" + errorMsg + '\'' +
                    '}';
        }
    }
}
