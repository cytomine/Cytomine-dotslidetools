package dotslide
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
import groovy.io.FileType

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import java.io.*;
import org.apache.commons.cli.*;


public class Main {

    static final String[][] level1 = [["AAA.jpg"]];

    static final String[][] level2 = [
            ["BAA.jpg","BBA.jpg"],
            ["BAB.jpg","BBB.jpg"]
    ];

    static final String[][] level3 = [
            ["CAA.jpg","CBA.jpg","CCA.jpg","CDA.jpg"],
            ["CAB.jpg","CBB.jpg","CCB.jpg","CDB.jpg"],
            ["CAC.jpg","CBC.jpg","CCC.jpg","CDC.jpg"],
            ["CAD.jpg","CBD.jpg","CCD.jpg","CDD.jpg"]
    ];

    static final String[][] level4 = [
            ["DAA","DBA","DCA","DDA","DEA","DFA","DGA","DHA"],
            ["DAB","DBB","DCB","DDB","DEB","DFB","DGB","DHB"],
            ["DAC","DBC","DCC","DDC","DEC","DFC","DGC","DHC"],
            ["DAD","DBD","DCD","DDD","DED","DFD","DGD","DHD"],
            ["DAE","DBE","DCE","DDE","DEE","DFE","DGE","DHE"],
            ["DAF","DBF","DCF","DDF","DEF","DFF","DGF","DHF"],
            ["DAG","DBG","DCG","DDG","DEG","DFG","DGG","DHG"],
            ["DAH","DBH","DCH","DDH","DEH","DFH","DGH","DHH"]
    ];

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        CommandLineParser parser = new BasicParser();

        options.addOption("h", "help", false, "print this message");
        options.addOption("fp", "filepath", true, "file with path to construct image");
        options.addOption("fi", "fileinfo", true, "file with info about image");
        options.addOption("fc", "filecoord", true, "file with info about image");
        options.addOption("p", "path", true, "path (directory) with image dotslide");

        int nbLevels = -1;
        int tileSize = 1024
        String absoluteFilePath = null;
        String absoluteFilePathInfo = null;
        String nameFileCoordinates = null;
        String imagePath = null;

