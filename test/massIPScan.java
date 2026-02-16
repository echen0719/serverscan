// important! Run with SUDO

import java.util.Scanner;

public class massIPScan {
    private static Scanner keyboard = new Scanner(System.in);
    private static String ipRanges;
    private static String portRanges;
    private static String outputFile;
    private static String rate;

    private static void getIPs() {
        System.out.println("----- Accepted Formats -----");
        System.out.println("Single IP: \t\t 1.2.3.4");
        System.out.println("IP Range: \t\t 1.2.3.4-5.6.7.8");
        System.out.println("Specific IPs: \t\t 1.2.3.4,5.6.7.8,9.10.11.12");
        System.out.println("Combination: \t\t 1.2.3.4-5.6.7.8,9.10.11.12");
        System.out.println("----------------------------\n");
        System.out.print("Enter IP range(s): ");
        ipRanges = keyboard.nextLine();
        System.out.println();
    }

    private static void getPorts() {
        System.out.println("----- Accepted Formats -----");
        System.out.println("Single Port: \t\t 25565");
        System.out.println("Port Range: \t\t 80-25565");
        System.out.println("Specific Ports: \t 80,443,25565");
        System.out.println("Combination: \t\t 80-25565,443");
        System.out.println("----------------------------\n");
        System.out.print("Enter port range(s): ");
        portRanges = keyboard.nextLine();
        System.out.println();
    }

    public static void getOutputFile() {
        System.out.print("Enter output file: ");
        outputFile = keyboard.nextLine();
        System.out.println();
    }

    public static void getRate() {
        System.out.print("Enter speed (packets/second): ");
        rate = keyboard.nextLine();
        System.out.println();
    }

    public static void main(String[] args) {
        getIPs(); getPorts(); getOutputFile(); getRate();

        try {
            ProcessBuilder peanutButter = new ProcessBuilder("./masscan", ipRanges, "-p", portRanges, "-oL", outputFile, "--rate=" + rate);

            peanutButter.inheritIO(); // outputs to command prompt for testing
            Process process = peanutButter.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) System.out.println("\nExited with error code : " + exitCode);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}