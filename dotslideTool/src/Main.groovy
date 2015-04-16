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

    /*static final String[][] level2 = [
            ["BAA.jpg","BAB.jpg"],
            ["BBA.jpg","BBB.jpg"]
    ];

    static final String[][] level3 = [
            ["CAA.jpg","CAB.jpg","CAC.jpg","CAD.jpg"],
            ["CBA.jpg","CBB.jpg","CBC.jpg","CBD.jpg"],
            ["CCA.jpg","CCB.jpg","CCC.jpg","CCD.jpg"],
            ["CDA.jpg","CDB.jpg","CDC.jpg","CDD.jpg"]
    ];

    static final String[][] level4 = [
            ["DAA","DAB","DAC","DAD","DAE","DAF","DAG","DAH"],
            ["DBA","DBB","DBC","DBD","DBE","DBF","DBG","DBH"],
            ["DCA","DCB","DCC","DCD","DCE","DCF","DCG","DCH"],
            ["DDA","DDB","DDC","DDD","DDE","DDF","DDG","DDH"],
            ["DEA","DEB","DEC","DED","DEE","DEF","DEG","DEH"],
            ["DFA","DFB","DFC","DFD","DFE","DFF","DFG","DFH"],
            ["DGA","DGB","DGC","DGD","DGE","DGF","DGG","DGH"],
            ["DHA","DHB","DHC","DHD","DHE","DHF","DHG","DHH"]
    ];*/

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        CommandLineParser parser = new BasicParser();

        options.addOption("h", "help", false, "print this message");
        options.addOption("fp", "filepath", true, "file with path to construct image");
        options.addOption("fi", "fileinfo", true, "file with info about image");
        options.addOption("fc", "filecoord", true, "file with info about image");
        options.addOption("p", "path", true, "path (directory) with image dotslide");

        int nbLevels = -1;
        String nameFilePath = null;
        String nameFileInfo = null;
        String nameFileCoordinates = null;
        String imagePath = null;

        try {
            CommandLine line = parser.parse( options, args );

            if(line.hasOption("filepath") && line.hasOption("fileinfo") && line.hasOption("path")) {
                nameFilePath = line.getOptionValue("filepath");
                nameFileInfo = line.getOptionValue("fileinfo");
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
        /*ArrayList<String> tiles = new ArrayList<String>();
        ArrayList<String> directories = new ArrayList<String>();*/

        for (String filename : imageDirectory.list()) {
            //Add to list
            /*if (filename.contains(".jpg"))
                tiles.add(filename);
            if (filename.contains("D"))
                directories.add(filename);*/

            if (filename.equals("ImageProperties.xml")) {
                //Seek number of level
                nbLevels = seekLevel(imagePath, filename);
            }
            if (filename.equals("ExtendedProps.xml")) {
                //Creating file with all informations about the image
                creatingTextInfo(imagePath, filename, nameFileInfo);
            }
        }

        assert(nbLevels != -1);


        String[] concernedTiles = null
        def concernedLevel = null
        if ( (nbLevels % 3) == 1) concernedLevel = level1
        if ( (nbLevels % 3) == 2) concernedLevel = level2
        if ( (nbLevels % 3) == 0) concernedLevel = level3
        assert(concernedLevel != null)

        concernedTiles = concernedLevel.flatten()

        //println "concernedTiles $concernedTiles"

        //System.out.println(nbLevels);

        int depthDirectories = (int) Math.floor((nbLevels - 1)/ 3)

        String[] tiles = getTilesAtMaximumLevel(imagePath, nbLevels, depthDirectories, concernedTiles)

        //println tiles
        def coordinates = computeCoordinatesUsingAlphabet(tiles, depthDirectories, concernedLevel, nbLevels)
        //def coordinates = seekPosition(tiles)


        //println coordinates

        int minX = coordinates.collect { it.xTotal }.min()
        int minY = coordinates.collect { it.yTotal }.min()

        if (nameFilePath && nameFileInfo) {
            try {
                String addressFile = System.getProperty("user.dir") + "/"+nameFilePath+".txt";
                //println addressFile
                if (new File(addressFile).exists()) {
                    new File(addressFile).delete()
                }
                FileWriter fw = new FileWriter(addressFile, true);
                BufferedWriter output = new BufferedWriter(fw);
                //coordinates.sort { map1, map2 -> map1.x <=> map2.x ?: map1.y <=> map2.y }

                coordinates.each {
                    output.write((it.xTotal - minX) + ";" + (it.yTotal - minY) + ";" + imagePath + File.separator + it.tilePath + "\n");
                    //output.write(it.x + ";" + it.y + ";" + imagePath + File.separator + it.tilePath + "\n");
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




        //Launch Thread
        /*try {


            (new ThreadBoucle(tiles, directories, level, imagePath, nbLevels, nameFilePath)).start();

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    static def seekPosition(String[] paths) {
        def coordinates = []
        int tileSize = 1 //1024
        paths.each { path ->
            boolean running = true;
            int[] x = new int[10];
            int[] y = new int[10];
            int xResult = 0;
            int yResult = 0;
            int z = 0;
            int level;

            //Look the position relative to the files in the path
            String[] parts = path.split("/");
            String filename
            for(int i = 0; running; i++) {
                if (!parts[i].endsWith("jpg")) {
                    for(int j = 0; j < 8; j++) {
                        for (int k = 0; k < 8; k++) {
                            if (parts[i].equals(SynchronizedFileWriter.dotSlideDirectories[j][k])) {
                                x[z] = j;
                                y[z] = k;
                                j = 7; k = 7;
                            }
                        }
                    }
                    z++;
                }
                if (parts[i].endsWith("jpg")){
                    filename = parts[i]
                    running = false;
                }
            }

            //Calculating the coordinates X
            level = 2 * z;
            for(int r = 0 ; r < z; r++) {
                int powLevel = (int) Math.pow(2,level);
                xResult += (x[r]*tileSize) * powLevel;
                level = level - 3;
            }

            //Calculating the coordinates Y
            level = 2 * z;
            for(int r = 0 ; r < z; r++) {
                int powLevel = (int) Math.pow(2,level);
                yResult += (y[r]*tileSize)*powLevel;
                level = level - 3;
            }

            def lastLevelIndices = getValueIndicesFromArray(level3, filename)
            xResult += (lastLevelIndices.i*tileSize);
            yResult += (lastLevelIndices.j*tileSize);
            //System.out.println("SeekPosition for " + path + ":" + xResult + "," + yResult + " and z : " + z);
            coordinates << [x : xResult, y : yResult, path : path]
            coordinates.sort {
                it.x
            }

        }
        return coordinates

    }

    static def computeCoordinates(String[] tilesPath, int depthDirectories,def concernedLevel, int nbLevels) {
        def coordinates = []
        tilesPath.each { tilePath ->
            //println "....tilePath $tilePath"
            String[] tilePathSplit = tilePath.split(File.separator)
            int x = 0
            int y = 0
            tilePathSplit.eachWithIndex { it, depth ->
                if (!it.endsWith("jpg")) {


                    def level4Indices = getValueIndicesFromArray(level4, it)
                    assert(level4Indices != null)
                    //println "$it [" + level4Indices + "]"
                    x += level4Indices.i
                    y += level4Indices.j

                } else {
                    def lastLevelIndices = getValueIndicesFromArray(concernedLevel, it)
                    //println "$it [" + lastLevelIndices + "]"
                    x += lastLevelIndices.i
                    y += lastLevelIndices.j

                }

            }
            def res = [x : x, y : y, tilePath : tilePath ]
            //println "=> $res"
            coordinates << res
        }
        coordinates
    }

    static def computeCoordinatesUsingAlphabet(String[] tilesPath, int depthDirectories,def concernedLevel, int nbLevels) {
        //println "depthDirectories $depthDirectories"
        def coordinates = []
        int tileSize = 1024
        tilesPath.each { tilePath ->

            //println "....tilePath $tilePath"
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
                else if (i >= 1) {
                    xTotal += _v * tileSize * Math.pow(2, zoom + ((i-1) * 3))
                }
            }
            y.reverse().eachWithIndex{ int _v, int i ->
                if (i == 0) {
                    yTotal += _v * tileSize

                }
                else if (i >= 1) {
                    yTotal += _v * tileSize * Math.pow(2, zoom + ((i-1) * 3))
                }
            }
            def res = [tilePath : tilePath, x : x, y : y, xTotal : xTotal, yTotal : yTotal ]
            //println "=> $res"
            coordinates << res
        }

        return coordinates
    }



    static def getValueIndicesFromArray(array, String value) {
        //println "Look for $value in $array"
        def indices = null
        array.eachWithIndex { row, i ->
            row.eachWithIndex { arrayValue, j ->
                //println "$arrayValue vs $value"
                if (arrayValue == value) {
                    indices = [ i : i, j : j]
                }
            }
        }
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

    public static int seekLevel(String imagePath, String nameFile) {
        int level = 0;
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
            }
        }
        catch (ParserConfigurationException e) { System.out.println("Error configuration parser ..." + e.getMessage()); }
        catch (SAXException e) { System.out.println("Error SAX : " + e.getMessage()); }
        catch (IOException e) { System.out.println("Error I/O : " + e.getMessage()); }

        return level;
    }

    public static void creatingTextInfo(String imagePath, String nameFile, String nameFileInfo) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(imagePath + "/" +nameFile));

            Element root = doc.getDocumentElement();
            String addressFile = System.getProperty("user.dir") + "/"+nameFileInfo+".txt";
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
