package be.pxl.qacademy.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Wie de test schreef (traceerbaarheid per teamlid, eis van de opdracht). Staat op de klasse
 * of op een methode; een methode-annotatie gaat voor. Samen met de git-historiek
 * ({@code git log --follow <bestand>}) toont dit per test de auteur.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Author {
    String value();
}
