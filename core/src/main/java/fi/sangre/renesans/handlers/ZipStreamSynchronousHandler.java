package fi.sangre.renesans.handlers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class ZipStreamSynchronousHandler implements AutoCloseable {
    private final ExecutorService executorService;
    private final ZipOutputStream stream;
    private final Object lock;

    public ZipStreamSynchronousHandler(final ExecutorService executorService, final OutputStream stream) {
        this.executorService = executorService;
        this.stream = new ZipOutputStream(new BufferedOutputStream(stream));
        this.stream.setLevel(Deflater.BEST_COMPRESSION);
        this.lock = new Object();
    }
    
    public void writeEntry(final String entryName, final InputStream entryStream) throws IOException {
        try {
            synchronized (lock) {
                log.debug("Writing from stream to new zip entry");
                stream.putNextEntry(new ZipEntry(entryName));
                IOUtils.copy(entryStream, stream);
                stream.closeEntry();
                log.debug("Writing entry finished with success");
            }
        } finally {
            IOUtils.closeQuietly(entryStream);
        }
    }

    public CompletableFuture writeEntryAsync(final String entryName, final InputStream entryStream) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                writeEntry(entryName, entryStream);
            } catch (final IOException e) {
                log.warn("IOException thrown. Probably the output stream was closed due to some timeout", e);
                throw  new CompletionException("", e);
            }
            return true;
        }, executorService);
    }

    public void close() {
        IOUtils.closeQuietly(stream);
    }
}
