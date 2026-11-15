package be.pxl.qacademy.support;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

/** Leest de tekst uit een PDF (bv. een gedownload certificaat). */
public final class PdfText {

    private PdfText() {
    }

    public static String extract(byte[] pdf) {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);
            // regeleinden en dubbele spaties normaliseren, zodat "Foundations of\nTesting" matcht
            return text.replaceAll("\\s+", " ").trim();
        } catch (IOException e) {
            throw new IllegalStateException("Geen geldige PDF (" + pdf.length + " bytes)", e);
        }
    }

    public static boolean isPdf(byte[] bytes) {
        return bytes.length > 4 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';
    }
}
