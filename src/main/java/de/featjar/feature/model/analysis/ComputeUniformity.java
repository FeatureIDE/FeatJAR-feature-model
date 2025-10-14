package de.featjar.feature.model.analysis;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.transformer.ComputeFormula;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;
import de.featjar.feature.model.IFeatureModel;
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
        BooleanAssignmentList booleanAssigmenAssignmentList = BOOLEAN_ASSIGMENT_LIST.get(dependencyList);
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
        fmMap.put("all", solutionsCount.floatValue());
        
        for (String varName : new VariableMap(fmFormula).getVariableNames()) {
        	Reference currentFormula = new Reference(new And((IFormula)fmFormula.getChildren().get(0), new Literal(varName)));
        	currentFormula.setFreeVariables(((Reference)fmFormula).getFreeVariables());
        	System.out.println(currentFormula.print());
        	IFormula NNFFormula = Computations.of((IFormula)currentFormula).map(ComputeNNFFormula::new).compute();
        	fmMap.put(varName, Computations.of(NNFFormula)
            		.map(ComputeCNFFormula::new).map(ComputeSolutionCount::new).compute().floatValue());
        	
        }
        return Result.of(fmMap);
    }
}