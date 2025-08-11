package com.mandrel.file_storage_service.architecture;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    public static void loadClasses() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.mandrel");
    }

    @Test
    public void servicesaShouldNotDependOnControllers() {
        noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat().resideInAPackage("..controller..")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    public void repositoriesShouldNotDependOnControllers() {
        noClasses()
                .that().resideInAPackage("..repository..")
                .should().dependOnClassesThat().resideInAPackage("..controller..")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    public void controllersShouldNotAccessRepositoriesDirectly() {
        noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    public void servicesShouldDependOnRepositories() {
        classes()
                .that().resideInAPackage("..service..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("..service..", "..repository..", "..model..", "java..", "jakarta..", "org.springframework..")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

    @Test
    public void enforceControllerNamingConventions() {
        classes()
                .that().resideInAPackage("..controller..")
                .should().haveSimpleNameEndingWith("Controller")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

        @Test
    public void enforceServiceNamingConventions() {
        classes()
                .that().resideInAPackage("..service..")
                .should().haveSimpleNameEndingWith("Service")
                .allowEmptyShould(true)
                .check(importedClasses);
    }

        @Test
    public void enforceRepositoryNamingConventions() {
        classes()
                .that().resideInAPackage("..repository..")
                .should().haveSimpleNameEndingWith("Repository")
                .allowEmptyShould(true)
                .check(importedClasses);
    }
}
