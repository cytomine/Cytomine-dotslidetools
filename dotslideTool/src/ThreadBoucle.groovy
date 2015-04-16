import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: pierre
 * Date: 12/03/13
 * Time: 12:16
 * To change this template use File | Settings | File Templates.
 */

public class ThreadBoucle extends Thread
{
    //All images and folders
    public String[] level1 = ["AAA.jpg"];
    public String[][] level2 = [
            ["BAA.jpg","BBA.jpg"],
            ["BAB.jpg","BBB.jpg"]
    ];

    public String[][] level3 = [["CAA.jpg","CBA.jpg","CCA.jpg","CDA.jpg"],
            ["CAB.jpg","CBB.jpg","CCB.jpg","CDB.jpg"],
            ["CAC.jpg","CBC.jpg","CCC.jpg","CDC.jpg"],
            ["CAD.jpg","CBD.jpg","CCD.jpg","CDD.jpg"]
    ];

    public String[][] level4 = [["DAA","DBA","DCA","DDA","DEA","DFA","DGA","DHA"],
            ["DAB","DBB","DCB","DDB","DEB","DFB","DGB","DHB"],
            ["DAC","DBC","DCC","DDC","DEC","DFC","DGC","DHC"],
            ["DAE","DBE","DCE","DDE","DEE","DFE","DGE","DHE"],
            ["DAD","DBD","DCD","DDD","DED","DFD","DGD","DHD"],
            ["DAF","DBF","DCF","DDF","DEF","DFF","DGF","DHF"],
            ["DAG","DBG","DCG","DDG","DEG","DFG","DGG","DHG"],
            ["DAH","DBH","DCH","DDH","DEH","DFH","DGH","DHH"]];

    public ArrayList<String> tiles = new ArrayList<String>();
    public ArrayList<String> directories = new ArrayList<String>();
    public int level;
    public String currentPath;
    public int nbLevels;
    public String filePath;

    public ThreadBoucle(ArrayList<String> tiles, ArrayList<String> directories, int level, String currentPath, int nbLevels, String filePath) {
        this.tiles = tiles;
        this.directories = directories;
        this.level = level;
        this.currentPath = currentPath;
        this.nbLevels = nbLevels;
        this.filePath = filePath;
    }

