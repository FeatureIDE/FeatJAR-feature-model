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
package de.featjar.feature.model.analysis;

import static de.featjar.formula.structure.Expressions.constant;
import static de.featjar.formula.structure.Expressions.integerAdd;
import static de.featjar.formula.structure.Expressions.variable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.tree.Trees;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.analysis.visitor.AtomsCountTreeVisitor;
import de.featjar.feature.model.analysis.visitor.FeatureDensityTreeVisitor;
import de.featjar.feature.model.analysis.visitor.OperatorDistributionTreeVisitor;
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
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ConstraintPropertiesVisitorTest {

    @Test
    public void atomsVisitorTest() {
        FeatureModel featureModel = createFeatureModel();
        Iterator<IConstraint> constraintsIterator =
                featureModel.getConstraints().iterator();
        IFormula formula1 = constraintsIterator.next().getFormula();
        IFormula formula2 = constraintsIterator.next().getFormula();
        IFormula formula3 = constraintsIterator.next().getFormula();
        int formula1BooleansCount =
                Trees.traverse(formula1, new AtomsCountTreeVisitor(false, false, true)).orElseThrow();
        int formula2ConstantsCount =
                Trees.traverse(formula3, new AtomsCountTreeVisitor(false, true, false)).orElseThrow();
        int formula3VariablesCount =
                Trees.traverse(formula3, new AtomsCountTreeVisitor(true, false, false)).orElseThrow();
        assertEquals(1, formula1BooleansCount);
        assertEquals(3, formula2ConstantsCount);
        assertEquals(1, formula3VariablesCount);
        assertEquals(
                0, Trees.traverse(new And(), new AtomsCountTreeVisitor(true, true, true)).orElseThrow());
    }

    @Test
    public void featureDensityVisitorTest() {
        FeatureModel featureModel = createFeatureModel();
        Iterator<IConstraint> constraintsIterator =
                featureModel.getConstraints().iterator();
        Set<String> formula1Features = Trees.traverse(constraintsIterator.next().getFormula(), new FeatureDensityTreeVisitor())
                .orElseThrow();
        Set<String> formula2Features = Trees.traverse(constraintsIterator.next().getFormula(), new FeatureDensityTreeVisitor())
                .orElseThrow();
        Set<String> formula3Features = Trees.traverse(constraintsIterator.next().getFormula(), new FeatureDensityTreeVisitor())
                .orElseThrow();

        assertEquals(5, formula1Features.size());
        assertTrue(formula1Features.contains("a"));
        assertTrue(formula1Features.contains("b"));
        assertTrue(formula1Features.contains("i"));
        assertTrue(formula1Features.contains("k"));
        assertTrue(formula1Features.contains("o"));
        assertFalse(formula1Features.contains("x"));
        assertFalse(formula1Features.contains("c"));
        assertEquals(5, formula2Features.size());
        assertTrue(formula2Features.contains("a"));
        assertTrue(formula2Features.contains("b"));
        assertTrue(formula2Features.contains("i"));
        assertTrue(formula2Features.contains("k"));
        assertTrue(formula2Features.contains("o"));
        assertFalse(formula2Features.contains("x"));
        assertFalse(formula2Features.contains("c"));
        assertEquals(1, formula3Features.size());
        assertTrue(formula3Features.contains("a"));
        assertFalse(formula3Features.contains("b"));
        assertEquals(
                0, Trees.traverse(new And(), new FeatureDensityTreeVisitor()).orElseThrow().size());
    }

    @Test
    public void operatorDensityVisitorTest() {
        FeatureModel featureModel = createFeatureModel();
        Iterator<IConstraint> constraintsIterator =
                featureModel.getConstraints().iterator();
        HashMap<String, Integer> formula1Count = Trees.traverse(
                        constraintsIterator.next().getFormula(), new OperatorDistributionTreeVisitor())
                .orElseThrow();
        HashMap<String, Integer> formula2Count = Trees.traverse(
                        constraintsIterator.next().getFormula(), new OperatorDistributionTreeVisitor())
                .orElseThrow();
        HashMap<String, Integer> formula3Count = Trees.traverse(
                        constraintsIterator.next().getFormula(), new OperatorDistributionTreeVisitor())
                .orElseThrow();

        assertEquals(2, formula1Count.get("And"));
        assertEquals(1, formula1Count.get("Or"));
        assertEquals(1, formula1Count.get("Not"));
        assertEquals(1, formula1Count.get("Implies"));
        assertEquals(4, formula1Count.size());

        assertEquals(2, formula2Count.get("And"));
        assertEquals(2, formula2Count.get("Or"));
        assertEquals(1, formula2Count.get("Not"));
        assertEquals(2, formula2Count.get("Implies"));
        assertEquals(4, formula2Count.size());

        assertEquals(0, formula3Count.size());
        assertEquals(
                0,
                Trees.traverse(null, new OperatorDistributionTreeVisitor()).orElseThrow().size());
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

        // operator((and,4), (or, 3), (not, 2), (implies, 3))
        // define formulas
        // 8 literal, 5 operator((and,2), (or, 1), (not, 1), (implies, 1)), Features(a,b,i,k,o)
        IFormula formula1 = new And(
                literalA,
                new Or(literalA, literalB, literalI),
                new Not(literalB),
                new Implies(literalK, literalO),
                new And(literalB),
                True.INSTANCE);

        IFormula formula2 = new Or(new Implies(formula1, literalO));
        IFormula formula3 = new Equals(termAdd, termAddLiteral);

        // add full formulas as constraints
        featureModel.addConstraint(formula1);
        featureModel.addConstraint(formula2);
        featureModel.addConstraint(formula3);
        return featureModel;
    }
}
