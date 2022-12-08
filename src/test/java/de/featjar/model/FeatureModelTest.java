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

import de.featjar.formula.structure.atomic.literal.Literal;
import de.featjar.model.mixins.FeatureModelFeatureTreeMixin;
import de.featjar.model.util.Identifier;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FeatureModel}, its elements, and its mixins.
 *
 * @author Elias Kuiter
 */
public class FeatureModelTest {
    FeatureModel featureModel;

    @BeforeEach
    public void createFeatureModel() {
        featureModel = new FeatureModel(Identifier.newCounter());
    }

    @Test
    public void featureModel() {
        Assertions.assertEquals("1", featureModel.getIdentifier().toString());
        assertTrue(featureModel.getFeatureTree().getChildren().isEmpty());
        assertTrue(featureModel.getConstraints().isEmpty());
        assertNotNull(featureModel.getFeatureOrder());
        assertEquals(1, featureModel.getFeatureCache().size());
        assertEquals(1, featureModel.getElementCache().size());
    }

    @Test
    public void commonAttributesMixin() {
        Assertions.assertEquals("@1", featureModel.getName());
        Assertions.assertEquals(Optional.empty(), featureModel.getDescription());
        featureModel.mutate(m -> {
            m.setName("My Model");
            m.setDescription("awesome description");
        });
        Assertions.assertEquals("My Model", featureModel.getName());
        Assertions.assertEquals(Optional.of("awesome description"), featureModel.getDescription());
    }

    @Test
    public void featureModelCacheMixin() {
        featureModel.mutate().createFeatureBelow(featureModel.getRootFeature());
        assertEquals(2, featureModel.getFeatureCache().size());
        featureModel.mutate().createFeatureBelow(featureModel.getRootFeature());
        assertEquals(3, featureModel.getFeatureCache().size());
        FeatureModelFeatureTreeMixin.Mutator uncachedMutator = () -> featureModel;
        uncachedMutator.createFeatureBelow(featureModel.getRootFeature());
        assertEquals(3, featureModel.getFeatureCache().size());
        featureModel.mutateInternal(() -> uncachedMutator.createFeatureBelow(featureModel.getRootFeature()));
        assertEquals(5, featureModel.getFeatureCache().size());
    }

    @Test
    public void featureModelConstraintMixin() {
        Assertions.assertEquals(0, featureModel.getNumberOfConstraints());
        Constraint constraint1 = featureModel.mutate().createConstraint(Literal.True);
        Constraint constraint2 = featureModel.mutate().createConstraint(Literal.True);
        Constraint constraint3 = featureModel.mutate().createConstraint(Literal.False);
        Assertions.assertEquals(3, featureModel.getNumberOfConstraints());
        Assertions.assertEquals(Optional.of(constraint1), featureModel.getConstraint(constraint1.getIdentifier()));
        Assertions.assertTrue(featureModel.hasConstraint(constraint2.getIdentifier()));
        Assertions.assertEquals(Optional.of(0), featureModel.getConstraintIndex(constraint1));
        Assertions.assertEquals(Optional.of(1), featureModel.getConstraintIndex(constraint2));
        assertSame(
                constraint3,
                featureModel
                        .getConstraints()
                        .get(featureModel.getConstraintIndex(constraint3).get()));
        constraint2.mutate().remove();
        Assertions.assertEquals(Optional.of(0), featureModel.getConstraintIndex(constraint1));
        Assertions.assertEquals(Optional.empty(), featureModel.getConstraintIndex(constraint2));
        Assertions.assertEquals(Optional.of(1), featureModel.getConstraintIndex(constraint3));
        assertSame(
                constraint3,
                featureModel
                        .getConstraints()
                        .get(featureModel.getConstraintIndex(constraint3).get()));
    }

    @Test
    public void featureModelFeatureOrderMixin() {
        Feature rootFeature = featureModel.getRootFeature();
        Feature childFeature = rootFeature.mutate().createFeatureBelow();
        Assertions.assertEquals(Arrays.asList(rootFeature, childFeature), featureModel.getOrderedFeatures());
        featureModel.mutate().setFeatureOrder(FeatureOrder.ofList(Arrays.asList(childFeature, rootFeature)));
        Assertions.assertEquals(Arrays.asList(childFeature, rootFeature), featureModel.getOrderedFeatures());
        featureModel.mutate().setFeatureOrder(FeatureOrder.ofList(Collections.singletonList(childFeature)));
        Assertions.assertEquals(Arrays.asList(childFeature, rootFeature), featureModel.getOrderedFeatures());
        featureModel.mutate().setFeatureOrder(FeatureOrder.ofList(Collections.singletonList(rootFeature)));
        Assertions.assertEquals(Arrays.asList(rootFeature, childFeature), featureModel.getOrderedFeatures());
        Comparator<Feature> comparator =
                Comparator.comparingLong(f -> ((Identifier.Counter) f.getIdentifier()).getCounter());
        featureModel.mutate().setFeatureOrder(FeatureOrder.ofComparator(comparator));
        Assertions.assertEquals(Arrays.asList(rootFeature, childFeature), featureModel.getOrderedFeatures());
        featureModel.mutate().setFeatureOrder(FeatureOrder.ofComparator(comparator.reversed()));
        Assertions.assertEquals(Arrays.asList(childFeature, rootFeature), featureModel.getOrderedFeatures());
        Feature anotherChildFeature = rootFeature.mutate().createFeatureBelow();
        Assertions.assertEquals(
                Arrays.asList(anotherChildFeature, childFeature, rootFeature), featureModel.getOrderedFeatures());
        featureModel.mutate().removeFeature(childFeature);
        featureModel.mutate().removeFeature(anotherChildFeature);
        Assertions.assertEquals(List.of(rootFeature), featureModel.getOrderedFeatures());
    }

    @Test
    public void featureModelFeatureTreeMixin() {
        Feature rootFeature = featureModel.getRootFeature();
        Assertions.assertEquals(1, featureModel.getNumberOfFeatures());
        final Feature childFeature = rootFeature.mutate().createFeatureBelow();
        assertSame(childFeature, childFeature.getFeatureTree().getFeature());
        assertSame(rootFeature, childFeature.getFeatureTree().getParent().get().getFeature());
        assertSame(childFeature.getFeatureTree().getParent().get(), rootFeature.getFeatureTree());
        assertSame(featureModel.getFeature(childFeature.getIdentifier()).get(), childFeature);
        Assertions.assertEquals(2, featureModel.getNumberOfFeatures());
        Assertions.assertEquals(Optional.of(childFeature), featureModel.getFeature(childFeature.getIdentifier()));
        Assertions.assertTrue(featureModel.hasFeature(childFeature.getIdentifier()));
        Assertions.assertTrue(featureModel.getFeaturesByName("root").isEmpty());
        rootFeature.mutate().setName("root");
        Assertions.assertEquals(Set.of(rootFeature), featureModel.getFeaturesByName("root"));
        assertEquals(
                List.of(childFeature.getFeatureTree()),
                rootFeature.getFeatureTree().getChildren());
        assertEquals(
                Optional.of(rootFeature.getFeatureTree()),
                childFeature.getFeatureTree().getParent());
        assertThrows(IllegalArgumentException.class, () -> rootFeature.mutate().remove());
        assertDoesNotThrow(() -> childFeature.mutate().remove());
        assertEquals(List.of(), rootFeature.getFeatureTree().getChildren());
        assertEquals(Optional.empty(), childFeature.getFeatureTree().getParent());
    }
}
