/**
 * Copyright © 2010 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.jsonschema2pojo.rules;

import static org.easymock.EasyMock.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.StringWriter;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Test;

import com.googlecode.jsonschema2pojo.SchemaMapper;
import com.googlecode.jsonschema2pojo.exception.GenerationException;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JPackage;

public class ObjectRuleTest {

    private static final String TARGET_PACKAGE_NAME = ArrayRuleTest.class.getPackage().getName() + ".test";

    private static final String EXPECTED_RESULT =
            "@javax.annotation.Generated(\"com.googlecode.jsonschema2pojo\")\n" +
                    "public class FooBar\n" +
                    "    implements java.io.Serializable\n{\n\n\n" +
                    "    @java.lang.Override\n" +
                    "    public java.lang.String toString() {\n" +
                    "        return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this);\n" +
                    "    }\n\n" +
                    "    @java.lang.Override\n" +
                    "    public int hashCode() {\n" +
                    "        return org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode(this);\n" +
                    "    }\n\n" +
                    "    @java.lang.Override\n" +
                    "    public boolean equals(java.lang.Object other) {\n" +
                    "        return org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals(this, other);\n" +
                    "    }\n\n" +
                    "}\n";

    private SchemaMapper mockSchemaMapper = createMock(SchemaMapper.class);
    private ObjectRule rule = new ObjectRule(mockSchemaMapper);

    @Test
    public void applyGeneratesBean() {

        JPackage jpackage = new JCodeModel()._package(TARGET_PACKAGE_NAME);

        ObjectNode objectNode = new ObjectMapper().createObjectNode();

        AdditionalPropertiesRule mockAdditionalPropertiesRule = createMock(AdditionalPropertiesRule.class);
        expect(mockSchemaMapper.getAdditionalPropertiesRule()).andReturn(mockAdditionalPropertiesRule);
        replay(mockSchemaMapper);

        JDefinedClass result = rule.apply("fooBar", objectNode, jpackage);

        StringWriter output = new StringWriter();
        result.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_RESULT));
    }

    @Test
    public void applyGeneratesWithAdditionalNodes() {

        JPackage jpackage = new JCodeModel()._package(TARGET_PACKAGE_NAME);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode descriptionNode = objectMapper.createObjectNode();
        ObjectNode optionalNode = objectMapper.createObjectNode();
        ObjectNode propertiesNode = objectMapper.createObjectNode();
        ObjectNode additionalPropertiesNode = objectMapper.createObjectNode();

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("description", descriptionNode);
        objectNode.put("optional", optionalNode);
        objectNode.put("properties", propertiesNode);
        objectNode.put("additionalProperties", additionalPropertiesNode);

        DescriptionRule mockDescriptionRule = createMock(DescriptionRule.class);
        OptionalRule mockOptionalRule = createMock(OptionalRule.class);
        PropertiesRule mockPropertiesRule = createMock(PropertiesRule.class);
        AdditionalPropertiesRule mockAdditionalPropertiesRule = createMock(AdditionalPropertiesRule.class);

        expect(mockDescriptionRule.apply(eq("fooBar"), eq(descriptionNode), isA(JDefinedClass.class))).andReturn(null);
        expect(mockOptionalRule.apply(eq("fooBar"), eq(optionalNode), isA(JDefinedClass.class))).andReturn(null);
        expect(mockPropertiesRule.apply(eq("fooBar"), eq(propertiesNode), isA(JDefinedClass.class))).andReturn(null);
        expect(mockAdditionalPropertiesRule.apply(eq("fooBar"), eq(additionalPropertiesNode), isA(JDefinedClass.class))).andReturn(null);

        expect(mockSchemaMapper.getDescriptionRule()).andReturn(mockDescriptionRule);
        expect(mockSchemaMapper.getOptionalRule()).andReturn(mockOptionalRule);
        expect(mockSchemaMapper.getPropertiesRule()).andReturn(mockPropertiesRule);
        expect(mockSchemaMapper.getAdditionalPropertiesRule()).andReturn(mockAdditionalPropertiesRule);
        
        replay(mockSchemaMapper, mockDescriptionRule, mockOptionalRule, mockPropertiesRule, mockAdditionalPropertiesRule);

        JDefinedClass result = rule.apply("fooBar", objectNode, jpackage);

        StringWriter output = new StringWriter();
        result.declare(new JFormatter(output));

        assertThat(output.toString(), equalTo(EXPECTED_RESULT));

        verify(mockDescriptionRule, mockOptionalRule, mockPropertiesRule, mockAdditionalPropertiesRule);

    }

    @Test(expected = GenerationException.class)
    public void applyFailsWhereClassAlreadyExists() throws JClassAlreadyExistsException {

        JPackage jpackage = new JCodeModel()._package(TARGET_PACKAGE_NAME);

        jpackage._class("ExistingClass");

        rule.apply("existingClass", new ObjectMapper().createObjectNode(), jpackage);

    }

}
