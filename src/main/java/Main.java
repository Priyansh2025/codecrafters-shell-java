import java.io.File;
import java.util.Arrays;
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

            String[] words = input.split(" ");
            String command = words[0];
            String[] rest = Arrays.copyOfRange(words, 1, words.length);
            String result = String.join(" ", rest);

            if (Objects.equals(command, "exit")) {
                exit = true;
            } else if (Objects.equals(command, "echo")) {
                System.out.println(result);
            } else if (Objects.equals(command, "type")) {
                System.out.println(type(result));
            } else if (Objects.equals(command, "pwd")) {
                System.out.println(currentDirectory.getCanonicalPath());
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

                ProcessBuilder pb = new ProcessBuilder(input.split(" "));
                pb.directory(currentDirectory);

                Process process = pb.start();
                process.getInputStream().transferTo(System.out);

            } else {
                System.out.println(input + ": command not found");
            }
        }

        sc.close();
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