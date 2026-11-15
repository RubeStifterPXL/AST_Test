package be.pxl.qacademy.tests.certificates;

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
 * US-12 (tweede aanpak): Als geslaagde student zie ik mijn certificaat en download ik het als PDF.
 *
 * <p>Andere invalshoek dan {@link CertificateTest}: de regels rond een certificaat. "Geslaagd"
 * beslist, niet de score. Meerdere certificaten krijgen elk een eigen nummer en een eigen PDF.
 * Een cursus zonder modules levert nog altijd een geldige PDF op.</p>
 */
@Tag("certificates")
@Tag("US-12")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-12 Certificaatregels: geslaagd beslist, uniek nummer, eigen PDF per cursus")
class CertificateRulesTest extends BaseUiTest {

    @Test
    @DisplayName("Score 100 maar niet geslaagd: geen certificaat (het resultaat 'Passed' beslist)")
    void perfectScoreWithoutPassIsNoCertificate() {
        TestCourse course = aCourse().titled("Rules score").free().withModule("Regels", "Score").create();
        api.subscribe(TestUser.STUDENT_3, course.id());
        api.recordExamResult(TestUser.INSTRUCTOR, course.id(), api.userId(TestUser.STUDENT_3), 100, false);

        MyCertificatesPage certificates = new MyCertificatesPage(pageAs(TestUser.STUDENT_3)).open();

        assertThat(certificates.card(course.id())).hasCount(0);
    }

    @Test
    @DisplayName("Twee geslaagde cursussen: twee kaarten, verschillende certificaatnummers en elk de juiste PDF")
    void twoCertificatesAreDistinct() {
        TestCourse first = aCourse().titled("Rules first").free().withModule("Eerste cursus module", "A").create();
        TestCourse second = aCourse().titled("Rules second").free().withModule("Tweede cursus module", "B").create();
        String student = api.userId(TestUser.STUDENT_2);
        api.subscribe(TestUser.STUDENT_2, first.id());
        api.subscribe(TestUser.STUDENT_2, second.id());
        api.recordExamResult(TestUser.INSTRUCTOR, first.id(), student, 81, true);
        api.recordExamResult(TestUser.INSTRUCTOR, second.id(), student, 93, true);

        MyCertificatesPage certificates = new MyCertificatesPage(pageAs(TestUser.STUDENT_2)).open();
        String firstNumber = certificates.number(first.id()).innerText().trim();
        String secondNumber = certificates.number(second.id()).innerText().trim();
        Assertions.assertThat(firstNumber).isNotBlank().isNotEqualTo(secondNumber);

        String firstPdf = PdfText.extract(certificates.download(first.id()).bytes());
        String secondPdf = PdfText.extract(certificates.download(second.id()).bytes());
        Assertions.assertThat(firstPdf).contains(first.title()).contains("Eerste cursus module").contains("81")
                .doesNotContain(second.title());
        Assertions.assertThat(secondPdf).contains(second.title()).contains("Tweede cursus module").contains("93")
                .doesNotContain(first.title());
    }

    @Test
    @DisplayName("De PDF vermeldt de naam van de student zelf")
    void pdfNamesTheStudent() {
        TestCourse course = aCourse().titled("Rules name").free().withModule("Naammodule", "C").create();
        api.subscribe(TestUser.STUDENT_1, course.id());
        api.recordExamResult(TestUser.INSTRUCTOR, course.id(), api.userId(TestUser.STUDENT_1), 75, true);

        MyCertificatesPage certificates = new MyCertificatesPage(pageAs(TestUser.STUDENT_1)).open();
        String text = PdfText.extract(certificates.download(course.id()).bytes());

        Assertions.assertThat(text).contains(api.displayName(TestUser.STUDENT_1));
    }

    @Test
    @DisplayName("Cursus zonder modules: het certificaat bestaat en de kaart toont '-' bij Modules")
    void courseWithoutModulesStillGivesCertificate() {
        TestCourse course = aCourse().titled("Rules no modules").free().create();
        api.subscribe(TestUser.STUDENT_1, course.id());
        api.recordExamResult(TestUser.INSTRUCTOR, course.id(), api.userId(TestUser.STUDENT_1), 60, true);

        MyCertificatesPage certificates = new MyCertificatesPage(pageAs(TestUser.STUDENT_1)).open();

        assertThat(certificates.modules(course.id())).hasText("-");
        Assertions.assertThat(PdfText.isPdf(certificates.download(course.id()).bytes())).isTrue();
    }
}
