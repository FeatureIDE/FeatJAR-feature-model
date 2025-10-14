package de.featjar.feature.model.analysis;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.transformer.ComputeFormula;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.analysis.javasmt.computation.ComputeSatisfiability;
import de.featjar.analysis.javasmt.computation.ComputeSolutionCount;

public class ComputeUniformity extends AComputation<HashMap<String, Float>> {

    protected static final Dependency<BooleanAssignmentList> BOOLEAN_ASSIGMENT_LIST =
            Dependency.newDependency(BooleanAssignmentList.class);
    protected static final Dependency<IFeatureModel> FEATURE_MODEL =
            Dependency.newDependency(IFeatureModel.class);

    public ComputeUniformity(IComputation<IFeatureModel> featureModel) {
        super(Computations.of(new BooleanAssignmentList(new VariableMap())), featureModel);
    }

    @Override
    public Result<HashMap<String, Float>> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList booleanAssignmentList = BOOLEAN_ASSIGMENT_LIST.get(dependencyList);
        IFeatureModel featureModel = FEATURE_MODEL.get(dependencyList);
        IComputation<IFormula> iFormula = Computations.of(featureModel).map(ComputeFormula::new);
        IFormula fmFormula = iFormula.compute();
        //new VariableMap(fmFormula);
        System.out.println(fmFormula.print());
        IComputation<BigInteger> solutionCountComputation = Computations.of(fmFormula).map(ComputeNNFFormula::new)
        		.map(ComputeCNFFormula::new).map(ComputeSolutionCount::new);
        BigInteger solutionsCount = solutionCountComputation.compute();
        HashMap<String, Float> returnMap = new HashMap<String, Float>();
        HashMap<String, Float> fmMap = new HashMap<String, Float>();
        HashMap<String, Float> sampleMap = new HashMap<String, Float>();
        VariableMap fmVariableMap = new VariableMap(fmFormula);
        
        for (String varName : fmVariableMap.getVariableNames()) {
        	fmMap.put(varName, (float) 0);
        	sampleMap.put(varName, (float) 0);
        }
        
        fmMap.put("all", solutionsCount.floatValue());
        
        for (String varName : fmVariableMap.getVariableNames()) {
        	Reference currentFormula = new Reference(new And((IFormula)fmFormula.getChildren().get(0), new Literal(varName)));
        	currentFormula.setFreeVariables(((Reference)fmFormula).getFreeVariables());
        	IFormula NNFFormula = Computations.of((IFormula)currentFormula).map(ComputeNNFFormula::new).compute();
        	fmMap.put(varName, Computations.of(NNFFormula)
            		.map(ComputeCNFFormula::new).map(ComputeSolutionCount::new).compute().floatValue());
        }
        
        int assignmentSolutionsCount = 0;
        for(BooleanAssignment booleanAssignment : booleanAssignmentList.getAll()) {
        	IFormula currentIFormulaAssignment = new And();
        	List<String> currentAssignmentVariables = new LinkedList();
        	for (int index : booleanAssignment.get()) {
        		if(fmVariableMap.get(index).isPresent()) {
            		currentIFormulaAssignment = new And(currentIFormulaAssignment, new Literal(fmVariableMap.get(index).orElseThrow()));
            		currentAssignmentVariables.add(fmVariableMap.get(index).get());
        		}
        	}
        	Reference currentFormula = new Reference(new And((IFormula)fmFormula.getChildren().get(0), currentIFormulaAssignment));
        	System.out.println(fmFormula.print());
        	System.out.println("Assignment: " + booleanAssignment + "\n" + currentFormula.print());
        	System.out.println(Computations.of((IFormula)currentFormula).map(ComputeNNFFormula::new)
                	.map(ComputeCNFFormula::new).map(ComputeSolutionCount::new).compute());
        	currentFormula.setFreeVariables(((Reference)fmFormula).getFreeVariables());
        	
        	if(Computations.of((IFormula)currentFormula).map(ComputeNNFFormula::new)
        	.map(ComputeCNFFormula::new).map(ComputeSolutionCount::new).compute().intValue() > 0) {
        		assignmentSolutionsCount++;
        		for (String key : currentAssignmentVariables) {
        			sampleMap.replace(key, sampleMap.get(key) + 1);
        		}
        	}
        }
        
        System.out.println("sampleMap: \n" + sampleMap);
        System.out.println("assignmentSolutionsCount: " + assignmentSolutionsCount);
        System.out.println("solutionsCount: " + solutionsCount);
        return Result.of(fmMap);
    }
}