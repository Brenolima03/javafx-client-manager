# JavaFX Application Setup Guide

This guide will help you configure your IDE to run this JavaFX application.

## Prerequisites
1. **Java Development Kit (JDK)**  
   - Ensure that JDK 21 or later is installed.

2. **JavaFX SDK**  
   - Download the JavaFX SDK from [Gluon](https://gluonhq.com/products/javafx/).  
   - Extract the downloaded `.zip` file to a directory of your choice.

---

## Configuration Steps

### Visual Studio Code

1. Make sure you have the `pom.xml` and the `target/classes/` directory.
2. Configure `launch.json` as follows:
   ```json
   {
   "version": "0.2.0",
   "configurations": [
      {
         "type": "java",
         "name": "Main",
         "request": "launch",
         "vmArgs": "--module-path C:/java-libs/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml",
         "mainClass": "com.application/com.application.Main",
         "projectName": "clientManager"
      }
   ]
   }
   ```
3. To run the application, press `F5` or use the `Run` option in the Debug panel.

---

### IntelliJ IDEA

1. **Open Project**  
   Open the project in IntelliJ IDEA.

2. **Set Up JDK**  
   - Go to `File > Project Structure > SDKs`.  
   - Click `+`, then select **Add JDK** and choose the directory where your JDK is installed.

3. **Add JavaFX Library**  
   - Go to `File > Project Structure > Libraries`.  
   - Click `+`, then choose **Java** and navigate to the `lib` folder inside the JavaFX SDK directory.  
   - Click `OK` to add the library.

4. **Modify Run Configuration**  
   - Go to `Run > Edit Configurations`.  
   - Under **VM Options**, add the following line:  
     ```
     --module-path "<path-to-javafx-sdk>/lib" --add-modules javafx.controls,javafx.fxml
     ```
     Replace `<path-to-javafx-sdk>` with the full path to your JavaFX SDK directory.

5. **Run the Application**  
   - Click the green **Run** button or press `Shift + F10` to run the application.

---

### Eclipse

1. **Install Eclipse IDE**  
   - Download and install [Eclipse IDE](https://www.eclipse.org/downloads/).

2. **Set Up JDK**  
   - Go to `Window > Preferences > Java > Installed JREs`.  
   - Add your JDK installation if it's not already listed.

3. **Add JavaFX Library**  
   - Go to `Project > Properties > Java Build Path > Libraries`.  
   - Click `Add External JARs` and navigate to the `lib` folder inside the JavaFX SDK directory.  
   - Select all `.jar` files and click `Apply and Close`.

4. **Modify Run Configuration**  
   - Go to `Run > Run Configurations`.  
   - Under the **Arguments** tab, in the **VM Arguments** field, add the module path presented above.

5. **Run the Application**  
   - Click the green **Run** button or press `Ctrl + F11` to run the application.

---

## Troubleshooting

- Ensure the correct JavaFX modules (`javafx.controls` and `javafx.fxml`) are included in the VM options.
- Verify the paths to the JavaFX SDK are correct.
- If you encounter any errors, double-check your IDE and library configurations.

---