        try {
            CommandLine line = parser.parse( options, args );

            if(line.hasOption("filepath") && line.hasOption("fileinfo") && line.hasOption("path")) {
                absoluteFilePath = line.getOptionValue("filepath");
                absoluteFilePathInfo = line.getOptionValue("fileinfo");
                imagePath = line.getOptionValue("path");
            } else if (line.hasOption("filecoord") && line.hasOption("path")) {
                nameFileCoordinates =  line.getOptionValue("filecoord");
                imagePath = line.getOptionValue("path");
            } else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("dotslideTool", options );
                System.exit(0);
            }

        } catch( ParseException exp ) { System.out.println( "Unexpected exception:" + exp.getMessage() ); }

        File imageDirectory = new File(imagePath);

        for (String filename : imageDirectory.list()) {

            if (filename.equals("ImageProperties.xml")) {
                //Seek number of level
                def infos = seekLevelAndTilesize(imagePath, filename);
                nbLevels = infos.level
                if(infos.tileSize) tileSize = infos.tileSize
            }
            if (filename.equals("ExtendedProps.xml")) {
                //Creating file with all informations about the image
                creatingTextInfo(imagePath, filename, absoluteFilePathInfo);
            }
        }

        assert(nbLevels != -1);


        String[] concernedTiles;
        def concernedLevel = null
        if ( (nbLevels % 3) == 1) concernedLevel = level1
        if ( (nbLevels % 3) == 2) concernedLevel = level2
        if ( (nbLevels % 3) == 0) concernedLevel = level3
        assert(concernedLevel != null)

        concernedTiles = concernedLevel.flatten()

        int depthDirectories = (int) Math.floor((nbLevels - 1)/ 3)

        String[] tiles = getTilesAtMaximumLevel(imagePath, nbLevels, depthDirectories, concernedTiles)

        def coordinates = computeCoordinatesUsingAlphabet(tiles, depthDirectories, concernedLevel, nbLevels, tileSize)


        //println coordinates

        int minX = coordinates.collect { it.xTotal }.min()
        int minY = coordinates.collect { it.yTotal }.min()

        if (absoluteFilePath && absoluteFilePathInfo) {
            try {
                String addressFile = absoluteFilePath+".txt";
                if (new File(addressFile).exists()) {
                    new File(addressFile).delete()
                }
                FileWriter fw = new FileWriter(addressFile, true);
                BufferedWriter output = new BufferedWriter(fw);

                coordinates.each {
                    output.write((it.xTotal - minX) + ";" + (it.yTotal - minY) + ";" + imagePath + File.separator + it.tilePath + "\n");
                }

                output.flush();
                output.close();
            }
            catch(IOException ioe){
                System.out.print("Error : ");
                ioe.printStackTrace();
            }
        } else if (nameFileCoordinates) {
            println "write minX ($minX), minY ($minY)  for $imagePath"
            String addressFile = System.getProperty("user.dir") + "/"+nameFileCoordinates+".txt";
            //println addressFile
            if (new File(addressFile).exists()) {
                new File(addressFile).delete()
            }
            FileWriter fw = new FileWriter(addressFile, true);
            BufferedWriter output = new BufferedWriter(fw);
            output.write(minX + "\n");
            output.write(minY + "\n");
            output.flush();
            output.close();
        }
    }

    static def computeCoordinatesUsingAlphabet(String[] tilesPath, int depthDirectories,def concernedLevel, int nbLevels, int tileSize) {
        def coordinates = []
        tilesPath.each { tilePath ->

            String[] tilePathSplit = tilePath.split(File.separator)
            def x = []
            def y = []
            tilePathSplit.eachWithIndex { it, depth ->
                if (!it.endsWith("jpg")) {
                    def ij = getValueIndicesFromArray(level4, it)
                    x << ij.i
                    y << ij.j
                } else {
                    def ij = getValueIndicesFromArray(concernedLevel, it)
                    x << ij.i
                    y << ij.j
                }
            }

            int zoom
            //init initialZoom factor
            if (concernedLevel == level1) zoom = 0
            if (concernedLevel == level2) zoom = 1
            if (concernedLevel == level3) zoom = 2
            int xTotal = 0
            int yTotal = 0

            x.reverse().eachWithIndex{ int _v, int i ->
                if (i == 0) {
                    xTotal += _v * tileSize

                }
                else {
                    xTotal += _v * tileSize * Math.pow(2, zoom + ((i-1) * 3))
                }
            }
            y.reverse().eachWithIndex{ int _v, int i ->
                if (i == 0) {
                    yTotal += _v * tileSize

                }
                else {
                    yTotal += _v * tileSize * Math.pow(2, zoom + ((i-1) * 3))
                }
            }
            def res = [tilePath : tilePath, x : x, y : y, xTotal : xTotal, yTotal : yTotal ]
            //println "=> $res"
            coordinates << res
        }

        coordinates = coordinates.sort{map1, map2 -> map1.xTotal <=> map2.xTotal ?: map1.yTotal <=> map2.yTotal}

        return coordinates
    }



    static def getValueIndicesFromArray(array, String value) {

        def alphabet = ('A'..'Z')

        def indices = [ i : alphabet.indexOf(value[2]) , j : alphabet.indexOf(value[1])]

        return indices
    }



    public static String[] getTilesAtMaximumLevel(String imagePath, int nbLevels, int depthDirectories, String[] concernedTiles) {

        //println "numberOfSubLevels $depthDirectories $imagePath"
        def level4Flat = level4.flatten()
        def tiles = []
        new File(imagePath).eachFileRecurse (FileType.FILES) { file ->

            String tilePath = file.toString().replace(imagePath, "")
            if (tilePath.endsWith("jpg") && tilePath != "thumb.jpg") {
                //println file
                String[] tilePathSplit = tilePath.split(File.separator)
                boolean badDirectory = false
                for (int i = 0; i < tilePathSplit.length - 1; i++) {
                    if (!level4Flat.contains(tilePathSplit[i])) {
                        println "$file -> NO"
                        badDirectory = true
                    }
                }
                String tileFilename = tilePathSplit[tilePathSplit.length - 1]
                //println "" + tilePath.count(File.separator) + " vs " + depthDirectories
                if (!badDirectory && (tilePath.count(File.separator) == depthDirectories) && concernedTiles.contains(tileFilename)) {
                    tiles << tilePath
                }
            }
        }

        return tiles
    }

    public static def seekLevelAndTilesize(String imagePath, String nameFile) {
        int level = 0;
        int tileSize = 0;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(imagePath + "/" +nameFile));

            Element root = doc.getDocumentElement();

            List<Element> result = new LinkedList<Element>();
            NodeList nl = root.getChildNodes();
            for (int i = 0; i < nl.getLength(); ++i) {
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE)
                    result.add((Element) nl.item(i));
            }

            for (Element test : result) {
                if (test.getAttribute("levels") != null)
                    level = Integer.parseInt(test.getAttribute("levels"));
                if (test.getAttribute("tilesize") != null)
                    tileSize = Integer.parseInt(test.getAttribute("tilesize"));
            }
        }
        catch (ParserConfigurationException e) { System.out.println("Error configuration parser ..." + e.getMessage()); }
        catch (SAXException e) { System.out.println("Error SAX : " + e.getMessage()); }
        catch (IOException e) { System.out.println("Error I/O : " + e.getMessage()); }

        return ["level" : level, "tileSize":tileSize];
    }

    public static void creatingTextInfo(String imagePath, String nameFile, String absoluteFilePathInfo) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(imagePath + "/" +nameFile));

            Element root = doc.getDocumentElement();
            String addressFile = absoluteFilePathInfo+".txt";
            try {
                FileWriter fw = new FileWriter(addressFile, true);
                BufferedWriter output = new BufferedWriter(fw);
                treeLoop(root, output);
                output.flush();
                output.close();
            }
            catch(IOException ioe){
                System.out.print("Error : ");
                ioe.printStackTrace();
            }

        }
        catch (ParserConfigurationException e) { System.out.println("Error configuration parser ..." + e.getMessage()); }
        catch (SAXException e) { System.out.println("Error SAX : " + e.getMessage()); }
        catch (IOException e) { System.out.println("Error I/O : " + e.getMessage()); }
    }

    public static void treeLoop (Element racine, BufferedWriter output) {
        NodeList nl = racine.getChildNodes();
        for (int i = 0; i < nl.getLength(); ++i) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) nl.item(i);

                creatingFileInfo(nl.item(i).getNodeName(), elem.getAttribute("description"), elem.getAttribute("value"), output);
                treeLoop(elem, output);
            }
        }
    }

    public static void creatingFileInfo (String nameNode, String description, String value, BufferedWriter output) {
        if (description != "" && value != "") {
            output.write(nameNode + "\n");
            output.write(" -> Description : " + description + "\n");
            output.write(" -> Value : " + value + "\n");
        } else if (description != "" && value == "") {
            output.write(nameNode + "\n");
            output.write(" -> Description : " + description + "\n");
        } else if (description == "" && value != "") {
            output.write(nameNode + "\n");
            output.write(" -> Value : " + value + "\n");
        } else {
            output.write(nameNode + "\n");
        }
    }
}
