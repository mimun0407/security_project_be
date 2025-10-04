package com.example.demo.common;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("com.example.demo.common.AutoModel")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AutoModelProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(AutoModel.class)) {
            if (element.getKind() != ElementKind.CLASS) continue;

            String className = element.getSimpleName().toString();

            generateClass("com.example.demo.model", className + "WId", element, true);
            generateClass("com.example.demo.model", className + "NId", element, false);
        }
        return true;
    }

    private void generateClass(String pkg, String name, Element source, boolean includeId) {
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(pkg).append(";\n\n");
        builder.append("public class ").append(name).append(" {\n");

        for (Element field : source.getEnclosedElements()) {
            if (field.getKind() == ElementKind.FIELD) {
                String fieldName = field.getSimpleName().toString();
                if (!includeId && fieldName.equalsIgnoreCase("id")) continue;
                builder.append("public")
                        .append(field.asType().toString())
                        .append(" ")
                        .append(fieldName)
                        .append(";\n");
            }
        }

        builder.append("}\n");

        try {
            JavaFileObject file = processingEnv.getFiler()
                    .createSourceFile(pkg + "." + name);
            try (Writer writer = file.openWriter()) {
                writer.write(builder.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
