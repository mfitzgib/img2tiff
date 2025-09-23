import qupath.lib.images.ImageData
import qupath.lib.images.servers.ImageServerBuilder
import qupath.lib.projects.Project
import qupath.lib.projects.ProjectIO
import qupath.lib.images.writers.ome.OMETiffWriter
import qupath.lib.images.writers.ome.OMEPyramidWriter

// Convert VSI image to OME-TIFF pyramid

String makeOutputPrefix(String outputFolder, String inputPath, String series, String name) {
    if (!inputPath.endsWith('.vsi')) {
        throw new IllegalArgumentException("Input must be a VSI image with .vsi suffix (received ${inputPath})")
    }
    def suffix = !name.endsWith(".vsi") || name.contains(" - ") ? name.split(" - ")[1].replace(" ", "_") : series
    def outputName = inputPath.replaceFirst(/.*\//, '').replaceFirst(/\.vsi$/, '.' + suffix)
    return outputFolder.endsWith(File.separator) ? outputFolder + outputName : outputFolder + File.separator + outputName
}

def writeOmeTiff(ImageServer server, String outputPath) {
    // single JPEG plane not currently used
    try {
        def writer = new OMETiffWriter()
        println "Writing OME-TIFF to: ${outputPath}"
        writer.writeImage(server, outputPath)
        println "Write complete: ${outputPath}"
    } catch (Exception e) {
        println "Error writing OME-TIFF: ${e.message}"
    }
}

def writeOmePyramid(ImageServer server, String outputPath) {
    // Configuration for OME Pyramid TIFF
    def tileSize = 512
    def nThreads = 2
    def compression = OMEPyramidWriter.CompressionType.LZW  // Use LZW. Also  //ZLIB //UNCOMPRESSED (not working with 32bit: //J2K_LOSSY //J2K)

    try {
        def writer = new OMEPyramidWriter.Builder(server)
            .compression(compression)
            .parallelize(nThreads)
            .channelsInterleaved()
            .tileSize(tileSize)
            .downsamples(1.0, 2.0, 4.0, 8.0)
            .build()
        writer.writePyramid(outputPath)
        println "OME Pyramid TIFF written to: ${outputPath}"
    } catch (Exception e) {
        println "Error writing OME Pyramid TIFF: ${e.message}"
    }
}

def checkFileExistence(String outputFilename) {
    def outputFile = new File(outputFilename)
    if (outputFile.exists()) {
        throw new IllegalArgumentException("File already exists: ${outputFile.absolutePath}")
    }
}

def buildImageServer(String inputFilename, String series) {
    try {
        return buildServer(inputFilename, "--series", series)
    } catch (Exception e) {
        throw new IllegalArgumentException("Error building image server: ${e.message}")
    }
}

def convert(String[] args) {

    if (args.size() < 1) {
        throw new IllegalArgumentException("Input filename is required")
    }
    println "Input Arguments: ${args}"

    String inputFilename = args[0]
    String outputFolder = args[1]

    println "Input Filename: ${inputFilename}"
    Boolean keep_going = true

    for (int s = 0; keep_going; s++) {
        String series = s
        try {
            convertSeries(outputFolder, inputFilename, series)
        } catch (IllegalArgumentException e) {
            println "Stopping iteration"
            keep_going = false
        }
    }

}

def saveMetadata(ImageServer server, String outputPrefix) {
    def metadata = server.getMetadata()
    def jsonFilename = outputPrefix + ".metadata.json"
    def file = new File(jsonFilename)
    file.withWriter { writer ->
        writer.println(metadata)
    }
    println "Metadata saved to: ${jsonFilename}"
}

def convertSeries(String outputFolder, String inputFilename, String series) {

    println "Converting series ${series}"

    def server = buildImageServer(inputFilename, series)

    name = server.getMetadata()["name"]
    String outputPrefix = makeOutputPrefix(outputFolder, inputFilename, series, name)

    // Save the series metadata to JSON
    saveMetadata(server, outputPrefix)

    def outputFilename = outputPrefix + ".ome.tif"

    println "Writing to ${outputFilename}"

    // Include the series number in the output file name
    checkFileExistence(outputFilename)
    println "Output Filename: ${outputFilename}"

    // Write image as OME TIFF Pyramid
    writeOmeTiff(server, outputFilename)
}

// Run the main function, renamed to avoid collision
convert(args)

