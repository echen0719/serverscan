public class generateIPs {
    // https://pingproxies.com/blog/generate-a-random-ip-address
    private static boolean isRestrictedIP(long ip) {
        int o1 = (int)(ip >>> 24) & 0xFF;
        int o2 = (int)(ip >>> 16) & 0xFF;
        int o3 = (int)(ip >>> 8) & 0xFF;

        // switch statements are around 2-4x faster than if statements
        switch (o1) {
            case 0: return true; // 0.x.x.x
            case 10: return true; // 10.x.x.x
            case 127: return true; // 127.x.x.x
            case 100: return (o2 >= 64 && o2 < 128); // 100.64-127.x.x
            case 169: return o2 == 254; // 169.254.x.x
            case 172: return (o2 >= 16 && o2 < 32); // 172.16-31.x.x
            case 192:
                switch(o2) {
                    case 0: return (o3 == 0 || o3 == 2); // 192.0.0.x and 192.0.2.x
                    case 168: return true;  // 192.168.x.x
                    default: return false;
                }
            case 198:
                switch(o2) {
                    case 18: return true;  // 198.18.x.x
                    case 19: return true;  // 198.19.x.x
                    case 51: return o3 == 100;  // 198.51.100.x
                    default: return false;
                }
            case 203: return (o2 == 0 && o3 == 113);  // 203.0.113.x

            default: return o1 >= 224;  // 224-255.x.x.x
        }
    }

    public static String intToIP(long ip) {
        if (isRestrictedIP(ip)) return null;

        // https://mkyong.com/java/java-and-0xff-example/
        int o1= (int)(ip >> 24) & 0xFF;
        int o2 = (int)(ip >> 16) & 0xFF;
        int o3 = (int)(ip >> 8) & 0xFF;
        int o4 = (int)ip & 0xFF;

        return o1 + "." + o2 + "." + o3 + "." + o4;
    }

    public static void main(String[] args) {
        for (long ip = 0; ip < 0xFFFFFFFFL; ip++) {
            // System.out.println(intToIP(ip)); // <-- this slows it down so MUCH
        }
    }
}