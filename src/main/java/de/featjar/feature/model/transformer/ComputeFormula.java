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
package de.featjar.feature.model.transformer;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Pair;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.structure.ParentIterator;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.base.tree.visitor.ITreeVisitor.TraversalAction;
import de.featjar.base.tree.visitor.PostOrderVisitor;
import de.featjar.feature.model.FeatureTree.Group;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.constraints.IAttributeAggregate;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.AtLeast;
import de.featjar.formula.structure.connective.AtMost;
import de.featjar.formula.structure.connective.Between;
import de.featjar.formula.structure.connective.Choose;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.True;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Transforms a feature model into a boolean formula. Supports a simple way of
 * transforming cardinality features and a more complicated transformation.
 *
 * @author Klara Surmeier
 * @author Nermine Mansour
 * @author Malena Horstmann
 * @author Sebastian Krieter
 */
public class ComputeFormula extends AComputation<IFormula> {
    protected static final Dependency<IFeatureModel> FEATURE_MODEL = Dependency.newDependency(IFeatureModel.class);

    private FeatureToFormula featureToFormula;

    public ComputeFormula(IComputation<IFeatureModel> featureModel) {
        super(featureModel);
    }

    protected ComputeFormula(ComputeFormula other) {
        super(other);
    }

    @Override
    public Result<IFormula> compute(List<Object> dependencyList, Progress progress) {
        IFeatureModel featureModel = FEATURE_MODEL.get(dependencyList);
        ArrayList<IFormula> constraints = new ArrayList<>();

        featureToFormula = new FeatureToFormula();

        Collection<IConstraint> crossTreeConstr = featureModel.getConstraints();
        Map<ConstraintContexts, List<IConstraint>> contextsToConstraints = findContextsToConstraints(crossTreeConstr);

        createTreeConstraints(featureModel, constraints);

        List<IFormula> crossTreeConstraints;

        for (IConstraint constraint : featureModel.getConstraints()) {
            List<IFeatureTree> cardinalityNodes = constraint
                    .getFormula()
                    .getVariableStream()
                    .map(variable -> featureModel
                            .getPseudoRoot()
                            .preOrderStream()
                            .skip(1)
                            .filter(f -> f.getFeature().getName().valueEquals(variable.getName()))
                            .findFirst()
                            .orElseThrow())
                    .flatMap(node -> StreamSupport.stream(new ParentIterator<>(node, true), false))
                    .filter(node -> node.getFeatureCardinalityUpperBound() > 1)
                    .distinct()
                    .collect(Collectors.toList());

            int[] reptitionMax = new int[cardinalityNodes.size()];
            int[] reptitionIndex = new int[reptitionMax.length];
            int index = 0;
            for (IFeatureTree node : cardinalityNodes) {
                reptitionMax[index] = node.getFeatureCardinalityUpperBound();
                reptitionIndex[index] = 1;
                index++;
            }
            for (int i = 0; i < reptitionIndex.length; i++) {}

            Collection<String> namesPerFeature = featureToFormula.getNamesPerFeature(variableName);
            Trees.traverse(constraint.getFormula(), visitor);
        }

        List<IConstraint> transformedConstraints = createContextualCloneConstraints(contextsToConstraints);
        // add transformed cross-tree-constraints to all constraints
        for (IConstraint constr : transformedConstraints) {
            constraints.add(constr.getFormula());
        }
        // extract global constraints to transform them to one big one
        List<IConstraint> globalConstraints = getGlobalConstraints(contextsToConstraints);
        List<IConstraint> existenceConstraints = createGlobalExistenceConstraints(globalConstraints);
        for (IConstraint constr : existenceConstraints) {
            constraints.add(constr.getFormula());
        }

        PostOrderVisitor<IFormula> visitor = new PostOrderVisitor<IFormula>(path -> {
            final IExpression expression = ITreeVisitor.getCurrentNode(path);

            if (expression instanceof IAttributeAggregate) {
                final Result<IFormula> parent = ITreeVisitor.getParentNode(path);
                if (parent.isEmpty()) {
                    return TraversalAction.FAIL;
                }
                Result<IExpression> result =
                        ((IAttributeAggregate) expression).translate(featureModel.getFeatures(), featureToFormula);
                if (result.isEmpty()) {
                    return TraversalAction.FAIL;
                }
                parent.get().replaceChild(expression, result.get());
            }
            return TraversalAction.CONTINUE;
        });
        for (IFormula constraint : crossTreeConstraints) {
            Trees.traverse(constraint, visitor);
        }
        constraints.addAll(crossTreeConstraints);

        return Result.of(new Reference(new And(constraints), featureToFormula.getVariables()));
    }

