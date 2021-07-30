package com.dji.customsdk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class TerrainFollowing {
    private final int offset = -12;
    private final int defaultAltitude = 20;
    private Hashtable<Integer, Hashtable<Integer, Float>> AltitudeData;

    public TerrainFollowing(InputStream inputStream) throws FileNotFoundException {
        AltitudeData = new Hashtable<Integer, Hashtable<Integer, Float>>();
        readFile(inputStream);
    }

    public void readFile(InputStream inputStream) throws FileNotFoundException {
        Scanner sc = new Scanner(inputStream);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            Scanner rowScanner = new Scanner(line);
            rowScanner.useDelimiter(",");
            int longitude = (int) (Float.parseFloat(rowScanner.next()) * 10000);
            int latitude = (int) (Float.parseFloat(rowScanner.next()) * 10000);
            Float altitude = Float.parseFloat(rowScanner.next());
            if (!AltitudeData.containsKey(latitude)) {
                AltitudeData.put(latitude, new Hashtable<Integer, Float>());
                AltitudeData.get(latitude).put(longitude, altitude);
            }
            AltitudeData.get(latitude).put(longitude, altitude);
        }
//        System.out.println("///////");
//        System.out.println("Finished reading file, with " + AltitudeData.size() + "kv pairs");
//        System.out.println("Average hashmap has " + AltitudeData.get(405703).size() + "kv pairs");
//        System.out.println("///////");
    }

    public float getAltitude(double lat, double lon) {
        int latitude = (int) (lat * 10000);
        int longitude = (int) (lon * 10000);
        int altitude = -9999;
        if (AltitudeData.containsKey(latitude)) {
            if (AltitudeData.get(latitude).containsKey(longitude)) {
                return AltitudeData.get(latitude).get(longitude);
            } else if (AltitudeData.get(latitude + 1).containsKey(longitude)) {
                return AltitudeData.get(latitude).get(longitude);
            }
        } else {
            if (AltitudeData.containsKey(latitude + 1)) {
                if (AltitudeData.get(latitude).containsKey(longitude)) {
                    return AltitudeData.get(latitude).get(longitude);
                } else if (AltitudeData.get(latitude + 1).containsKey(longitude)) {
                    return AltitudeData.get(latitude).get(longitude);
                }
            }
        }
        if (altitude < -100) {
            return 20;
        }
        else {
            return altitude + offset;
        }
    }
}
