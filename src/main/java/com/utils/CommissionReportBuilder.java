package com.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.gui.Alerts;

import javafx.scene.control.Alert.AlertType;

public class CommissionReportBuilder {
  public void generateReportFile(Map<String, String> commissionData) {
    if (commissionData.isEmpty()) {
      Alerts.showAlert("Erro", "A lista está vazia.", null, AlertType.ERROR);
      return;
    }

    String desktopPath = FindDesktopPath.getPathForWindows();
    if (desktopPath == null) {
      Alerts.showAlert(
        "Erro", "Falha ao localizar a área de trabalho.", null, AlertType.ERROR
      );
      return;
    }

    String outputFilePath = Paths.get(desktopPath, "comissões.xlsx").toString();

    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Comissões");

      Row headerRow = sheet.createRow(0);
      headerRow.createCell(0).setCellValue("Locador");
      headerRow.createCell(1).setCellValue("Comissão");

      int rowIndex = 1;
      for (Map.Entry<String, String> entry : commissionData.entrySet()) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(entry.getKey());
        row.createCell(1).setCellValue(entry.getValue());
      }

      sheet.autoSizeColumn(0);
      sheet.autoSizeColumn(1);

      try (FileOutputStream fileOut = new FileOutputStream(outputFilePath)) {
        workbook.write(fileOut);
        Alerts.showAlert(
          "Sucesso", "Arquivo Excel gerado com sucesso na área de trabalho!",
          null, AlertType.INFORMATION
        );
      }
    } catch (IOException e) {
      Alerts.showAlert(
        "Erro", "Falha ao gerar o arquivo. Entre em contato com o suporte.",
        e.getMessage(), AlertType.ERROR
      );
    }
  }
}
