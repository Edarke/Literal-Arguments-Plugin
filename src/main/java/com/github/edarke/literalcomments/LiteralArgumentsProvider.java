package com.github.edarke.literalcomments;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

public class LiteralArgumentsProvider implements ApplicationComponent, InspectionToolProvider {

    @Override
    public void initComponent() {
        // do nothing
    }

    @Override
    public void disposeComponent() {
        // do nothing
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "InspectionRegistration";
    }


    @NotNull
    public Class[] getInspectionClasses() {
        return new Class[]{LiteralArgumentsInspection.class};
    }
}

