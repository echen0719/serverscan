import java.util.Random;

public class portScanner {
    // https://pingproxies.com/blog/generate-a-random-ip-address
    private static boolean isRestrictedIP(long ip) {
        int o1 = (int)(ip >>> 24) & 0xFF;
        int o2 = (int)(ip >>> 16) & 0xFF;
        int o3 = (int)(ip >>> 8) & 0xFF;

        if (o1 == 0) return true; // 0.x.x.x
        if (o1 == 10) return true; // 10.x.x.x
        if (o1 == 127) return true; // 127.x.x.x
        if (o1 == 100 && (o2 >= 64 && o2 < 128)) return true; // 100.64-127.x.x
        if (o1 == 169 && o2 == 254) return true; // 169.254.x.x
        if (o1 == 172 && (o2 >= 16 && o2 < 32)) return true; // 172.16-31.x.x
        if (o1 == 192 && o2 == 0 && o3 == 0) return true; // 192.0.0.x
        if (o1 == 192 && o2 == 0 && o3 == 2) return true; // 192.0.2.x
        if (o1 == 192 && o2 == 168) return true; // 192.168.x.x
        if (o1 == 198 && (o2 == 18 || o2 == 19)) return true; // 198.18-19.100.x
        if (o1 == 198 && o2 == 51 && o3 == 100) return true; // 198.51.100.x
        if (o1 == 203 && o2 == 0 && o3 == 113) return true; // 203.0.113.x
        if (o1 >= 224) return true; // 224-255.x.x.x

        return false;
    }

    private static String intToIP(long ip) {
        // https://mkyong.com/java/java-and-0xff-example/
        int o1= (int)(ip >> 24) & 0xFF;
        int o2 = (int)(ip >> 16) & 0xFF;
        int o3 = (int)(ip >> 8) & 0xFF;
        int o4 = (int)ip & 0xFF;

        return o1 + "." + o2 + "." + o3 + "." + o4;
    }

    public static void main(String[] args) {
        for (long ip = 0; ip < 0xFFFFFFFFL; ip++) {
            if (isRestrictedIP(ip)) continue;
            // else System.out.println(intToIP(ip));
        }
    }
}