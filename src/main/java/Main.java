import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("$ ");
            String input = sc.nextLine();
            if (input.equals("exit")) {
                break;
            }

            // Echo command
            else if (input.startsWith("echo ")) {
                System.out.println(input.substring(5));

            } else if (input.startsWith("type ")) {
                if (input.substring(6).equals("echo") || input.substring(6).equals("exit")) {
                    System.out.println(input.substring(6) + " is a shell builtin");
                }
            } else {
                System.out.println(input + ": command not found");
            }

        }
    }
}