    public void run() {
        //System.out.println("run in " + currentPath + " with level " + level);
        File file;
        int currentLevel = level;
        String currentPathBis = "";

        def level2Flat = level2.flatten()
        def level3Flat = level3.flatten()

        int cRow = 0, cRowMax = 0;
        int cCol = 0, cColMax = 0, cColTemp = 0;
        int dRow = 0, dRowMax = 0;
        int dCol = 0, dColMax = 0, dColTemp = 0;

        for (int aRow = 0; aRow < 2; aRow++) {
            for (int aCol = 0; aCol < 2; aCol++) {      //aRow = 0 & aCol = 0
                if (aRow==0) {                          /*****************/
                    cRow = 0;                           /* ***** *       */
                    cRowMax = aRow + 2;                 /* ***** *       */
                } else {                                /*****************/      //tiles B .. ..
                    cRow = 2;                           /*       *       */
                    cRowMax = cRow + 2;                 /*       *       */
                }                                       /*****************/

                if (aCol==0) {
                    cCol = 0;
                    cColTemp = cCol;
                    cColMax = aCol + 2;
                } else {
                    cCol = 2;
                    cColTemp = cCol;
                    cColMax = cCol + 2;                         // ***** : cRow = 0 , cRowMax = 2 & cCol = 0 , cColMax = 2
                }                                               // ///// : cRow = 0 , cRowMax = 2 & cCol = 1 , cColMax = 2
                // \\\\\ : cRow = 1 , cRowMax = 2 & cCol = 0 , cColMax = 2
                while (cRow < cRowMax) {                        // ----- : cRow = 1 , cRowMax = 2 & cCol = 1 , cColMax = 2
                    cCol = cColTemp;                            /*********************************/
                    while (cCol < cColMax) {                    /* ***** * ///// *       *       */
                        if (cRow==0) {                          /* ***** * ///// *       *       */
                            dRow = 0;                           /*********************************/
                            dRowMax = cRow + 2;                 /* \\\\\ * ----- *       *       */
                        } else {                                /* \\\\\ * ----- *       *       */
                            dRow = cRow + cRow;                 /*********************************/      //tiles C .. ..
                            dRowMax = dRow + 2;                 /*       *       *       *       */
                        }                                       /*       *       *       *       */
                        /*********************************/
                        if (cCol==0) {                          /*       *       *       *       */
                            dCol = 0;                           /*       *       *       *       */
                            dColTemp = dCol;                    /*********************************/
                            dColMax = cCol + 2;
                        } else {
                            dCol = cCol + cCol;                                                                                     // ***** : dRow = 0->1 , dRowMax = 2 & dCol = 0->1 , dColMax = 2
                            dColTemp = dCol;                                                                                        // ///// : dRow = 0->1 , dRowMax = 2 & dCol = 2->3 , dColMax = 4
                            dColMax = dCol + 2;                                                                                     // \\\\\ : dRow = 2->3 , dRowMax = 4 & dCol = 0->1 , dColMax = 2
                        }                                                                                                           // ----- : dRow = 2->3 , dRowMax = 4 & dCol = 2->3 , dColMax = 4
                        /****************************************************************/
                        while (dRow < dRowMax) {                                                                                    /* ***** * ***** * ///// * ///// *       *       *       *      */
                            dCol = dColTemp;                                                                                        /* ***** * ***** * ///// * ///// *       *       *       *      */
                            while (dCol < dColMax) {                                                                                /****************************************************************/
                                if (tiles.contains(level1[0])) {                                                                       /* ***** * ***** * ///// * ///// *       *       *       *      */
                                    level++;                                                                                       /* ***** * ***** * ///// * ///// *       *       *       *      */

                                    System.out.println("level : " + level + " (nbLevels : " + nbLevels +") with currentPath " + this.currentPath); /****************************************************************/
                                    //Means that is the final image                                                                 /* \\\\\ * \\\\\ * ----- * ----- *       *       *       *      */
                                    if(level >= nbLevels - 2) {                                                                           /* \\\\\ * \\\\\ * ----- * ----- *       *       *       *      */
                                        //Option 1 : only level1 image : AAA.JPG, finalLevel = level
                                        //Option 2 : only level2 image : B**.JPG, finalLevel = level + 1
                                        //Option 3 : only level2 image : C**.JPG, finalLevel = level + 2

                                        //boolean tilesFound = false;
                                        //int localLevel = 1; //by default LEVEL1 (AAA.jpg)
                                        String[] possibleTiles = null;
                                        //check if we bound B** images
                                        if (new File(currentPath).isDirectory()) {
                                            for (String filename : new File(currentPath).list()) {
                                                for (String tileName : level3Flat) {
                                                    if (filename.equals(tileName)) { //LEVEL + 2, inject C**.jpg images
                                                        possibleTiles = level3Flat;
                                                        break;
                                                    }
                                                }

                                                for (String tileName : level2Flat) {
                                                    if (possibleTiles != null) break;
                                                    if (filename.equals(tileName)) { //LEVEL + 1, inject B**.jpg images
                                                        possibleTiles = level3Flat;
                                                        break;
                                                    }

                                                }
                                            }

                                            if (possibleTiles == null) possibleTiles = level1;

                                            for (String tileName : possibleTiles) {
                                                String fullTileFilename = currentPath + "/" + tileName;
                                                System.out.println("add " + fullTileFilename);
                                                if (new File(fullTileFilename).exists()) {
                                                    SynchronizedFileWriter.writeFile(fullTileFilename, filePath);
                                                }
                                            }
                                        } else {
                                            System.out.println("is not a  " +  currentPath + " directory ");
                                        }



                                        //Write the path in aRow file                                                               /****************************************************************/
                                                                                           /* \\\\\ * \\\\\ * ----- * ----- *       *       *       *      */
                                        aRow = 1; aCol = 1; cRow = 3; cCol = 3; dRow = 7; dCol = 7;                                 /* \\\\\ * \\\\\ * ----- * ----- *       *       *       *      */
                                    } else {                                                                                        /****************************************************************/       //Directory D .. ..
                                        if (tiles.contains(level2[aRow][aCol])) {                                                   /*       *       *       *       *       *       *       *      */
                                            level++;                                                                               /*       *       *       *       *       *       *       *      */
                                            if (tiles.contains(level3[cRow][cCol])) {                                               /****************************************************************/
                                                level++;                                                                           /*       *       *       *       *       *       *       *      */
                                                if (directories.contains(level4[dRow][dCol])) {                                        /*       *       *       *       *       *       *       *      */
                                                    //The next directory                                                            /****************************************************************/
                                                    currentPathBis = this.currentPath + "/" + level4[dRow][dCol];                                  /*       *       *       *       *       *       *       *      */
                                                                                                                                    /*       *       *       *       *       *       *       *      */
                                                    file = new File(currentPathBis);                                                     /****************************************************************/
                                                                                                                                    /*       *       *       *       *       *       *       *      */
                                                    ArrayList<String> listImageTemp = new ArrayList<String>();                          /*       *       *       *       *       *       *       *      */
                                                    ArrayList<String> listDossierDTemp = new ArrayList<String>();                       /****************************************************************/

                                                    for (String temp : file.list()) {
                                                        //Add to the list
                                                        if (temp.contains(".jpg"))
                                                            listImageTemp.add(temp);
                                                        else if (temp.contains("D"))
                                                            listDossierDTemp.add(temp);
                                                    }

                                                    //Launch aRow new thread with the new image and directory.
                                                    (new ThreadBoucle(listImageTemp, listDossierDTemp, level, currentPathBis, nbLevels, filePath)).start();
                                                }
                                            }
                                        }
                                    }
                                }
                                level = currentLevel;
                                dCol++;
                            }
                            dRow++;
                        }
                        cCol++;
                    }
                    cRow++;
                }
            }
        }
    }
}
