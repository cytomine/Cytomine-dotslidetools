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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: pierre ansen
 * Date: 13/03/13
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */

public class SynchronizedFileWriter {
    private static String[][] dotSlideDirectories = [
                                        ["DAA","DBA","DCA","DDA","DEA","DFA","DGA","DHA"],
                                        ["DAB","DBB","DCB","DDB","DEB","DFB","DGB","DHB"],
                                        ["DAC","DBC","DCC","DDC","DEC","DFC","DGC","DHC"],
                                        ["DAD","DBD","DCD","DDD","DED","DFD","DGD","DHD"],
                                        ["DAE","DBE","DCE","DDE","DEE","DFE","DGE","DHE"],
                                        ["DAF","DBF","DCF","DDF","DEF","DFF","DGF","DHF"],
                                        ["DAG","DBG","DCG","DDG","DEG","DFG","DGG","DHG"],
                                        ["DAH","DBH","DCH","DDH","DEH","DFH","DGH","DHH"]];

    /*private static ArrayList<Integer> listX = new ArrayList<Integer>();
    private static ArrayList<Integer> listY = new ArrayList<Integer>();*/

    public static synchronized void writeFile(String content, String filename) {

        String addressFile = System.getProperty("user.dir") + "/"+filename+".txt";
        //System.out.println("write " + content + " into " + filename + " ( " + addressFile + " ) ");


        //Seeking image's position relative to the primary image
        String position = seekPosition(content);

        //Write the path and the position in a file
        try {
            FileWriter fw = new FileWriter(addressFile, true);
            BufferedWriter output = new BufferedWriter(fw);

            output.write(position + " " + content + "\n");
            output.flush();
            output.close();
        }
        catch(IOException ioe){
            System.out.print("Error : ");
            ioe.printStackTrace();
        }

        //On cherche le min X et le max X
        //Pareil pour le min Y et le max Y
        /*String[] part = position.split(";");
        listX.add(Integer.parseInt(part[0]));
        listY.add(Integer.parseInt(part[1]));*/
    }

    public static String _seekPosition(String path) {

        System.out.println(path);

        return "-";
    }

    public static String seekPosition(String path) {
        boolean running = true;
        int[] x = new int[10];
        int[] y = new int[10];
        int xResult = 0;
        int yResult = 0;
        int z = 0;
        int level;

        //Look the position relative to the files in the path
        String[] parts = path.split("/");
        for(int i = 0; running; i++) {
            if (parts[i].contains("D")) {
                for(int j = 0; j < 8; j++) {
                    for (int k = 0; k < 8; k++) {
                        if (parts[i].equals(dotSlideDirectories[j][k])) {
                            x[z] = j;
                            y[z] = k;
                            j = 7; k = 7;
                        }
                    }
                }
                z++;
            }
            if (parts[i].equals("AAA.jpg"))
                running = false;
        }

        //Calculating the coordinates X
        level = 2 * z;
        for(int r = 0 ; r < z; r++) {
            int powLevel = (int) Math.pow(2,level);
            xResult += (x[r]*1024) * powLevel;
            level = level - 3;
        }

        //Calculating the coordinates Y
        level = 2 * z;
        for(int r = 0 ; r < z; r++) {
            int powLevel = (int) Math.pow(2,level);
            yResult += (y[r]*1024)*powLevel;
            level = level - 3;
        }
        System.out.println("SeekPosition for " + path + ":" + xResult + "," + yResult + " and z : " + z);
        return (xResult+";"+yResult+";");
    }
}
