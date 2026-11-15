package be.pxl.qacademy.factories;

import java.math.BigDecimal;
import java.util.List;

/** Een cursus die een test zelf aanmaakte (via {@link CourseBuilder}). */
public record TestCourse(
        String id,
        String title,
        String slug,
        String description,
        BigDecimal price,
        String capacityPolicy,
        Integer maxSubscribers,
        String locationName,
        List<String> moduleTitles,
        List<String> moduleDescriptions,
        String status) {

    public boolean isFree() {
        return price == null || price.signum() == 0;
    }
}
