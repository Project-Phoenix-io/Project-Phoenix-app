package com.projectphoenix.projectphoenix;

/**
 * Created by KooTG on 3/20/2018.
 */

public class Utils {

    public static String flipIp(String ip) {
        String[] split = ip.split("\\.");
        return split[3] + "." + split[2] + "." + split[1] + "." + split[0];
    }

}
