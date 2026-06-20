import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    private static File currentDirectory = new File(System.getProperty("user.dir"));

    public static void main(String[] args) throws Exception {
        boolean exit = false;
        Scanner sc = new Scanner(System.in);

        while (!exit) {
            System.out.print("$ ");
            String input = sc.nextLine();

            List<String> words = parseInput(input);

            if (words.isEmpty()) {
                continue;
            }

            String outputFile = null;

            for (int i = 0; i < words.size(); i++) {
                if (words.get(i).equals(">") || words.get(i).equals("1>")) {
                    if (i + 1 < words.size()) {
                        outputFile = words.get(i + 1);
                        words = new ArrayList<>(words.subList(0, i));
                    }
                    break;
                }
            }

            PrintStream out = System.out;

            if (outputFile != null) {
                out = new PrintStream(new FileOutputStream(outputFile, false));
            }

            String command = words.get(0);
            String result = words.size() > 1
                    ? String.join(" ", words.subList(1, words.size()))
                    : "";

            if (Objects.equals(command, "exit")) {
                exit = true;

            } else if (Objects.equals(command, "echo")) {
                out.println(result);

            } else if (Objects.equals(command, "type")) {
                out.println(type(result));

            } else if (Objects.equals(command, "pwd")) {
                out.println(currentDirectory.getCanonicalPath());

            } else if (Objects.equals(command, "cd")) {

                File target;

                if (result.equals("~")) {
                    target = new File(System.getenv("HOME"));
                } else if (new File(result).isAbsolute()) {
                    target = new File(result);
                } else {
                    target = new File(currentDirectory, result);
                }

                if (target.exists() && target.isDirectory()) {
                    currentDirectory = target.getCanonicalFile();
                } else {
                    System.out.println("cd: " + result + ": No such file or directory");
                }

            } else if (getExecutable(command) != null) {

                ProcessBuilder pb = new ProcessBuilder(words);
                pb.directory(currentDirectory);

                Process process = pb.start();

                if (outputFile != null) {
                    process.getInputStream().transferTo(out);
                } else {
                    process.getInputStream().transferTo(System.out);
                }

                process.getErrorStream().transferTo(System.err);

            } else {
                System.out.println(command + ": command not found");
            }

            if (outputFile != null) {
                out.close();
            }
        }

        sc.close();
    }

    private static List<String> parseInput(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean escaping = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (inDoubleQuotes) {
                if (escaping) {
                    if (c == '"' || c == '\\' || c == '$') {
                        current.append(c);
                    } else {
                        current.append('\\');
                        current.append(c);
                    }
                    escaping = false;
                    continue;
                }

                if (c == '\\') {
                    escaping = true;
                    continue;
                }

                if (c == '"') {
                    inDoubleQuotes = false;
                    continue;
                }

                current.append(c);
                continue;
            }

            if (escaping) {
                current.append(c);
                escaping = false;
                continue;
            }

            if (!inSingleQuotes && c == '\\') {
                escaping = true;
                continue;
            }

            if (c == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
                continue;
            }

            if (c == '"' && !inSingleQuotes) {
                inDoubleQuotes = true;
                continue;
            }

            if (c == ' ' && !inSingleQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (escaping) {
            current.append('\\');
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens;
    }

    public static String type(String command) {
        String[] commands = { "exit", "echo", "type", "pwd", "cd" };

        for (String text : commands) {
            if (Objects.equals(text, command))
                return command + " is a shell builtin";
        }

        String pathCommands = System.getenv("PATH");
        String[] pathCommand = pathCommands.split(":");

        for (String path : pathCommand) {
            File file = new File(path, command);
            if (file.exists() && file.canExecute()) {
                return command + " is " + file.getAbsolutePath();
            }
        }

        return command + ": not found";
    }

    public static String getExecutable(String command) {
        String pathCommands = System.getenv("PATH");
        String[] pathCommand = pathCommands.split(":");

        for (String path : pathCommand) {
            File file = new File(path, command);
            if (file.exists() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }

        return null;
    }
}