/*
 * Copyright (c) 2009-2015. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
	Author: P. Ansen
**/
public class Main {

    private static ArrayList<Integer> listX;
    private static ArrayList<Integer> listY;
    private static ArrayList<String> listPath;

    public static void main(String[] args) throws Exception {

        listX = new ArrayList<Integer>();
        listY = new ArrayList<Integer>();
        listPath = new ArrayList<String>();
        String nameFile = "";
        String nameImage = "";

        Options options = new Options();
        CommandLineParser parser = new BasicParser();


        options.addOption("h", "help", false, "print this message");
        options.addOption("f", "file", true, "file with path to construct image");
        options.addOption("io", "imageout", true, "return image with format tiff");

        try {
            CommandLine line = parser.parse( options, args );

            if(line.hasOption("file") && line.hasOption("imageout")) {
                nameFile = line.getOptionValue("file");
                nameImage = line.getOptionValue("imageout");
            } else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("dotslideBuildTool", options );
                System.exit(0);
            }

        } catch( ParseException exp ) { System.out.println( "Unexpected exception:" + exp.getMessage() ); }

        System.out.println("-> Read File");
        BufferedReader br = new BufferedReader(new FileReader(nameFile));
        try {
            String line = br.readLine();
            splitLine(line);

            while (line != null) {
                line = br.readLine();
                splitLine(line);
            }
        } finally { br.close(); }

        System.out.println("-> Call Method BuildImage");
        int rows = (Collections.max(listX) - Collections.min(listX))/1024 + 1;
        int cols = (Collections.max(listY) - Collections.min(listY))/1024 + 1;
        buildImageVIPS(rows, cols, nameImage);
    }

    public static void buildImageVIPS (int rows, int cols, String nameImage) throws IOException, InterruptedException {
        int xmin = Collections.min(listX);
        int ymin = Collections.min(listY);
        Runtime rt = Runtime.getRuntime();
        Process pr;

        System.out.println("-> Creating Image VIPS");
        System.out.println("Width : " + (1024*cols) + " - Height : " + (1024*rows));
        pr = rt.exec("/usr/local/bin/vips im_black "+nameImage+".v " + (1024*cols) + " " + (1024*rows) + " 3");
        pr.waitFor();

        System.out.println("-> InsertPlace JPG on VIPS");
        for (int i = 0; i <listX.size(); i++) {
            String command = "/usr/local/bin/vips im_insertplace "+ nameImage +".v "+ listPath.get(i) + " " + (listY.get(i) - ymin) + " " + (listX.get(i) - xmin);
            System.out.print(".");
            pr = rt.exec(command);
            pr.waitFor();
        }

        System.out.println("-> Convert VIPS to TIFF");
        pr = rt.exec("/usr/local/bin/vips im_vips2tiff "+nameImage+".v "+nameImage+".tif:jpeg:95,tile:256x256,pyramid,,,,8");
        pr.waitFor();

        System.out.println("-> Deleting File VIPS ("+nameImage+".v)");
        rt.exec(new String[] { "sh", "-c", "rm "+nameImage+".v" });
        pr.waitFor();
    }

    public static void splitLine(String line) {
        if(line!= null) {
            String[] parts = line.split(";");

            listX.add(Integer.parseInt(parts[0]));
            listY.add(Integer.parseInt(parts[1]));
            listPath.add(parts[2]);
        }
    }
}
