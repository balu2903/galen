/*******************************************************************************
* Copyright 2015 Ivan Shubin http://mindengine.net
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
package net.mindengine.galen.tests.speclang2.pagespec;

import net.mindengine.galen.browser.Browser;
import net.mindengine.galen.browser.SeleniumBrowser;
import net.mindengine.galen.components.mocks.driver.MockedDriver;
import net.mindengine.galen.speclang2.reader.pagespec.PageSpecReaderV2;
import net.mindengine.galen.specs.page.CorrectionsRect;
import net.mindengine.galen.specs.page.Locator;
import net.mindengine.galen.specs.page.ObjectSpecs;
import net.mindengine.galen.specs.page.PageSection;
import net.mindengine.galen.specs.reader.page.PageSpec;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PageSpecReaderV2Test {

    private static final Browser NO_BROWSER = null;

    @Test
    public void shouldRead_objectDefinitions() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/object-definitions.gspec");


        assertThat(pageSpec.getObjects(), is((Map<String, Locator>)new HashMap<String, Locator>(){{
            put("header", new Locator("css", "#header"));
            put("header-icon", new Locator("css", "#header img"));
            put("button", new Locator("xpath", "//div[@id='button']"));
            put("cancel-link", new Locator("id", "cancel"));
            put("caption", new Locator("css", "#wrapper")
                    .withCorrections(new CorrectionsRect(
                            new CorrectionsRect.Correction(0, CorrectionsRect.Type.PLUS),
                            new CorrectionsRect.Correction(100, CorrectionsRect.Type.PLUS),
                            new CorrectionsRect.Correction(5, CorrectionsRect.Type.MINUS),
                            new CorrectionsRect.Correction(7, CorrectionsRect.Type.PLUS)
                    )));
        }}));
    }


    @Test
    public void shouldRead_objectDefinitions_withMultiObjects() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/object-definitions-multi-objects.gspec",
                new SeleniumBrowser(new MockedDriver("/speclang2/mocks/menu-items.json")));

        assertThat(pageSpec.getObjects(), is((Map<String, Locator>)new HashMap<String, Locator>(){{
            put("menu-item-1", new Locator("css", "#menu li", 1));
            put("menu-item-2", new Locator("css", "#menu li", 2));
            put("menu-item-3", new Locator("css", "#menu li", 3));
            put("menu-item-4", new Locator("css", "#menu li", 4));
        }}));
    }

    @Test
    public void shouldRead_objectDefinitions_withMultiLevelObjects() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/object-definitions-multi-level-objects.gspec",
                new SeleniumBrowser(new MockedDriver("/speclang2/mocks/multi-level-objects.json")));

        assertThat(pageSpec.getObjects(), is((Map<String, Locator>)new HashMap<String, Locator>(){{
            put("header", new Locator("css", "#header"));
            put("header.icon", new Locator("css", "img")
                    .withParent(new Locator("css", "#header")));

            put("box-1", new Locator("css", ".box", 1));
            put("box-1.caption", new Locator("css", ".caption")
                    .withParent(new Locator("css", ".box", 1)));

            put("box-2", new Locator("css", ".box", 2));
            put("box-2.caption", new Locator("css", ".caption")
                    .withParent(new Locator("css", ".box", 2)));

            put("box-3", new Locator("css", ".box", 3));
            put("box-3.caption", new Locator("css", ".caption")
                    .withParent(new Locator("css", ".box", 3)));
        }}));
    }


    @Test
    public void shouldRead_sectionsWithObjectSpecs() throws  IOException {
        PageSpec pageSpec = readPageSpec("speclang2/sections-with-object-specs.gspec");

        assertThat(pageSpec.getSections().size(), is(2));

        PageSection section1 = pageSpec.getSections().get(0);
        assertThat(section1.getObjects().size(), is(1));
        assertThat(section1.getObjects().get(0).getObjectName(), is("header"));
        assertThat(section1.getObjects().get(0).getSpecs().size(), is(1));
        assertThat(section1.getObjects().get(0).getSpecs().get(0).getOriginalText(), is("height 100px"));

        assertThat(section1.getSections().size(), is(1));
        PageSection subSection = section1.getSections().get(0);
        assertThat(subSection.getObjects().size(), is(2));
        assertThat(subSection.getObjects().get(0).getObjectName(), is("login-link"));
        assertThat(subSection.getObjects().get(0).getSpecs().size(), is(1));
        assertThat(subSection.getObjects().get(0).getSpecs().get(0).getOriginalText(), is("height 30px"));
        assertThat(subSection.getObjects().get(1).getObjectName(), is("register-link"));
        assertThat(subSection.getObjects().get(1).getSpecs().size(), is(1));
        assertThat(subSection.getObjects().get(1).getSpecs().get(0).getOriginalText(), is("right-of login-link 10 to 30px"));

        PageSection section2 = pageSpec.getSections().get(1);
        assertThat(section2.getName(), is("Main section"));
        assertThat(section2.getObjects().size(), is(1));
        assertThat(section2.getObjects().get(0).getObjectName(), is("main-section"));
        assertThat(section2.getObjects().get(0).getSpecs().size(), is(2));
        assertThat(section2.getObjects().get(0).getSpecs().get(0).getOriginalText(), is("below header 0 to 5px"));
        assertThat(section2.getObjects().get(0).getSpecs().get(1).getOriginalText(), is("inside screen 0px left right"));
    }


    /**
     * Purpose of this test is to check that "${}" expressions could be processed everywhere
     */
    @Test
    public void shouldRead_variablesDefinition_andProcessThem() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/variables-and-processing.gspec");


        assertThat(pageSpec.getSections().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getName(), is("Section for user Johny"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getObjectName(), is("welcome-message"));
        assertThat(pageSpec.getSections().get(0).getObjects().get(0).getSpecs().get(0).getOriginalText(), is("text is \"Welcome, Johny\""));

    }

    @Test
    public void shouldRead_simpleForLoop_andProcessIt() throws IOException {
        PageSpec pageSpec = readPageSpec("speclang2/for-loop.gspec");

        assertThat(pageSpec.getSections().size(), is(1));
        assertThat(pageSpec.getSections().get(0).getName(), is("Main section"));
        assertThat(pageSpec.getSections().get(0).getObjects().size(), is(13));


        List<ObjectSpecs> objects = pageSpec.getSections().get(0).getObjects();

        int objectIndex = 0;
        for (int i = 1; i <= 3; i++) {
            for (Integer j : asList(5, 7, 9)) {
                assertThat("Object #" + objectIndex + " name should be",
                        objects.get(objectIndex).getObjectName(),
                        is("box-" + i + "-" + j));

                assertThat("Object #" + objectIndex + " spec should be",
                        objects.get(objectIndex).getSpecs().get(0).getOriginalText(),
                        is("text is \"" + i + " and " + j + "\""));
                objectIndex++;
            }

            assertThat("Object #" + objectIndex + " name should be",
                    objects.get(objectIndex).getObjectName(),
                    is("label-" + i));

            assertThat("Object #" + objectIndex + " spec should be",
                    objects.get(objectIndex).getSpecs().get(0).getOriginalText(),
                    is("height 10px"));

            objectIndex++;
        }

        assertThat("Object #11 name should be",
                objects.get(objectIndex).getObjectName(),
                is("caption"));

        assertThat("Object #11 spec should be",
                objects.get(objectIndex).getSpecs().get(0).getOriginalText(),
                is("width 50px"));
    }

    private PageSpec readPageSpec(String resource) throws IOException {
        return readPageSpec(resource, NO_BROWSER);
    }

    private PageSpec readPageSpec(String resource, Browser browser) throws IOException {
        return new PageSpecReaderV2().read(resource, browser);
    }

}
