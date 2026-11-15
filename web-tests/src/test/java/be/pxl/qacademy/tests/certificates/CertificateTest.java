package be.pxl.qacademy.tests.certificates;

import be.pxl.qacademy.api.ApiClient;
import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.MyCertificatesPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import be.pxl.qacademy.support.PdfText;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-12: Als geslaagde student zie ik mijn certificaat en download ik het als PDF (cursus,
 * modules, instructor, score); bij een niet-geslaagd examen is er geen certificaat.
 */
@Tag("certificates")
@Tag("US-12")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-12 Certificaat bekijken en downloaden als PDF")
class CertificateTest extends BaseUiTest {

    private TestCourse courseWithModules(String label) {
        return aCourse().titled(label).free()
                .withModule("Certificaatmodule Een", "Basis")
                .withModule("Certificaatmodule Twee", "Verdieping")
                .create();
    }

    @Test
    @DisplayName("Geslaagd: kaart toont cursus, score, modules en instructor")
    void passedStudentSeesCertificateCard() {
        TestCourse course = courseWithModules("Cert card");
        api.subscribe(TestUser.STUDENT_1, course.id());
        api.recordExamResult(TestUser.INSTRUCTOR, course.id(), api.userId(TestUser.STUDENT_1), 88, true);

        MyCertificatesPage certificates = new MyCertificatesPage(pageAs(TestUser.STUDENT_1)).open();

        assertThat(certificates.card(course.id())).isVisible();
        assertThat(certificates.courseTitle(course.id())).hasText(course.title());
        assertThat(certificates.score(course.id())).hasText("88");
        assertThat(certificates.modules(course.id())).containsText("Certificaatmodule Een");
        assertThat(certificates.modules(course.id())).containsText("Certificaatmodule Twee");
        assertThat(certificates.instructors(course.id())).containsText(api.displayName(TestUser.INSTRUCTOR));
        assertThat(certificates.number(course.id())).not().isEmpty();
    }

    @Test
    @DisplayName("Geslaagd: de PDF bevat cursusnaam, beide modules, de instructor en de score")
    void downloadedPdfContainsAllDetails() {
        TestCourse course = courseWithModules("Cert pdf");
        api.subscribe(TestUser.STUDENT_1, course.id());
        api.recordExamResult(TestUser.INSTRUCTOR, course.id(), api.userId(TestUser.STUDENT_1), 91, true);

        MyCertificatesPage certificates = new MyCertificatesPage(pageAs(TestUser.STUDENT_1)).open();
        String number = certificates.number(course.id()).innerText().trim();
        MyCertificatesPage.DownloadedFile pdf = certificates.download(course.id());

        Assertions.assertThat(pdf.fileName()).isEqualTo("certificate-" + number + ".pdf");
        Assertions.assertThat(PdfText.isPdf(pdf.bytes())).as("bestand begint met %PDF").isTrue();
        String text = PdfText.extract(pdf.bytes());
        Assertions.assertThat(text)
                .contains(course.title())
                .contains("Certificaatmodule Een")
                .contains("Certificaatmodule Twee")
                .contains(api.displayName(TestUser.INSTRUCTOR))
                .contains("91");
    }

    @Test
    @DisplayName("Niet geslaagd: geen certificaatkaart en de PDF-download wordt geweigerd")
    void failedStudentHasNoCertificate() {
        TestCourse course = courseWithModules("Cert failed");
        api.subscribe(TestUser.STUDENT_2, course.id());
        api.recordExamResult(TestUser.INSTRUCTOR, course.id(), api.userId(TestUser.STUDENT_2), 42, false);

        MyCertificatesPage certificates = new MyCertificatesPage(pageAs(TestUser.STUDENT_2)).open();

        assertThat(certificates.card(course.id())).hasCount(0);
        ApiClient.Response download = api.downloadCertificate(TestUser.STUDENT_2, course.id());
        Assertions.assertThat(download.status()).as("PDF van een niet-geslaagd examen").isIn(403, 404, 409);
        Assertions.assertThat(PdfText.isPdf(download.bytes())).isFalse();
    }

    @Test
    @DisplayName("Zonder examenresultaat: geen certificaat")
    void noResultMeansNoCertificate() {
        TestCourse course = courseWithModules("Cert none");
        api.subscribe(TestUser.STUDENT_3, course.id());

        MyCertificatesPage certificates = new MyCertificatesPage(pageAs(TestUser.STUDENT_3)).open();

        assertThat(certificates.card(course.id())).hasCount(0);
    }

    @Test
    @DisplayName("Een andere student kan het certificaat van een geslaagde student niet downloaden")
    void certificateIsPersonal() {
        TestCourse course = courseWithModules("Cert personal");
        api.subscribe(TestUser.STUDENT_1, course.id());
        api.recordExamResult(TestUser.INSTRUCTOR, course.id(), api.userId(TestUser.STUDENT_1), 77, true);

        MyCertificatesPage other = new MyCertificatesPage(pageAs(TestUser.STUDENT_3)).open();

        assertThat(other.card(course.id())).hasCount(0);
        Assertions.assertThat(api.downloadCertificate(TestUser.STUDENT_3, course.id()).ok()).isFalse();
    }

    @Test
    @DisplayName("Resultaat gecorrigeerd van geslaagd naar niet geslaagd: het certificaat verdwijnt")
    void certificateDisappearsWhenResultIsCorrectedToFailed() {
        TestCourse course = courseWithModules("Cert revoked");
        String student = api.userId(TestUser.STUDENT_1);
        api.subscribe(TestUser.STUDENT_1, course.id());
        api.recordExamResult(TestUser.INSTRUCTOR, course.id(), student, 70, true);
        api.recordExamResult(TestUser.INSTRUCTOR, course.id(), student, 30, false);

        MyCertificatesPage certificates = new MyCertificatesPage(pageAs(TestUser.STUDENT_1)).open();

        assertThat(certificates.card(course.id())).hasCount(0);
    }
}
