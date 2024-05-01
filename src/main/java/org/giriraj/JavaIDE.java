package org.giriraj;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import javax.tools.*;
import java.util.*;

public class JavaIDE extends JFrame {
    private JTextArea codeTextArea;
    private JTextArea consoleTextArea;

    public JavaIDE() {
        setTitle("Java IDE");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        codeTextArea = new JTextArea();
        JScrollPane codeScrollPane = new JScrollPane(codeTextArea);

        consoleTextArea = new JTextArea();
        JScrollPane consoleScrollPane = new JScrollPane(consoleTextArea);

        JButton runButton = new JButton("Run");

        runButton.addActionListener(e -> runCode());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(runButton);

        JPanel contentPanel = new JPanel(new GridLayout(1, 2));
        contentPanel.add(codeScrollPane);
        contentPanel.add(consoleScrollPane);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        String sampleCode = "public class HelloWorld {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}";
        codeTextArea.setText(sampleCode);
    }

    private void runCode() {
        String code = codeTextArea.getText();
        consoleTextArea.setText(""); // Clear console before running code
        File tempFile = null;
        try {
            // Extract class name from code
            String className = getClassName(code);
            if (className == null) {
                consoleTextArea.append("Error: Class name not found in code.\n");
                return;
            }

            // Create a temporary file with the class name
            tempFile = new File(className + ".java");
            FileWriter writer = new FileWriter(tempFile);
            writer.write(code);
            writer.close();

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(tempFile);
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
            boolean success = task.call();

            if (success) {
                ProcessBuilder pb = new ProcessBuilder("java", className);
                pb.directory(tempFile.getParentFile());
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    consoleTextArea.append(line + "\n"); // Append output to console
                }
                reader.close();
            } else {
                for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                    consoleTextArea.append("Error on line " + diagnostic.getLineNumber() + " in " + diagnostic + "\n");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (tempFile != null && !tempFile.delete()) {
                consoleTextArea.append("Temporary file deletion failed.\n");
            }
        }
    }

    private String getClassName(String code) {
        // Extract class name from code
        int classIndex = code.indexOf("class");
        if (classIndex == -1) {
            return null;
        }
        int startIndex = classIndex + 5; // index after "class"
        int endIndex = code.indexOf("{", startIndex);
        return code.substring(startIndex, endIndex).trim();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JavaIDE::new);
    }
}
