package echen0719.serverscan.utils;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class IPUtils {
    // https://stackoverflow.com/questions/12057853
    public static long ipToLong(String ip) {
		String[] parts = ip.split("\\.");
		long result = 0;
		for (String part : parts) {
			result = (result << 8) | Integer.parseInt(part);
		}
		return result;
    }

    public static String longToIP(long ip) {
        int o1 = (int)(ip >> 24) & 0xFF;
        int o2 = (int)(ip >> 16) & 0xFF;
        int o3 = (int)(ip >> 8) & 0xFF;
        int o4 = (int)ip & 0xFF;

        return o1 + "." + o2 + "." + o3 + "." + o4;
    }

    public static List<long[]> parseIPRanges(String ipRange) {
		List<long[]> rawRanges = new ArrayList<long[]>();

		// ex: ipRange = 1.2.3.4-5.6.7.8, 9.10.11.12
		String[] parts = ipRange.split(","); 

		for (String part : parts) {
			part = part.trim();
			if (part.isEmpty()) continue;

			long start, end;

			if (part.contains("-")) {
				String[] portions = part.split("-");
				if (portions.length != 2) continue;
		
				start = ipToLong(portions[0].trim());
				end = ipToLong(portions[1].trim());
			}
	    	else { // for single IP inputs
				start = ipToLong(part);
				end = start;
	    	}
		
	    	if (start > end) continue;

	    	rawRanges.add(new long[] {start, end});
		} // shuffle chunks
		Collections.shuffle(rawRanges);
        return rawRanges;
    }
}
