package com.utils;

import java.io.IOException;

public class FindDesktopPath {
  public static String getPathForWindows() {
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(
        "powershell", "-command", "[Environment]::GetFolderPath('Desktop')"
      );
      processBuilder.redirectErrorStream(true);

      Process process = processBuilder.start();

      try (var reader = new java.io.BufferedReader(
        new java.io.InputStreamReader(process.getInputStream()))
      ) {
        return reader.readLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
