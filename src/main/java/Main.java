import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Main {
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

            if (Objects.equals(command, "exit"))
                exit = true;
            else if (Objects.equals(command, "echo"))
                System.out.println(result);
            else if (Objects.equals(command, "type"))
                System.out.println(type(result));
            else if (getExecutable(command) != null) {
                Process process = Runtime.getRuntime().exec(input.split(" "));
                process.getInputStream().transferTo(System.out);
            } else
                System.out.println(input + ": command not found");
        }
        sc.close();
    }

    public static String type(String command) {
        String[] commands = { "exit", "echo", "type" };
        String path_commands = System.getenv("PATH");
        String[] path_command = path_commands.split(":");
        for (String text : commands) {
            if (Objects.equals(text, command))
                return command + " is a shell builtin";
        }
        for (String path : path_command) {
            File file = new File(path, command);
            if (file.exists() && file.canExecute()) {
                return command + " is " + file.getAbsolutePath();
            }
        }
        return command + ": not found";
    }

    public static String getExecutable(String command) {
        String path_commands = System.getenv("PATH");
        String[] path_command = path_commands.split(":");
        for (String path : path_command) {
            File file = new File(path, command);
            if (file.exists() && file.canExecute()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }
}