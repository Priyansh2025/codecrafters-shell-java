import java.io.*;
import java.util.*;

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

            String stdoutFile = null;
            String stderrFile = null;
            boolean stdoutAppend = false;

            List<String> commandWords = new ArrayList<>();

            for (int i = 0; i < words.size(); i++) {
                String token = words.get(i);

                if ((token.equals(">") || token.equals("1>")) && i + 1 < words.size()) {
                    stdoutFile = words.get(++i);
                    stdoutAppend = false;
                } else if ((token.equals(">>") || token.equals("1>>")) && i + 1 < words.size()) {
                    stdoutFile = words.get(++i);
                    stdoutAppend = true;
                } else if (token.equals("2>") && i + 1 < words.size()) {
                    stderrFile = words.get(++i);
                } else {
                    commandWords.add(token);
                }
            }

            if (commandWords.isEmpty()) {
                continue;
            }

            PrintStream out = System.out;
            PrintStream err = System.err;

            if (stdoutFile != null) {
                out = new PrintStream(new FileOutputStream(stdoutFile, stdoutAppend));
            }

            if (stderrFile != null) {
                err = new PrintStream(new FileOutputStream(stderrFile, false));
            }

            String command = commandWords.get(0);
            String result = commandWords.size() > 1
                    ? String.join(" ", commandWords.subList(1, commandWords.size()))
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
                    err.println("cd: " + result + ": No such file or directory");
                }

            } else if (getExecutable(command) != null) {

                ProcessBuilder pb = new ProcessBuilder(commandWords);
                pb.directory(currentDirectory);

                Process process = pb.start();

                process.getInputStream().transferTo(out);
                process.getErrorStream().transferTo(err);

                process.waitFor();

            } else {
                err.println(command + ": command not found");
            }

            if (out != System.out) {
                out.close();
            }

            if (err != System.err) {
                err.close();
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
            if (Objects.equals(text, command)) {
                return command + " is a shell builtin";
            }
        }

        String[] paths = System.getenv("PATH").split(":");

        for (String path : paths) {
            File file = new File(path, command);

            if (file.exists() && file.canExecute()) {
                return command + " is " + file.getAbsolutePath();
            }
        }

        return command + ": not found";
    }

    public static String getExecutable(String command) {
        String[] paths = System.getenv("PATH").split(":");

        for (String path : paths) {
            File file = new File(path, command);

            if (file.exists() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }

        return null;
    }
}