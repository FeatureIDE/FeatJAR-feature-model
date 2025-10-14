package de.featjar.feature.model.analysis;

import java.util.HashMap;
import java.util.List;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.feature.model.FeatureModel;
import de.featjar.formula.assignment.BooleanAssignmentList;

public class ComputeUniformity extends AComputation<HashMap<String, Float>> {

    protected static final Dependency<BooleanAssignmentList> BOOLEAN_ASSIGMENT_LIST =
            Dependency.newDependency(BooleanAssignmentList.class);
    protected static final Dependency<FeatureModel> FEATURE_MODEL =
            Dependency.newDependency(FeatureModel.class);

    public ComputeUniformity(IComputation<BooleanAssignmentList> booleanAssigmentList, FeatureModel featureModel) {
        super(booleanAssigmentList, featureModel);
    }

    @Override
    public Result<HashMap<String, Float>> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList booleanAssigmenAssignmentList = BOOLEAN_ASSIGMENT_LIST.get(dependencyList);
        FeatureModel featureModel = FEATURE_MODEL.get(dependencyList);
        
        
        
        return Result.empty();
    }
}