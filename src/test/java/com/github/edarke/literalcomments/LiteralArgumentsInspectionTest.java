package com.github.edarke.literalcomments;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import junit.framework.Assert;

import java.util.List;

public class LiteralArgumentsInspectionTest extends UsefulTestCase {


    protected CodeInsightTestFixture myFixture;
    // Specify path to your test data directory
    final String dataPath = "testData";

    @Override
    public void setUp() throws Exception {

        final IdeaTestFixtureFactory fixtureFactory = IdeaTestFixtureFactory.getFixtureFactory();
        final TestFixtureBuilder<IdeaProjectTestFixture> testFixtureBuilder =
                fixtureFactory.createFixtureBuilder(getName());
        myFixture = JavaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(testFixtureBuilder.getFixture());
        myFixture.setTestDataPath(dataPath);
        final JavaModuleFixtureBuilder builder = testFixtureBuilder.addModule(JavaModuleFixtureBuilder.class);

        builder.addContentRoot(myFixture.getTempDirPath()).addSourceRoot("");
        builder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
        myFixture.enableInspections(LiteralArgumentsInspection.class);
        myFixture.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
        myFixture.tearDown();
        myFixture = null;
    }

    protected void doTest(String testName, String hint) {
        myFixture.configureByFile(testName + ".java");
        List<HighlightInfo> highlightInfos = myFixture.doHighlighting();
        Assert.assertTrue(!highlightInfos.isEmpty());

        final IntentionAction action = myFixture.findSingleIntention(hint);

        Assert.assertNotNull(action);
        myFixture.launchAction(action);
        myFixture.checkResultByFile(testName + ".after.java");
    }

    // Test the "==" case
    public void test() {
        doTest("before", "Use equals()");
    }

    // Test the "!=" case
    public void test1() {
        doTest("before1", "Use equals()");
    }

}