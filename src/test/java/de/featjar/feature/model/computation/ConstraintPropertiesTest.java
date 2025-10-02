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
import org.junit.jupiter.api.Test;

public class ConstraintPropertiesTest {
    // FeatJAR.();
    // FeatureJAR.log();

    @Test
    public void AtomsTest() {
        FeatureModel featureModel = new FeatureModel();
        Literal literal1 = new Literal("o");
        literal1.setPositive(true);
        IFormula tree1 = new And(
                new Literal("a"), new Literal("b"), new Literal("x"), new Or(literal1, new Literal(false, "i")));
        IFormula tree2 = new And(new Literal("a"), new Or(literal1, new Literal(false, "i")));
        featureModel.addConstraint(tree1);
        featureModel.addConstraint(tree2);
        int compuational = Computations.of(featureModel)
                .map(ComputeConstraintProperties::new)
                .set(ComputeConstraintProperties.COUNTVARIABLES, Boolean.FALSE)
                .compute();
        tree1.print();
        assertEquals(0, compuational);
    }
}
