package com.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import com.model.entities.Client;
import com.model.entities.Contract;
import com.model.entities.Estate;

public class ContractBuilder {
  public static void emitContract(
    Client tenant, Client landlord, Estate estate, Contract contract
  ) {
    try {
      String fileName = "src\\main\\java\\com\\ContractModel.docx";

      // Read the .docx file
      try (XWPFDocument doc =
        new XWPFDocument(Files.newInputStream(Paths.get(fileName)))) {

        // Loop through all paragraphs and replace placeholders
        for (XWPFParagraph paragraph : doc.getParagraphs())
          replacePlaceholders(paragraph, tenant, landlord, estate, contract);

        // Save the modified document to a new file
        String outputFileName = String.format(
          "src\\main\\java\\com\\Contrato %d.docx", contract.getId()
        );
        try (FileOutputStream out = new FileOutputStream(outputFileName)) {
          doc.write(out);
        }
      }
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  private static void replacePlaceholders(
    XWPFParagraph paragraph, Client tenant, Client landlord,
    Estate estate, Contract contract
  ) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Create a map with placeholders as keys and corresponding values
    Map<String, String> replacements = new HashMap<>();
    replacements.put("{LANDLORD_NAME}", landlord.getName());
    replacements.put("{LANDLORD_NATIONALITY}", landlord.getNationality());
    replacements.put(
      "{LANDLORD_MARITAL_STATUS}", landlord.getMaritalStatus().toString()
    );
    replacements.put("{LANDLORD_PROFESSION}", landlord.getProfession());
    replacements.put("{LANDLORD_ID}", landlord.getRg());
    replacements.put("{LANDLORD_CPF}", landlord.getCpfCnpj());
    replacements.put("{LANDLORD_ADDRESS}", landlord.getAddress());
    replacements.put("{LANDLORD_NEIGHBORHOOD}", landlord.getNeighborhood());
    replacements.put("{LANDLORD_CITY}", landlord.getCity());

    replacements.put("{TENANT_NAME}", tenant.getName());
    replacements.put("{TENANT_NATIONALITY}", tenant.getNationality());
    replacements.put(
      "{TENANT_MARITAL_STATUS}", tenant.getMaritalStatus().toString()
    );
    replacements.put("{TENANT_PROFESSION}", tenant.getProfession());
    replacements.put("{TENANT_ID}", tenant.getRg());
    replacements.put("{TENANT_CPF}", tenant.getCpfCnpj());
    replacements.put("{TENANT_ADDRESS}", tenant.getAddress());
    replacements.put("{TENANT_NEIGHBORHOOD}", tenant.getNeighborhood());
    replacements.put("{TENANT_CITY}", tenant.getCity());
    replacements.put("{TENANT_ZIP}", tenant.getZip());

    replacements.put("{ESTATE_ADDRESS}", estate.getAddress());
    replacements.put("{ESTATE_NUMBER}", String.valueOf(estate.getNumber()));
    replacements.put("{ESTATE_NEIGHBORHOOD}", estate.getNeighborhood());
    replacements.put("{ESTATE_CITY}", estate.getCity());
    replacements.put("{ESTATE_STATE}", estate.getState());
    replacements.put("{ESTATE_DETAILS}", estate.getDescription());

    replacements.put(
      "{RENT_BEGINNING}", contract.getRentBeginning().format(dateFormatter)
    );
    replacements.put(
      "{RENT_END}", contract.getRentEnd().format(dateFormatter)
    );
    replacements.put(
      "{RENT_VALUE}",
      Currency.getCurrencyConverter().toString(contract.getRentValue())
    );
    replacements.put(
      "{ENERGY_BILL}",
      Currency.getCurrencyConverter().toString(contract.getEnergyBill())
    );
    replacements.put(
      "{WATER_BILL}",
      Currency.getCurrencyConverter().toString(contract.getWaterBill())
    );
    replacements.put(
      "{CONTRACT_SIGNING_DATE}", 
      contract.getContractSigningDate().format(dateFormatter)
    );

    // Iterate through all runs in the paragraph
    for (XWPFRun run : paragraph.getRuns()) {
      String text = run.getText(0);

      if (text != null) {
        // Replace the placeholders using the map
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
          text = text.replace(entry.getKey(), entry.getValue());
        }

        // Update the run's text with the modified text
        run.setText(text, 0);
      }
    }
  }
}