    /**
     * Creates tree constraints for the feature tree under every root node.
     * @param featureModel for which the constraints are generated
     * @param constraints list of constraints to which the generated constraints are added
     * @param variables
     */
    private void createTreeConstraints(IFeatureModel featureModel, ArrayList<IFormula> constraints) {

        addChildConstraints(featureModel.getPseudoRoot(), constraints, null, new ArrayDeque<>());
    }

    /**
     * Recursively traverses a feature tree with cardinality features and adds the
     * tree constraints for every node.
     * @param parentNode from which to start the traversal
     * @param constraints list of constraints to which the generated constraints are added
     */
    private void addChildConstraints(
            IFeatureTree parentNode,
            ArrayList<IFormula> constraints,
            String parentName,
            ArrayDeque<String> cardinalityNames) {

        IFormula parentLiteral = parentName == null ? True.INSTANCE : featureToFormula.getFeatureFormula2(parentName);

        String cardinalityPrefix =
                cardinalityNames.isEmpty() ? null : cardinalityNames.stream().collect(Collectors.joining("."));

        for (IFeatureTree child : parentNode.getChildren()) {
            int upperBound = child.getFeatureCardinalityUpperBound();
            int lowerBound = child.getFeatureCardinalityLowerBound();
            IFeature feature = child.getFeature();

            String featureName = feature.getName().orElse("???");
            if (cardinalityPrefix != null) {
                featureName = cardinalityPrefix + "." + featureName;
            }

            if (upperBound > 1) {
                IFormula previousLiteral = null;
                for (int i = 1; i <= upperBound; i++) {
                    String featureNameInstance = featureName + "_" + i;

                    IFormula currentLiteral = featureToFormula.getOrCreateFeatureFormula(feature, featureNameInstance);
                    constraints.add(new Implies(currentLiteral, parentLiteral));
                    if (previousLiteral != null) {
                        constraints.add(new Implies(currentLiteral, previousLiteral));
                    }
                    previousLiteral = currentLiteral;

                    cardinalityNames.add(feature.getName().orElse("???") + "_" + i);
                    addChildConstraints(child, constraints, featureNameInstance, cardinalityNames);
                    cardinalityNames.removeLast();
                }
                if (lowerBound > 0) {
                    constraints.add(new Implies(
                            parentLiteral, featureToFormula.getFeatureFormula2(featureName + "_" + lowerBound)));
                }
            } else {
                String featureNameInstance = featureName;

                IFormula currentLiteral = featureToFormula.getOrCreateFeatureFormula(feature, featureNameInstance);
                constraints.add(new Implies(currentLiteral, parentLiteral));
                addChildConstraints(child, constraints, featureNameInstance, cardinalityNames);

                if (lowerBound > 0) {
                    constraints.add(new Implies(parentLiteral, currentLiteral));
                }
            }
        }

        handleGroups(parentLiteral, parentNode, cardinalityPrefix, constraints);
    }

