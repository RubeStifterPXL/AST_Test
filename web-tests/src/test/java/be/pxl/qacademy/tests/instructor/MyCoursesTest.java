package be.pxl.qacademy.tests.instructor;

import be.pxl.qacademy.config.TestUser;
import be.pxl.qacademy.factories.CourseBuilder;
import be.pxl.qacademy.factories.TestCourse;
import be.pxl.qacademy.fixtures.BaseUiTest;
import be.pxl.qacademy.pages.MyCoursesPage;
import be.pxl.qacademy.support.Author;
import be.pxl.qacademy.support.Authors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * US-13: Als instructor zie ik enkel de cursussen waaraan ik ben toegewezen (pagina My courses).
 */
@Tag("instructor")
@Tag("US-13")
@Author(Authors.TO_ASSIGN)
@DisplayName("US-13 My courses toont enkel toegewezen cursussen")
class MyCoursesTest extends BaseUiTest {

    @Test
    @DisplayName("Toegewezen cursus staat erin met titel en status, een niet-toegewezen cursus niet")
    void onlyAssignedCoursesAreListed() {
        TestCourse assigned = aCourse().titled("Assigned").create();
        TestCourse notAssigned = aCourse().titled("Not assigned").withoutInstructor().create();

        MyCoursesPage myCourses = new MyCoursesPage(pageAs(TestUser.INSTRUCTOR)).open();

        assertThat(myCourses.row(assigned.id())).isVisible();
        assertThat(myCourses.title(assigned.id())).hasText(assigned.title());
        assertThat(myCourses.status(assigned.id())).hasText("PUBLISHED");
        assertThat(myCourses.row(notAssigned.id())).hasCount(0);
    }

    @Test
    @DisplayName("Toewijzing weghalen (instructorIds vervangt de hele set): de cursus verdwijnt uit My courses")
    void removedAssignmentDisappears() {
        TestCourse course = aCourse().titled("Unassigned later").create();
        MyCoursesPage myCourses = new MyCoursesPage(pageAs(TestUser.INSTRUCTOR)).open();
        assertThat(myCourses.row(course.id())).isVisible();

        Map<String, Object> update = new HashMap<>();
        update.put("title", course.title());
        update.put("description", course.description());
        update.put("capacityPolicy", course.capacityPolicy());
        update.put("maxSubscribers", course.maxSubscribers());
        update.put("locationId", CourseBuilder.sharedLocationId());
        var current = api.getCourse(course.id());
        update.put("startDateTime", current.path("startDateTime").asText());
        update.put("endDateTime", current.path("endDateTime").asText());
        update.put("instructorIds", List.of());
        api.updateCourse(course.id(), update);

        myCourses.open();
        assertThat(myCourses.row(course.id())).hasCount(0);
    }

    @Test
    @DisplayName("Een DRAFT-cursus waaraan de instructor toegewezen is, staat er ook in (met status DRAFT)")
    void assignedDraftCourseIsListed() {
        TestCourse draft = aCourse().titled("Assigned draft").draft().create();

        MyCoursesPage myCourses = new MyCoursesPage(pageAs(TestUser.INSTRUCTOR)).open();

        assertThat(myCourses.status(draft.id())).hasText("DRAFT");
    }

    @Test
    @DisplayName("Vanuit My courses opent de link 'Subscribers' de inschrijvingen van die cursus")
    void linksOpenCoursePages() {
        TestCourse course = aCourse().titled("Links").create();

        MyCoursesPage myCourses = new MyCoursesPage(pageAs(TestUser.INSTRUCTOR)).open();
        myCourses.openSubscribers(course.id());

        assertThat(myCourses.page()).hasURL(Pattern.compile("/courses/" + course.id() + "/subscribers$"));
    }
}
