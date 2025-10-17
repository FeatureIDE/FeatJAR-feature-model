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

import static de.featjar.formula.structure.Expressions.constant;
import static de.featjar.formula.structure.Expressions.integerAdd;
import static de.featjar.formula.structure.Expressions.variable;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.featjar.base.computation.Computations;
import de.featjar.base.computation.IComputation;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.analysis.visitor.AtomsCountTreeVisitor;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Equals;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.True;
import de.featjar.formula.structure.term.ITerm;
import de.featjar.formula.structure.term.value.Constant;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class ConstraintPropertiesTest {

    @Test
    public void atomsTest() {
        FeatureModel featureModel = createFeatureModel();
        IComputation<Integer> compuational = Computations.of(featureModel).map(ComputeAtomsCount::new);

        Trees.traverse(
                featureModel.getConstraints().iterator().next().getFormula(),
                new AtomsCountTreeVisitor(false, false, false));
        assertEquals(23, compuational.compute());
        assertEquals(
                3,
                compuational
                        .set(ComputeAtomsCount.COUNTVARIABLES, Boolean.FALSE)
                        .set(ComputeAtomsCount.COUNTBOOLEAN, Boolean.FALSE)
                        .compute());
        assertEquals(
                18,
                compuational
                        .set(ComputeAtomsCount.COUNTCONSTANTS, Boolean.FALSE)
                        .set(ComputeAtomsCount.COUNTVARIABLES, Boolean.TRUE)
                        .set(ComputeAtomsCount.COUNTBOOLEAN, Boolean.FALSE)
                        .compute());
        assertEquals(
                0,
                compuational
                        .set(ComputeAtomsCount.COUNTCONSTANTS, Boolean.FALSE)
                        .set(ComputeAtomsCount.COUNTVARIABLES, Boolean.FALSE)
                        .set(ComputeAtomsCount.COUNTBOOLEAN, Boolean.FALSE)
                        .compute());
    }

    @Test
    public void featureDensityTest() {
        FeatureModel featureModel = createFeatureModel();
        float computational =
                Computations.of(featureModel).map(ComputeFeatureDensity::new).compute();
        assertEquals((float) 5 / (float) 7, computational);
    }
    // operator((and,4), (or, 3), (not, 2), (implies, 3))
    @Test
    public void operatorDensityTest() {
        FeatureModel featureModel = createFeatureModel();
        HashMap<String, Integer> computational = Computations.of(featureModel)
                .map(ComputeOperatorDistribution::new)
                .compute();
        assertEquals(4, computational.get("And"));
        assertEquals(3, computational.get("Or"));
        assertEquals(2, computational.get("Not"));
        assertEquals(3, computational.get("Implies"));
    }

    @Test
    public void AverageConstraint() {
        FeatureModel featureModel = createFeatureModel();
        float computational =
                Computations.of(featureModel).map(ComputeAverageConstraint::new).compute();
        assertEquals((float) 23 / (float) 3, computational);
    }

    public FeatureModel createFeatureModel() {
        FeatureModel featureModel = new FeatureModel();

        // add Features (7)
        featureModel.addFeature("a");
        featureModel.addFeature("b");
        featureModel.addFeature("c");
        featureModel.addFeature("i");
        featureModel.addFeature("k");
        featureModel.addFeature("o");
        featureModel.addFeature("x");

        // define Features as literals
        Literal literalA = new Literal("a");
        Literal literalB = new Literal("b");
        Literal literalC = new Literal("c");
        Literal literalI = new Literal("i");
        Literal literalK = new Literal("k");
        Literal literalO = new Literal("o");
        Literal literalX = new Literal("x");

        // set some variables or literals
        literalO.setPositive(true);
        literalB.setPositive(false);

        // define terms
        ITerm termAdd = integerAdd(constant(42L), variable("a", Long.class));
        ITerm termAddLiteral = integerAdd(constant(42L), new Constant(2L));
        // ITerm termAddLiteral1 = integerAdd(constant(42L), new Constant(literalA, literalA.getClass()));

        // define formulas
        IFormula formula1 = new And(
                literalA,
                new Or(literalA, literalB, literalI),
                new Not(literalB),
                new Implies(literalK, literalO),
                new And(literalB),
                True.INSTANCE);

        IFormula formula2 = new Or(new Implies(formula1, literalO));
        IFormula formula3 = new Equals(termAdd, termAddLiteral);

        featureModel.addConstraint(formula1);
        featureModel.addConstraint(formula2);
        featureModel.addConstraint(formula3);
        return featureModel;
    }
}
