package com.dji.customsdk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class TerrainFollowing {
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

    public float getAltitude(double latitude, double longitude) {}
}