    /**
     * Adds group constraints (or, alternative, cardinality) for a given node.
     *
     * @param parentLiteral literal name of the node
     * @param parentNode
     * @param constraints    list of constraints to add generated group constraints
     *                       to
     */
    private void handleGroups(
            IFormula parentLiteral, IFeatureTree parentNode, String prefix, ArrayList<IFormula> constraints) {

        for (Pair<Group, List<IFeatureTree>> featureGroup : parentNode.getGroupedChildren()) {
            Group group = featureGroup.getKey();
            if (group != null && !group.isAnd()) {
                ArrayList<IFormula> groupLiterals =
                        new ArrayList<>(featureGroup.getValue().size());
                for (IFeatureTree childNode : featureGroup.getValue()) {
                    String featureName = childNode.getFeature().getName().orElse("???");
                    if (prefix != null) {
                        featureName = prefix + "." + featureName;
                    }
                    int upperBound = childNode.getFeatureCardinalityUpperBound();
                    if (upperBound > 1) {
                        for (int i = 1; i <= upperBound; i++) {
                            groupLiterals.add(featureToFormula.getFeatureFormula2(featureName + "_" + i));
                        }
                    } else {
                        groupLiterals.add(featureToFormula.getFeatureFormula2(featureName));
                    }
                }

                if (group.isOr()) {
                    constraints.add(new Implies(parentLiteral, new Or(groupLiterals)));
                } else if (group.isAlternative()) {
                    constraints.add(new Implies(parentLiteral, new Choose(1, groupLiterals)));
                } else {
                    int lowerBound = group.getLowerBound();
                    int upperBound = group.getUpperBound();
                    if (lowerBound > 0) {
                        if (upperBound != Range.OPEN) {
                            constraints.add(
                                    new Implies(parentLiteral, new Between(lowerBound, upperBound, groupLiterals)));
                        } else {
                            constraints.add(new Implies(parentLiteral, new AtMost(upperBound, groupLiterals)));
                        }
                    } else {
                        if (upperBound != Range.OPEN) {
                            constraints.add(new Implies(parentLiteral, new AtLeast(lowerBound, groupLiterals)));
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates a mapping from context features to local and global cross-tree
     * constraints to use for the creation of local and global contextual clone
     * constraints and global existence constraints.
     *
     * @param crossTreeConstr
     * @return mapping from context features to constraints
     */
    private Map<ConstraintContexts, List<IConstraint>> findContextsToConstraints(
            Collection<IConstraint> crossTreeConstr) {
        Map<ConstraintContexts, List<IConstraint>> contextsToConstraints = new HashMap<>();

        // iterate through all constraints
        for (IConstraint constraint : crossTreeConstr) {
            // create a list to store the contextFeatures
            Set<IFeatureTree> contextFeatures = new HashSet<>();

            // identify the contained features
            LinkedHashSet<IFeature> features = constraint.getReferencedFeatures();

            // find context feature for every feature
            for (IFeature feature : features) {
                IFeatureTree contextFeature =
                        getNextCardinalityFeature(feature.getFeatureTree().get());
                if (contextFeature != null) {
                    contextFeatures.add(contextFeature);
                }
            }

            // group the constraints according to their contextFeatures
            ConstraintContexts key = new ConstraintContexts(contextFeatures);
            contextsToConstraints.computeIfAbsent(key, k -> new ArrayList<>()).add(constraint);
        }

        return contextsToConstraints;
    }

    /**
     * Creates local and global contextual clone constraints.
     *
     * @param contextsToConstraints mapping from context features to constraints
     * @return list of local and global contextual clone constraints
     */
    private List<IConstraint> createContextualCloneConstraints(
            Map<ConstraintContexts, List<IConstraint>> contextsToConstraints) {

        List<IConstraint> finalConstraints = new ArrayList<>();

        for (Map.Entry<ConstraintContexts, List<IConstraint>> entry : contextsToConstraints.entrySet()) {
            ConstraintContexts contextOriginalFeatures = entry.getKey();
            List<IConstraint> constraints = entry.getValue();

            boolean isGlobal = false;
            Set<IFeatureTree> contexts = contextOriginalFeatures.getContextFeatures();
            List<IFeatureTree> orderedContexts = new ArrayList<>(contexts);

            for (IFeatureTree context : orderedContexts) {

                List<IFeatureTree> contextFeatureNames = featureToCardinalityNames.get(context.getFeature());

                // generates the constraints for every context
                for (IConstraint constraint : constraints) {

                    // checks whether the constraint is local or global
                    if (!hasCommonCardinalityFeature(constraint)) {
                        isGlobal = true;
                    }

                    if (contextFeatureNames == null || contextFeatureNames.isEmpty()) continue;
                    for (IFeatureTree contextFeatureName : contextFeatureNames) {
                        IConstraint modifiedConstraint = constraint.clone();
                        LinkedHashSet<IFeature> features = constraint.getReferencedFeatures();

                        for (IFeature feature : features) {
                            IFeatureTree currentFeatureTree =
                                    feature.getFeatureTree().get();

                            IFormula replacement = null;
                            if (isGlobal) {
                                // 2., 3. and 4. case: feature is not the current context feature
                                IFeatureTree currentCardinalityParent = getNextCardinalityFeature(currentFeatureTree);

                                // checks whether the current feature in the loop is equal to the current context
                                boolean notEqual = false;
                                if (currentCardinalityParent == null) {
                                    notEqual = true;
                                } else if (!contextFeatureName
                                        .getFeature()
                                        .getName()
                                        .get()
                                        .equals(currentCardinalityParent
                                                .getFeature()
                                                .getName()
                                                .get())) {
                                    notEqual = true;
                                }

                                if (notEqual) {
                                    // 4. case: context of feature is located under current context feature
                                    if (contextUnderCurrentContext(contextFeatureName, currentFeatureTree)) {
                                        replacement = createOrReplacementWithContext(contextFeatureName, feature);
                                    } else {
                                        // 2. + 3. case: feature's context is independent from current context
                                        replacement = createOrReplacementWithoutContext(feature);
                                    }
                                    // feature is the current context feature
                                } else {
                                    List<IFeatureTree> contextualFeatureName =
                                            findContextualFeatureNames(contextFeatureName, feature);
                                    if (contextualFeatureName != null && !contextualFeatureName.isEmpty()) {
                                        replacement = featureToFormula.getFeatureFormula(
                                                feature,
                                                contextualFeatureName
                                                        .get(0)
                                                        .getAttributeValue(literalNameAttribute)
                                                        .orElse(contextualFeatureName
                                                                .get(0)
                                                                .getFeature()
                                                                .getName()
                                                                .orElse("")));
                                    } else {
                                        replacement = featureToFormula.getFeatureFormula(feature);
                                    }
                                }
                            } else {
                                List<IFeatureTree> contextualFeatureName =
                                        findContextualFeatureNames(contextFeatureName, feature);
                                if (contextualFeatureName != null && !contextualFeatureName.isEmpty()) {
                                    replacement = featureToFormula.getFeatureFormula(
                                            feature,
                                            contextualFeatureName
                                                    .get(0)
                                                    .getAttributeValue(literalNameAttribute)
                                                    .orElse(contextualFeatureName
                                                            .get(0)
                                                            .getFeature()
                                                            .getName()
                                                            .orElse("")));
                                } else {
                                    replacement = featureToFormula.getFeatureFormula(feature);
                                }
                            }

                            if (replacement != null) {
                                IFormula toReplace = featureToFormula.getFeatureFormula(feature);

                                if (!toReplace.equals(replacement)) {
                                    replaceInTree(modifiedConstraint.getFormula(), toReplace, replacement);
                                }
                            }
                        }

                        IFormula formula = modifiedConstraint.getFormula();
                        IFormula contextFormula = new Implies(
                                featureToFormula.getFeatureFormula(
                                        contextFeatureName.getFeature(),
                                        contextFeatureName
                                                .getAttributeValue(literalNameAttribute)
                                                .orElse("")),
                                formula);
                        modifiedConstraint.mutate().setFormula(contextFormula);

                        finalConstraints.add(modifiedConstraint);
                    }
                }
            }
        }

        return finalConstraints;
    }

    /**
     * Gets the next cardinality feature above the current node.
     *
     * @param node
     * @return next cardinality feature above, in some case node itself.
     */
    private IFeatureTree getNextCardinalityFeature(IFeatureTree node) {

        if (!node.hasParent() && !isCardinalityFeature(node)) return null;

        if (isCardinalityFeature(node)) {
            return node;
        } else if (isCardinalityFeature(node.getParent().get())) {
            return node.getParent().get();
        } else {
            return getNextCardinalityFeature(node.getParent().get());
        }
    }

    /**
     * Checks whether all features in the constraint share the same context, have
     * the same next cardinality feature.
     *
     * @param constraint
     * @return true if all features in the constraint have the same context, false
     *         otherwise
     */
    private boolean hasCommonCardinalityFeature(IConstraint constraint) {

        LinkedHashSet<IFeature> features = constraint.getReferencedFeatures();

        Set<IFeatureTree> contexts = new HashSet<>();

        for (IFeature feature : features) {
            IFeatureTree context =
                    getNextCardinalityFeature(feature.getFeatureTree().get());

            contexts.add(context);
            if (contexts.size() > 1) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether a node is located under a given node.
     *
     * @param currentContext node which holds the current context
     * @param context        node for which the location is checked
     * @return true if the node is located under the given node, false otherwise
     */
    private boolean contextUnderCurrentContext(IFeatureTree currentContext, IFeatureTree context) {
        IFeatureTree node = context;
        while (node != null) {
            if (node.getFeature()
                    .getName()
                    .get()
                    .equals(currentContext.getFeature().getName().get())) return true;
            node = node.getParent().isPresent()
                    ? getNextCardinalityFeature(node.getParent().get())
                    : null;
        }
        return false;
    }

    /**
     * Finds the contextual feature name for a feature in a given context.
     *
     * @param contextFeature
     * @param targetFeature
     * @return list of the feature names in the given context
     */
    private List<IFeatureTree> findContextualFeatureNames(IFeatureTree contextFeature, IFeature targetFeature) {

        List<IFeatureTree> contextualFeatureNames = new ArrayList<>();

        // 1. case: feature is the current context feature
        if (contextFeature
                .getFeature()
                .getName()
                .get()
                .equals(targetFeature.getName().get())) {
            contextualFeatureNames.add(contextFeature);
            return contextualFeatureNames;
        }

        Queue<IFeatureTree> nodesToVisit = new LinkedList<>();
        nodesToVisit.add(contextFeature);

        while (!nodesToVisit.isEmpty()) {
            IFeatureTree currentNode = nodesToVisit.poll();

            Map<IFeature, List<IFeatureTree>> childrenMap = featureToChildren.get(currentNode);
            if (childrenMap == null) {
                continue;
            }

            for (Map.Entry<IFeature, List<IFeatureTree>> entry : childrenMap.entrySet()) {
                IFeature originalChildFeature = entry.getKey();
                List<IFeatureTree> childInstances = entry.getValue();

                for (IFeatureTree childInstance : childInstances) {

                    if (originalChildFeature.equals(targetFeature)) {
                        contextualFeatureNames.add(childInstance);
                    }

                    nodesToVisit.add(childInstance);
                }
            }
        }

        return contextualFeatureNames;
    }

    /**
     * Creates an or replacement for a feature in a given context which is used in
     * the replacement of global constraints.
     *
     * @param currentContext
     * @param feature
     * @return generated or replacement
     */
    private IFormula createOrReplacementWithContext(IFeatureTree currentContext, IFeature feature) {
        List<IFeatureTree> contextualFeatureNames = findContextualFeatureNames(currentContext, feature);
        if (contextualFeatureNames == null || contextualFeatureNames.isEmpty()) {
            return featureToFormula.getFeatureFormula(feature); // fallback to plain feature
        }
        return createOrFromFeatureTrees(contextualFeatureNames);
    }

    /**
     * Creates an or replacement for a feature without a context which is used in
     * the replacement of global constraints.
     *
     * @param feature
     * @return generated or replacement
     */
    private IFormula createOrReplacementWithoutContext(IFeature feature) {
        List<IFeatureTree> featureNames = featureToCardinalityNames.get(feature);
        if (featureNames == null || featureNames.isEmpty() || featureNames.size() == 1) {
            return featureToFormula.getFeatureFormula(feature); // fallback
        }
        return createOrFromFeatureTrees(featureNames);
    }

    /**
     * Creates an or from a given list of nodes.
     *
     * @param featureTrees
     * @return or
     */
    private IFormula createOrFromFeatureTrees(List<IFeatureTree> featureTrees) {

        List<IFormula> featureLiterals = new ArrayList<>();

        for (IFeatureTree featureTree : featureTrees) {
            String literalName =
                    featureTree.getAttributeValue(literalNameAttribute).orElse("");
            ;
            IFormula featureLiteral = featureToFormula.getFeatureFormula(featureTree.getFeature(), literalName);
            featureLiterals.add(featureLiteral);
        }

        return new Or(featureLiterals);
    }

    /**
     * Replaces a feature in a given constraint. Recursively iterates through the
     * children of the constraint in order to find the feature which needs to be
     * replaced.
     *
     * @param currentNode starting formula for the recursion
     * @param toReplace   feature which should be replaced
     * @param replacement
     * @return true if the replacement was successful, false otherwise
     */
    private boolean replaceInTree(IExpression currentNode, IFormula toReplace, IFormula replacement) {
        if (currentNode == null) {
            return false;
        }

        boolean replaced = false;
        try {
            replaced = currentNode.replaceChild(toReplace, replacement);
        } catch (Exception e) {
        }

        if (replaced) {
            return true;
        }

        for (IExpression child : currentNode.getChildren()) {

            if (replaceInTree(child, toReplace, replacement)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates global existence constraints.
     *
     * @param globalConstraints list of global constraints for which the existence
     *                          constraints should be generated
     * @return list of global existence constraints
     */
    private List<IConstraint> createGlobalExistenceConstraints(List<IConstraint> globalConstraints) {
        List<IConstraint> globalExistenceConstraints = new ArrayList<>();

        for (IConstraint constraint : globalConstraints) {
            IConstraint modifiedConstraint = constraint.clone();
            LinkedHashSet<IFeature> features = constraint.getReferencedFeatures();

            for (IFeature feature : features) {
                IFormula replacement = createOrReplacementWithoutContext(feature);
                IFormula toReplace = featureToFormula.getFeatureFormula(feature);
                replaceInTree(modifiedConstraint.getFormula(), toReplace, replacement);
            }

            globalExistenceConstraints.add(modifiedConstraint);
        }

        return globalExistenceConstraints;
    }

    /**
     * Gets global constraints from the mapping from context features to
     * constraints.
     *
     * @param contextsToConstraints mapping from contexts to constraints
     * @return list of global constraints
     */
    private List<IConstraint> getGlobalConstraints(Map<ConstraintContexts, List<IConstraint>> contextsToConstraints) {
        List<IConstraint> globalConstraints = new ArrayList<>();

        for (Map.Entry<ConstraintContexts, List<IConstraint>> entry : contextsToConstraints.entrySet()) {
            List<IConstraint> constraints = entry.getValue();

            for (IConstraint constraint : constraints) {
                if (!hasCommonCardinalityFeature(constraint)) {
                    globalConstraints.add(constraint);
                }
            }
        }

        return globalConstraints;
    }
}
