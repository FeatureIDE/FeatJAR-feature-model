/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.feature.model.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.IComputation;
import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.IO;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.analysis.ComputeDistributionFeatureSelections;
import de.featjar.feature.model.analysis.ComputeFeatureCounter;
import de.featjar.feature.model.analysis.ComputeNumberConfigurations;
import de.featjar.feature.model.analysis.ComputeNumberVariables;
import de.featjar.feature.model.io.FeatureModelFormats;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import org.junit.jupiter.api.Test;

public class SamplePropertiesTest {

    public BooleanAssignmentList createAssignmentList() {
        LinkedList<String> variableNames = new LinkedList<String>();
        variableNames.add("A");
        variableNames.add("B");
        variableNames.add("C");
        variableNames.add("D");
        variableNames.add("E");
        variableNames.add("F");
        variableNames.add("G");
        VariableMap variableMap = new VariableMap(variableNames);

        BooleanAssignmentList booleanAssignmentList = new BooleanAssignmentList(
                variableMap,
                new BooleanAssignment(1, -2, -5, -6),
                new BooleanAssignment(-1, -3, -6),
                new BooleanAssignment(1, 2, 4, 5),
                new BooleanAssignment(5, 6),
                new BooleanAssignment());
        return booleanAssignmentList;
    }
    
    public BooleanAssignmentList createAssignmentListUniformity() {
        LinkedList<String> variableNames = new LinkedList<String>();
        variableNames.add("Root");
        variableNames.add("A");
        variableNames.add("B");
        VariableMap variableMap = new VariableMap(variableNames);

        BooleanAssignmentList booleanAssignmentList = new BooleanAssignmentList(
                variableMap,
                new BooleanAssignment(2),
                new BooleanAssignment(3),
                new BooleanAssignment(-2),
                new BooleanAssignment(-3));
        return booleanAssignmentList;
    }

    @Test
    public void computeDistributionFeaturesSelectionsTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        IComputation<HashMap<String, Integer>> computational =
                Computations.of(booleanAssignmentList).map(ComputeDistributionFeatureSelections::new);
        HashMap<String, Integer> selectionDistribution = computational.compute();
        assertEquals(7, selectionDistribution.get("selected"));
        assertEquals(6, selectionDistribution.get("deselected"));
        assertEquals(22, selectionDistribution.get("undefined"));
    }

    @Test
    public void computeFeatureCounterTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        IComputation<HashMap<String, HashMap<String, Integer>>> computational =
                Computations.of(booleanAssignmentList).map(ComputeFeatureCounter::new);
        HashMap<String, HashMap<String, Integer>> featureCounter = computational.compute();
        System.out.println(featureCounter);
        assertEquals(2, featureCounter.get("A").get("selected"));
        assertEquals(1, featureCounter.get("B").get("selected"));
        assertEquals(0, featureCounter.get("C").get("selected"));
        assertEquals(1, featureCounter.get("D").get("selected"));
        assertEquals(2, featureCounter.get("E").get("selected"));
        assertEquals(1, featureCounter.get("F").get("selected"));
        assertEquals(0, featureCounter.get("G").get("selected"));

        assertEquals(1, featureCounter.get("A").get("deselected"));
        assertEquals(1, featureCounter.get("B").get("deselected"));
        assertEquals(1, featureCounter.get("C").get("deselected"));
        assertEquals(0, featureCounter.get("D").get("deselected"));
        assertEquals(1, featureCounter.get("E").get("deselected"));
        assertEquals(2, featureCounter.get("F").get("deselected"));
        assertEquals(0, featureCounter.get("G").get("deselected"));

        assertEquals(2, featureCounter.get("A").get("undefined"));
        assertEquals(3, featureCounter.get("B").get("undefined"));
        assertEquals(4, featureCounter.get("C").get("undefined"));
        assertEquals(4, featureCounter.get("D").get("undefined"));
        assertEquals(2, featureCounter.get("E").get("undefined"));
        assertEquals(2, featureCounter.get("F").get("undefined"));
        assertEquals(5, featureCounter.get("G").get("undefined"));
    }

    @Test
    public void computeNumberConfigurationTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        IComputation<Integer> computational =
                Computations.of(booleanAssignmentList).map(ComputeNumberConfigurations::new);
        assertEquals(5, computational.compute());
    }

    @Test
    public void computeNumberVariablesTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        IComputation<Integer> computational =
                Computations.of(booleanAssignmentList).map(ComputeNumberVariables::new);
        assertEquals(7, computational.compute());
    }
    
    @Test
    public void computeUniformity() {
    	FeatJAR.initialize();
    	/*  
        if (Files.exists(Paths.get("../formula/src/testFixtures/resources/Automotive02_V1"))) {
            System.out.println("The file exists.");
        } else {
            System.out.println("The file does not exist.");
        }
        
        Result<IFeatureModel> featureModelFormatResult = Result.empty();
        featureModelFormatResult.of(IO.load(Paths.get("../formula/src/testFixtures/resources/Automotive02_V1")
        		, FeatureModelFormats.getInstance()).get());
        System.out.println(featureModelFormatResult.get().getNumberOfConstraints());*/
    	IComputation<HashMap<String, Float>> computation = Computations.of(IO.load(Paths.get("../formula/src/testFixtures/resources/testFeatureModels/basic.xml"),
		FeatureModelFormats.getInstance()).get()).map(ComputeUniformity::new).set(ComputeUniformity.BOOLEAN_ASSIGMENT_LIST, createAssignmentListUniformity());
    	
    	//IComputation<HashMap<String, Float>> computation = Computations.of(()).map(ComputeUniformity::new);
    	HashMap<String, Float> result = computation.compute(); 
    	System.out.println(result);
    }
    
    private IFeatureTree generateMediumTree() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        IFeatureTree treeRoot =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("ConfigDB"));

        IFeature featureAPI = featureModel.mutate().addFeature("API");
        IFeatureTree treeAPI = treeRoot.mutate().addFeatureBelow(featureAPI);
        treeAPI.isMandatory();
        IFeature featureGet = featureModel.mutate().addFeature("Get");
        treeAPI.mutate().addFeatureBelow(featureGet);
        IFeature featurePut = featureModel.mutate().addFeature("Put");
        treeAPI.mutate().addFeatureBelow(featurePut);
        IFeature featureDelete = featureModel.mutate().addFeature("Delete");
        treeAPI.mutate().addFeatureBelow(featureDelete);
        treeAPI.mutate().toOrGroup();

        IFeature featureOS = featureModel.mutate().addFeature("OS");
        IFeatureTree treeOS = treeRoot.mutate().addFeatureBelow(featureOS);
        treeOS.isMandatory();
        IFeature featureWindows = featureModel.mutate().addFeature("Windows");
        treeOS.mutate().addFeatureBelow(featureWindows);
        IFeature featureLinux = featureModel.mutate().addFeature("Linux");
        treeOS.mutate().addFeatureBelow(featureLinux);
        treeOS.mutate().toAlternativeGroup();

        IFeature featureTransactions = featureModel.mutate().addFeature("Transactions");
        IFeatureTree treeTransactions = treeRoot.mutate().addFeatureBelow(featureTransactions);
        treeTransactions.isOptional();

        return treeRoot;
    }
}
