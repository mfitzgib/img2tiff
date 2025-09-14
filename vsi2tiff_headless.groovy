import qupath.lib.images.ImageData
import qupath.lib.images.servers.ImageServerBuilder
import qupath.lib.projects.Project
import qupath.lib.projects.ProjectIO
import qupath.lib.images.writers.ome.OMETiffWriter
import qupath.lib.images.writers.ome.OMEPyramidWriter

// Converts VSI image to OME-TIFF format (pyramid, JPEG pending)
// Arguments:
//  - param1: input VSI image path
//  - param2: series number (optional, defaults to 0)

String makeOutputFilename(String inputPath) {
    if (!inputPath.endsWith('.vsi')) {
        throw new IllegalArgumentException("Input must be a VSI image with .vsi suffix (received ${inputPath})")
    }
    return inputPath.replaceFirst(/\.vsi$/, '.ome.tif')
}

def writeOmeTiff(ImageServer server, String outputPath) {
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
    def minDownsample = 1
    def pyramidScale = 2
    def nThreads = 2
    def compression = OMEPyramidWriter.CompressionType.LZW  // Use LZW. Also  //ZLIB //UNCOMPRESSED (not working with 32bit: //J2K_LOSSY //J2K)

    try {
        def writer = new OMEPyramidWriter.Builder(server)
            .compression(compression)
            .parallelize(nThreads)
            .channelsInterleaved()
            .tileSize(tileSize)
            .scaledDownsampling(minDownsample, pyramidScale)
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

    String inputFilename = args[0]
    String outputFilename = makeOutputFilename(inputFilename)
    String series = args.size() > 1 ? args[1] : '1' 

    println "Input Filename: ${inputFilename}"
    println "Output Filename: ${outputFilename}"
    println "Series: ${series}"

    // Ensure output file doesn't already exist
    checkFileExistence(outputFilename)

    // Build ImageServer for input file and series
    def server = buildImageServer(inputFilename, series)

    // Write image in desired format
    writeOmeTiff(server, outputFilename)
}

// Execute the main function with provided arguments
convert(args)

