package be.pxl.qacademy.pages;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** "My certificates": certificaten van geslaagde cursussen, met PDF-download. */
public class MyCertificatesPage extends BasePage {

    public MyCertificatesPage(Page page) {
        super(page);
    }

    public MyCertificatesPage open() {
        open("/my-certificates");
        assertThat(byTestId("my-certificates-page")).isVisible();
        waitUntilLoaded();
        return this;
    }

    public Locator card(String courseId) {
        return byTestId("certificate-card-" + courseId);
    }

    public Locator courseTitle(String courseId) {
        return byTestId("certificate-course-" + courseId);
    }

    public Locator number(String courseId) {
        return byTestId("certificate-number-" + courseId);
    }

    public Locator score(String courseId) {
        return byTestId("certificate-score-" + courseId);
    }

    public Locator modules(String courseId) {
        return byTestId("certificate-modules-" + courseId);
    }

    public Locator instructors(String courseId) {
        return byTestId("certificate-instructors-" + courseId);
    }

    public Locator downloadButton(String courseId) {
        return byTestId("certificate-download-" + courseId);
    }

    public Locator emptyMessage() {
        return byTestId("empty-certificates-message");
    }

    /** Klikt op "Download PDF" en geeft de bytes van het gedownloade bestand terug. */
    public DownloadedFile download(String courseId) {
        Download download = page.waitForDownload(() -> downloadButton(courseId).click());
        try {
            Path path = download.path();
            return new DownloadedFile(download.suggestedFilename(), Files.readAllBytes(path));
        } catch (IOException e) {
            throw new IllegalStateException("Kan download niet lezen", e);
        }
    }

    public record DownloadedFile(String fileName, byte[] bytes) {
    }
}
