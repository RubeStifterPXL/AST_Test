package be.pxl.qacademy.fixtures;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Slaat traces en screenshots op zodra een test faalt. Draait na de testmethode maar vóór
 * {@code @AfterEach}, dus terwijl de browsercontexten nog open zijn.
 */
public class FailureArtifacts implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        boolean failed = context.getExecutionException().isPresent();
        context.getTestInstance()
                .filter(BaseUiTest.class::isInstance)
                .map(BaseUiTest.class::cast)
                .ifPresent(test -> test.saveArtifacts(failed));
    }
}
