/*
 * Copyright (C) 2022 Elias Kuiter
 *
 * This file is part of feature-model.
 *
 * feature-model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatJAR/model> for further information.
 */
package de.featjar.model;

import static org.junit.jupiter.api.Assertions.*;

import de.featjar.model.util.Attributable;
import de.featjar.model.util.Attribute;
import de.featjar.model.util.Identifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Attribute}, {@link Attributes}, and {@link Attributable}.
 *
 * @author Elias Kuiter
 */
public class AttributeTest {
    FeatureModel featureModel;
    Attribute<String> attribute = new Attribute<>("any", "test", String.class);

    @BeforeEach
    public void createFeatureModel() {
        featureModel = new FeatureModel(Identifier.newCounter());
    }

    @Test
    public void attribute() {
        assertEquals("any", attribute.getNamespace());
        assertEquals("test", attribute.getName());
        assertEquals(String.class, attribute.getType());
    }

    @Test
    public void attributableGetSet() {
        Map<Attribute<?>, Object> attributeToValueMap = new HashMap<>();
        Attribute.WithDefaultValue<String> attributeWithDefaultValue =
                new Attribute.WithDefaultValue<>(Attribute.DEFAULT_NAMESPACE, "test", String.class, "default");
        Assertions.assertEquals(Optional.empty(), featureModel.getAttributeValue(attribute));
        Assertions.assertEquals("default", featureModel.getAttributeValue(attributeWithDefaultValue));
        assertEquals(attributeToValueMap, featureModel.getAttributeToValueMap());
        featureModel.mutate().setAttributeValue(attribute, "value");
        attributeToValueMap.put(attribute, "value");
        Assertions.assertEquals(Optional.of("value"), featureModel.getAttributeValue(attribute));
        assertEquals(Optional.of("value"), attribute.apply(attributeToValueMap));
        assertEquals(attributeToValueMap, featureModel.getAttributeToValueMap());
        featureModel.mutate().removeAttributeValue(attribute);
        attributeToValueMap.clear();
        Assertions.assertEquals(Optional.empty(), featureModel.getAttributeValue(attribute));
        assertEquals(attributeToValueMap, featureModel.getAttributeToValueMap());
    }

    @Test
    public void attributableToggle() {
        Attribute.WithDefaultValue<Boolean> booleanAttribute =
                new Attribute.WithDefaultValue<>(Attribute.DEFAULT_NAMESPACE, "test", Boolean.class, false);
        Assertions.assertEquals(false, featureModel.getAttributeValue(booleanAttribute));
        featureModel.mutate().toggleAttributeValue(booleanAttribute);
        Assertions.assertEquals(true, featureModel.getAttributeValue(booleanAttribute));
    }

    @Test
    public void attributesName() {
        Assertions.assertEquals(featureModel.getName(), featureModel.getAttributeValue(Attributes.NAME));
        Assertions.assertEquals("@" + featureModel.getIdentifier(), featureModel.getName());
        Assertions.assertEquals(Optional.empty(), featureModel.getDescription());
    }

    @Test
    public void attributesDescription() {
        featureModel.mutate().setDescription("desc");
        Assertions.assertEquals(Optional.of("desc"), featureModel.getDescription());
        featureModel.mutate().setDescription(null);
        Assertions.assertEquals(Optional.empty(), featureModel.getDescription());
    }

    @Test
    public void attributesHidden() {
        Assertions.assertFalse(featureModel.getRootFeature().isHidden());
        featureModel.getRootFeature().mutate().setHidden(true);
        Assertions.assertTrue(featureModel.getRootFeature().isHidden());
        Assertions.assertFalse(featureModel.getRootFeature().mutate().toggleHidden());
    }
}
