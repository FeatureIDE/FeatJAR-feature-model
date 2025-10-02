/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model.
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
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model> for further information.
 */
package de.featjar.feature.model.computation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.featjar.base.computation.*;
import de.featjar.feature.model.FeatureModel;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.*;
import de.featjar.formula.structure.predicate.Literal;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class ConstraintPropertiesTest {
    // FeatJAR.();
    // FeatureJAR.log();

    @Test
    public void atomsTest() {
        FeatureModel featureModel = createFeatureModel();
        int compuational = Computations.of(featureModel)
                .map(ComputeConstraintProperties::new)
                .set(ComputeConstraintProperties.COUNTVARIABLES, Boolean.FALSE)
                .compute();
        assertEquals(0, compuational);
    }

    @Test
    public void featureDensityTest() {
        FeatureModel featureModel = createFeatureModel();
        float computational =
                Computations.of(featureModel).map(ComputeFeatureDensity::new).compute();
        assertEquals((float) 5 / (float) 6, computational);
    }

    @Test
    public void operatorDensityTest() {
        FeatureModel featureModel = createFeatureModel();
        HashMap<String, Integer> computational = Computations.of(featureModel)
                .map(ComputeOperatorDistribution::new)
                .compute();
        System.out.println(computational);
        assertEquals(3, computational.get("And"));
        assertEquals(2, computational.get("Or"));
    }

    @Test
    public void AverageConstraint() {
        FeatureModel featureModel = createFeatureModel();
        float computational =
                Computations.of(featureModel).map(ComputeAverageConstraint::new).compute();
        System.out.println(computational);
        assertEquals((float) 8 / (float) 2, computational);
    }

    public FeatureModel createFeatureModel() {
        FeatureModel featureModel = new FeatureModel();
        featureModel.addFeature("o");
        featureModel.addFeature("b");
        featureModel.addFeature("x");
        featureModel.addFeature("a");
        featureModel.addFeature("i");
        featureModel.addFeature("k");
        Literal literal1 = new Literal("o");
        literal1.setPositive(true);
        IFormula tree1 = new And(
                new And(),
                new Literal("a"),
                new Literal("b"),
                new Literal("x"),
                new Or(literal1, new Literal(false, "i")));
        IFormula tree2 = new And(new Literal("a"), new Or(literal1, new Literal(false, "i")));
        featureModel.addConstraint(tree1);
        featureModel.addConstraint(tree2);
        return featureModel;
    }
}
