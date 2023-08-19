import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class PDFSplitter {
    private String filePath;
    private List<Long> pageOffsets;

    public PDFSplitter(String filePath) {
        this.filePath = filePath;
        this.pageOffsets = new ArrayList<>();
        findPageOffsets();
    }

    private void findPageOffsets() {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            long fileLength = raf.length();
            byte[] buffer = new byte[1024];
            int bytesRead;
            long position = 0;

            while (position < fileLength) {
                raf.seek(position);
                bytesRead = raf.read(buffer);

                for (int i = 0; i < bytesRead; i++) {
                    // Check for the PDF page header (bytes: %PDF-)
                    if (buffer[i] == 0x25 && i + 4 < bytesRead &&
                            buffer[i + 1] == 0x50 && buffer[i + 2] == 0x44 &&
                            buffer[i + 3] == 0x46 && buffer[i + 4] == 0x2D) {
                        pageOffsets.add(position + i);
                    }
                }
                position += bytesRead;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void split(int numPages) {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            FileChannel channel = raf.getChannel();
            String outputFilePrefix = "output_page_";
            int pageNumber = 1;

            for (int i = 0; i < numPages && i < pageOffsets.size() - 1; i++) {
                long startOffset = pageOffsets.get(i);
                long endOffset = pageOffsets.get(i + 1);

                long outputSize = endOffset - startOffset;
                File outputFile = new File(outputFilePrefix + pageNumber + ".pdf");
                try (RandomAccessFile outputRaf = new RandomAccessFile(outputFile, "rw")) {
                    FileChannel outputChannel = outputRaf.getChannel();
                    channel.transferTo(startOffset, outputSize, outputChannel);
                }
                pageNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String filePath = "C:/javaprg/code clause/input.pdf"; // Replace with the actual file path of the input PDF
        int numPagesToSplit = 5; // Specify the number of pages to split the PDF into
        PDFSplitter pdfSplitter = new PDFSplitter(filePath);
        pdfSplitter.split(numPagesToSplit);
    }
}
