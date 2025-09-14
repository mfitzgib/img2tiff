import qupath.lib.images.ImageData
import qupath.lib.images.servers.ImageServerBuilder
import qupath.lib.projects.Project
import qupath.lib.projects.ProjectIO
import qupath.lib.images.writers.ome.OMETiffWriter
import qupath.lib.images.writers.ome.OMEPyramidWriter

String make_output_filename(String inputPath) {
    if (!inputPath.endsWith('.vsi')) {
	throw new IllegalArgumentException("Input must be a VSI image with .vsi suffix (received ${inputPath})")
    }
    return inputPath.replaceFirst(/\.vsi$/, '.ome.tif')
}

def write_ome_jpeg(server, outputPath) {
    def writer = new OMETiffWriter()
    println('Writing OME-TIFF ' + outputPath)
    writer.writeImage(server, outputPath)
    println('Done:' + outputPath)
}

def write_ome_lzw(server, outputPath) {
    def tilesize = 512
    def outputDownsample = 1
    def pyramidscaling = 2
    def nThreads = 2
    def compression = OMEPyramidWriter.CompressionType.LZW  //ZLIB //UNCOMPRESSED //LZW  (not working with 32bit: //J2K_LOSSY //J2K)

    def outputPath2 = "second.ome.tif"
    new OMEPyramidWriter.Builder(server)
	.compression(compression)
	.parallelize(nThreads)
	.channelsInterleaved()     
	.tileSize(tilesize)
	.scaledDownsampling(outputDownsample, pyramidscaling)
	.build()
	.writePyramid(outputPath2)

    println('Done:' + outputPath2)
}

def inputFilename = args[0]
def outputFilename =  make_output_filename(inputFilename)
def series = args.size() > 1 ? args[1] : '0'

println inputFilename
println outputFilename
println series

outputFile = new File(outputFilename)
if (outputFile.exists()) {
    throw new IllegalArgumentException("File already exists: ${outputFile.absolutePath}")
}
def server = buildServer(inputFilename, "--series", series)
write_ome_jpeg(server, outputFilename)
