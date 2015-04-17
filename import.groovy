import be.cytomine.client.*
import be.cytomine.client.models.*
import be.cytomine.client.collections.*

def cytomineHost = "http://shareview.ecampus.ulg.ac.be"
def uploadHost = "http://storage.shareview.ecampus.ulg.ac.be"
String publickey = args[0]
String privatekey = args[1]
Long idStorage = 8275040l //user vmartin storage
Cytomine cytomine = new Cytomine(uploadHost, publickey, privatekey, "./");

//Get argument
Long idProject = Long.parseLong(args[0])
def directoryName = args[1]
boolean onlyUpload = args.length==3 && args[2].equals("ONLYUPLOAD")

if(!onlyUpload) {
	println "Generate metadata files about tiles...(a few seconds)"
	executeOnShell("java -jar lib/dotslideTool_good.jar -fi images/${directoryName}/fi -fp images/${directoryName}/fp -p images/${directoryName}/")

	println "Build image...(a few minutes/hours)"
	executeOnShell("java -jar lib/dotslideBuildTool_good.jar -f images/${directoryName}/fp.txt -io images/${directoryName}/${directoryName}")
}

println "upload on shareview (a few minutes)"
def json = cytomine.uploadImage("images/${directoryName}/${directoryName}.tif",idProject,idStorage,cytomineHost);
println "IMAGE UPLOADED!"
println json
println "IMAGE MUST NOW BE CONVERTED IN THE STORAGE. THIS MAY TAKE A FEW MINUTES/HOURS..."


println "Script is finished..."
def executeOnShell(String command) {
    return executeOnShell(command, new File("./"))
}

def executeOnShell(String command, File workingDir) {
    println command
    def process = new ProcessBuilder(addShellPrefix(command))
            .directory(workingDir)
            .redirectErrorStream(true)
            .start()
    process.inputStream.eachLine { println it }
    process.waitFor();
    return process.exitValue()
}

def addShellPrefix(String command) {
    String[] commandArray = new String[3]
    commandArray[0] = "sh"
    commandArray[1] = "-c"
    commandArray[2] = command
    return commandArray
}

