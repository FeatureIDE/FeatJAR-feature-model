/*
 * Copyright (C) 2024 FeatJAR-Development-Team
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
package de.featjar.feature.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.IIdentifier;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.data.identifier.UUIDIdentifier;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.TreePrinter;
import de.featjar.formula.structure.Expressions;
//import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.formula.IFormula;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeatureModel.IMutableFeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureTree;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FeatureModel}, its elements, and its mixins.
 * Updated to include MoveFeature tests.
 */
public class FeatureModelTest {
    IFeatureModel featureModel;

    @BeforeEach
    public void createFeatureModel() {
        featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
    }

    private static class TestableFeatureModel extends FeatureModel {
        private boolean shouldDeleteRootFeatureFlag = true;

        public TestableFeatureModel(IIdentifier iIdentifier) {
            super(iIdentifier);
        }
        
        public void setShouldDeleteRootFeatureFlag(boolean flag) {
            this.shouldDeleteRootFeatureFlag = flag;
        }

        @Override
        protected boolean shouldDeleteRootFeature(IFeature feature) {
            return this.shouldDeleteRootFeatureFlag;
        }

        
        @Override
        public boolean deleteFeatureAndPromoteChildren(IIdentifier featureId) {
            // Retrieve the feature to be deleted; exit if not found
            Result<IFeature> maybeFeatureToDelete = getFeature(featureId);
            if (maybeFeatureToDelete.isEmpty()) {
                return false; // Handle case where the feature doesn't exist
            }

            IFeature featureToDelete = maybeFeatureToDelete.get();
            // Retrieve the feature's tree; exit if it does not exist
            Result<IFeatureTree> maybeFeatureTree = featureToDelete.getFeatureTree();
            if (maybeFeatureTree.isEmpty()) {
                return false; // Handle case where the feature has no associated tree (safety check)
            }

            IFeatureTree featureTree = maybeFeatureTree.get();
            IFeatureTree parentTree = featureTree.getParent().orElse(null);

            // If the feature is a root, handle it differently
            if (featureTree.isRoot()) {
                // Remove the root feature from the model
                featureTreeRoots.remove(featureTree);

                // Promote one of the children to root if available
                List<IFeatureTree> children = new ArrayList<>(featureTree.getChildren());
                if (!children.isEmpty()) {
                    IFeatureTree childToPromote = children.remove(0);
                    featureTreeRoots.add(childToPromote);
                    for (IFeatureTree child : children) {
                        childToPromote.mutate().addChild(child);
                    }
                }
            } else {
                // If the feature is not a root, remove it from its parent tree
                if (parentTree != null) {
                    parentTree.mutate().removeChild(featureTree);
                    // Promote all children of the deleted feature to the same level as the deleted feature
                    for (IFeatureTree child : new ArrayList<>(featureTree.getChildren())) {
                        parentTree.mutate().addChild(child);
                    }
                }
            }
            return true; // Return true if feature is deleted
        }

    }

    @Test
    public void featureModel() {
        Assertions.assertEquals("1", featureModel.getIdentifier().toString());
        assertTrue(featureModel.getRoots().isEmpty());
        assertTrue(featureModel.getFeatures().isEmpty());
        assertTrue(featureModel.getConstraints().isEmpty());
    }

    @Test
    public void commonAttributesMixin() {
        Assertions.assertEquals("@1", featureModel.getName().get());
        Assertions.assertEquals(Result.empty(), featureModel.getDescription());
        featureModel.mutate().setName("My Model");
        featureModel.mutate().setDescription("awesome description");
        Assertions.assertEquals(Result.of("My Model"), featureModel.getName());
        Assertions.assertEquals(Result.of("awesome description"), featureModel.getDescription());
    }

    @Test
    public void featureModelConstraintMixin() {
        Assertions.assertEquals(0, featureModel.getNumberOfConstraints());
        IConstraint constraint1 = featureModel.mutate().addConstraint(Expressions.True);
        IConstraint constraint2 = featureModel.mutate().addConstraint(Expressions.True);
        IConstraint constraint3 = featureModel.mutate().addConstraint(Expressions.False);
        Assertions.assertEquals(3, featureModel.getNumberOfConstraints());
        Assertions.assertEquals(Result.of(constraint1), featureModel.getConstraint(constraint1.getIdentifier()));
        Assertions.assertTrue(featureModel.hasConstraint(constraint2.getIdentifier()));
        constraint2.mutate().remove();
        Assertions.assertFalse(featureModel.hasConstraint(constraint2.getIdentifier()));
        Assertions.assertTrue(featureModel.hasConstraint(constraint3));
    }

    @Test
    public void featureModelFeatureTreeMixin() {
        IFeature rootFeature = featureModel.mutate().addFeature("root");
        Assertions.assertEquals(1, featureModel.getNumberOfFeatures());
        IFeatureTree rootTree = featureModel.mutate().addFeatureTreeRoot(rootFeature);
        IFeature childFeature = featureModel.mutate().addFeature("child1");
        final IFeatureTree childTree = rootTree.mutate().addFeatureBelow(childFeature);
        assertSame(childFeature, childTree.getFeature());
        assertSame(childTree, childFeature.getFeatureTree().get());
        assertSame(childFeature, childTree.getFeature());
        assertSame(rootFeature, childTree.getParent().get().getFeature());
        assertSame(childTree.getParent().get(), rootFeature.getFeatureTree().get());
        assertSame(featureModel.getFeature(childFeature.getIdentifier()).get(), childFeature);
        Assertions.assertEquals(2, featureModel.getNumberOfFeatures());
        Assertions.assertEquals(Result.of(childFeature), featureModel.getFeature(childFeature.getIdentifier()));
        Assertions.assertTrue(featureModel.hasFeature(childFeature.getIdentifier()));
        Assertions.assertTrue(featureModel.getFeature("root2").isEmpty());
        rootFeature.mutate().setName("root2");
        Assertions.assertEquals(Result.of(rootFeature), featureModel.getFeature("root2"));
        assertEquals(List.of(childTree), rootFeature.getFeatureTree().get().getChildren());
        assertEquals(rootFeature.getFeatureTree(), childTree.getParent());
        childTree.mutate().removeFromTree();
        assertEquals(List.of(), rootFeature.getFeatureTree().get().getChildren());
    }

    
    @Test
    public void testDeleteChildFeatureAndPromoteChildren() {
        // Create and set up the first feature model
        featureModel = new TestableFeatureModel(Identifiers.newCounterIdentifier());
        TestableFeatureModel testableFeatureModel = (TestableFeatureModel) featureModel;

        IFeature rootFeature = testableFeatureModel.mutate().addFeature("root");
        IFeature childFeature1 = testableFeatureModel.mutate().addFeature("child1");
        IFeature childFeature2 = testableFeatureModel.mutate().addFeature("child2");
        IFeature childFeature3 = testableFeatureModel.mutate().addFeature("child3");
        IFeature childFeature4 = testableFeatureModel.mutate().addFeature("child4");

        IFeatureTree featureTree = testableFeatureModel.mutate().addFeatureTreeRoot(rootFeature);
        rootFeature.getFeatureTree().get().mutate().addFeatureBelow(childFeature1);
        rootFeature.getFeatureTree().get().mutate().addFeatureBelow(childFeature2);
        rootFeature.getFeatureTree().get().mutate().addFeatureBelow(childFeature3);
        rootFeature.getFeatureTree().get().mutate().addFeatureBelow(childFeature4);

        IFeature childFeature1a = testableFeatureModel.mutate().addFeature("a");
        IFeature childFeature1b = testableFeatureModel.mutate().addFeature("b");
        IFeature childFeature2c = testableFeatureModel.mutate().addFeature("c");
        IFeature childFeature3d = testableFeatureModel.mutate().addFeature("d");
        IFeature childFeature3e = testableFeatureModel.mutate().addFeature("e");
        IFeature childFeature4f = testableFeatureModel.mutate().addFeature("f");

        childFeature1.getFeatureTree().get().mutate().addFeatureBelow(childFeature1a);
        childFeature1.getFeatureTree().get().mutate().addFeatureBelow(childFeature1b);
        childFeature2.getFeatureTree().get().mutate().addFeatureBelow(childFeature2c);
        childFeature3.getFeatureTree().get().mutate().addFeatureBelow(childFeature3d);
        childFeature3.getFeatureTree().get().mutate().addFeatureBelow(childFeature3e);
        childFeature4.getFeatureTree().get().mutate().addFeatureBelow(childFeature4f);

        // Print initial feature model structure
        System.out.println("Feature model before deleting child4:");
        printFeatureModel(testableFeatureModel);

        // Delete childFeature4 and promote its children
        testableFeatureModel.deleteFeatureAndPromoteChildren(childFeature4.getIdentifier());

        // Print feature model structure after deletion
        System.out.println("Feature model after deleting child4:");
        printFeatureModel(testableFeatureModel);

        // Verify the root's children after deletion
        List<String> expectedRootChildrenNames = Arrays.asList("child1", "child2", "child3", "f");
        List<String> actualRootChildrenNames = rootFeature.getFeatureTree().get().getChildren().stream()
                .map(child -> child.getFeature().getName().orElse(""))
                .collect(Collectors.toList());
        Assertions.assertEquals(expectedRootChildrenNames, actualRootChildrenNames, "root should have correct children");

        // Verify that child4's children have been promoted correctly
        Assertions.assertTrue(rootFeature.getFeatureTree().get().getChildren().stream()
                .anyMatch(child -> child.getFeature().getName().orElse("").equals("f")), "root should have childFeature4f after deleting child4");

        // Create and set up the second feature model for comparison
        TestableFeatureModel testableFeatureModel2 = new TestableFeatureModel(Identifiers.newCounterIdentifier());

        IFeature rootFeature2 = testableFeatureModel2.mutate().addFeature("root");
        IFeature childFeature12 = testableFeatureModel2.mutate().addFeature("child1");
        IFeature childFeature22 = testableFeatureModel2.mutate().addFeature("child2");
        IFeature childFeature32 = testableFeatureModel2.mutate().addFeature("child3");
        IFeature childFeature42 = testableFeatureModel2.mutate().addFeature("child4");

        IFeatureTree featureTree2 = testableFeatureModel2.mutate().addFeatureTreeRoot(rootFeature2);
        rootFeature2.getFeatureTree().get().mutate().addFeatureBelow(childFeature12);
        rootFeature2.getFeatureTree().get().mutate().addFeatureBelow(childFeature22);
        rootFeature2.getFeatureTree().get().mutate().addFeatureBelow(childFeature32);

        IFeature childFeature1a2 = testableFeatureModel2.mutate().addFeature("a");
        IFeature childFeature1b2 = testableFeatureModel2.mutate().addFeature("b");
        IFeature childFeature2c2 = testableFeatureModel2.mutate().addFeature("c");
        IFeature childFeature3d2 = testableFeatureModel2.mutate().addFeature("d");
        IFeature childFeature3e2 = testableFeatureModel2.mutate().addFeature("e");
        IFeature childFeature4f2 = testableFeatureModel2.mutate().addFeature("f");

        childFeature12.getFeatureTree().get().mutate().addFeatureBelow(childFeature1a2);
        childFeature12.getFeatureTree().get().mutate().addFeatureBelow(childFeature1b2);
        childFeature22.getFeatureTree().get().mutate().addFeatureBelow(childFeature2c2);
        childFeature32.getFeatureTree().get().mutate().addFeatureBelow(childFeature3d2);
        childFeature32.getFeatureTree().get().mutate().addFeatureBelow(childFeature3e2);
        rootFeature2.getFeatureTree().get().mutate().addFeatureBelow(childFeature4f2);

        // Compare the structure of the first and second feature models
        assertFeatureModelStructureEquals(testableFeatureModel2, testableFeatureModel);
    }

    private void printFeatureModel(TestableFeatureModel model) {
        for (IFeatureTree root : model.getRoots()) {
            printTree(root, "");
        }
    }

//    private void printTree(IFeatureTree node, String prefix) {
//        System.out.println(prefix + node.getFeature().getName().orElse("Unnamed Feature"));
//        for (IFeatureTree child : node.getChildren()) {
//            printTree(child, prefix + "  ");
//        }
//    }

    private void assertFeatureModelStructureEquals(TestableFeatureModel expected, TestableFeatureModel actual) {
        assertFeatureTreeEquals(expected.getRoots(), actual.getRoots());
    }

    private void assertFeatureTreeEquals(List<IFeatureTree> expected, List<IFeatureTree> actual) {
        Assertions.assertEquals(expected.size(), actual.size(), "Number of roots should be the same");

        for (int i = 0; i < expected.size(); i++) {
            assertFeatureTreeEquals(expected.get(i), actual.get(i));
        }
    }

    private void assertFeatureTreeEquals(IFeatureTree expected, IFeatureTree actual) {
        Assertions.assertEquals(expected.getFeature().getName().orElse(""), actual.getFeature().getName().orElse(""), "Feature names should be the same");
        Assertions.assertEquals(expected.getChildren().size(), actual.getChildren().size(), "Number of children should be the same");

        for (int i = 0; i < expected.getChildren().size(); i++) {
            assertFeatureTreeEquals(expected.getChildren().get(i), actual.getChildren().get(i));
        }
    }



    

    @Test
    public void testRootFeatureAndChildrenRemainIntact() {
        // Create and set up the feature model
        featureModel = new TestableFeatureModel(Identifiers.newCounterIdentifier());
        TestableFeatureModel testableFeatureModel = (TestableFeatureModel) featureModel;

        IFeature rootFeature = testableFeatureModel.mutate().addFeature("root");
        IFeature childFeature1 = testableFeatureModel.mutate().addFeature("child1");
        IFeature childFeature2 = testableFeatureModel.mutate().addFeature("child2");
        IFeature childFeature3 = testableFeatureModel.mutate().addFeature("child3");
        IFeature childFeature4 = testableFeatureModel.mutate().addFeature("child4");

        IFeatureTree rootTree = testableFeatureModel.mutate().addFeatureTreeRoot(rootFeature);
        rootTree.mutate().addFeatureBelow(childFeature1);
        rootTree.mutate().addFeatureBelow(childFeature2);
        rootTree.mutate().addFeatureBelow(childFeature3);
        rootTree.mutate().addFeatureBelow(childFeature4);

        IFeature childFeature1a = testableFeatureModel.mutate().addFeature("a");
        IFeature childFeature1b = testableFeatureModel.mutate().addFeature("b");
        IFeature childFeature2c = testableFeatureModel.mutate().addFeature("c");
        IFeature childFeature3d = testableFeatureModel.mutate().addFeature("d");
        IFeature childFeature3e = testableFeatureModel.mutate().addFeature("e");
        IFeature childFeature4f = testableFeatureModel.mutate().addFeature("f");

        childFeature1.getFeatureTree().get().mutate().addFeatureBelow(childFeature1a);
        childFeature1.getFeatureTree().get().mutate().addFeatureBelow(childFeature1b);
        childFeature2.getFeatureTree().get().mutate().addFeatureBelow(childFeature2c);
        childFeature3.getFeatureTree().get().mutate().addFeatureBelow(childFeature3d);
        childFeature3.getFeatureTree().get().mutate().addFeatureBelow(childFeature3e);
        childFeature4.getFeatureTree().get().mutate().addFeatureBelow(childFeature4f);

        // Print initial feature model structure
        System.out.println("Feature model before checking integrity:");
        printFeatureModelStructure(testableFeatureModel);

        // Ensure the root feature and its children are intact
        List<String> expectedRootChildrenNames = Arrays.asList("child1", "child2", "child3", "child4");
        List<String> actualRootChildrenNames = rootFeature.getFeatureTree().get().getChildren().stream()
                .map(child -> child.getFeature().getName().orElse(""))
                .collect(Collectors.toList());
        Assertions.assertEquals(expectedRootChildrenNames, actualRootChildrenNames, "root should have correct children");

        // Verify the children count
        List<IFeatureTree> actualChildren = new ArrayList<>(rootFeature.getFeatureTree().get().getChildren());
        Assertions.assertEquals(4, actualChildren.size(), "Number of children should be 4");

        // Verify the hierarchy of children under root
        Assertions.assertTrue(actualChildren.contains(childFeature1.getFeatureTree().get()), "Root should have childFeature1");
        Assertions.assertTrue(actualChildren.contains(childFeature2.getFeatureTree().get()), "Root should have childFeature2");
        Assertions.assertTrue(actualChildren.contains(childFeature3.getFeatureTree().get()), "Root should have childFeature3");
        Assertions.assertTrue(actualChildren.contains(childFeature4.getFeatureTree().get()), "Root should have childFeature4");

        // Verify that the internal children are correctly maintained
        Assertions.assertTrue(childFeature1.getFeatureTree().get().getChildren().contains(childFeature1a.getFeatureTree().get()), "child1 should have childFeature1a");
        Assertions.assertTrue(childFeature1.getFeatureTree().get().getChildren().contains(childFeature1b.getFeatureTree().get()), "child1 should have childFeature1b");
        Assertions.assertTrue(childFeature2.getFeatureTree().get().getChildren().contains(childFeature2c.getFeatureTree().get()), "child2 should have childFeature2c");
        Assertions.assertTrue(childFeature3.getFeatureTree().get().getChildren().contains(childFeature3d.getFeatureTree().get()), "child3 should have childFeature3d");
        Assertions.assertTrue(childFeature3.getFeatureTree().get().getChildren().contains(childFeature3e.getFeatureTree().get()), "child3 should have childFeature3e");
        Assertions.assertTrue(childFeature4.getFeatureTree().get().getChildren().contains(childFeature4f.getFeatureTree().get()), "child4 should have childFeature4f");

        // Print feature model structure after checking integrity
        System.out.println("Feature model after checking integrity:");
        printFeatureModelStructure(testableFeatureModel);
    }

    private void printFeatureModelStructure(TestableFeatureModel model) {
        for (IFeatureTree root : model.getRoots()) {
            printTreeStructure(root, "");
        }
    }

    private void printTreeStructure(IFeatureTree node, String prefix) {
        System.out.println(prefix + node.getFeature().getName().orElse("Unnamed Feature"));
        for (IFeatureTree child : node.getChildren()) {
            printTreeStructure(child, prefix + "  ");
        }
    }




    
    
    private IFeatureTree root;
    private IFeatureTree childNode1;
    private IFeatureTree childNode2;
    private IFeatureTree gcNode1;
    private IFeatureTree gcNode2;
    private IFeatureTree gcNode3;
    private IFeatureTree gcNode4;
    private IFeatureTree gcNode5;
    private IFeatureTree gcNode6;
    private IFeatureTree gcNode7;
    private IFeatureTree gcNode8;
    private IFeatureTree gcNode9;
    private IFeatureTree gcNode10;

    @BeforeEach
    public void setUp() {
        FeatureModel featureModel = new FeatureModel();

        // Initialize the root feature and its tree node
        IFeature rootFeature = featureModel.mutate().addFeature("Root");
        root = featureModel.mutate().addFeatureTreeRoot(rootFeature);

        // Add child1 and its grandchildren (gc1, gc2, gc3)
        IFeature child1 = featureModel.mutate().addFeature("Child1");
        childNode1 = root.mutate().addFeatureBelow(child1);

        IFeature gc1 = featureModel.mutate().addFeature("GC1");
        gcNode1 = childNode1.mutate().addFeatureBelow(gc1);

        IFeature gc2 = featureModel.mutate().addFeature("GC2");
        gcNode2 = childNode1.mutate().addFeatureBelow(gc2);

        IFeature gc3 = featureModel.mutate().addFeature("GC3");
        gcNode3 = childNode1.mutate().addFeatureBelow(gc3);

        // Add child2 and its grandchildren (gc4, gc5, gc6, gc7, gc8, gc9, gc10)
        IFeature child2 = featureModel.mutate().addFeature("Child2");
        childNode2 = root.mutate().addFeatureBelow(child2);

        IFeature gc4 = featureModel.mutate().addFeature("GC4");
        gcNode4 = childNode2.mutate().addFeatureBelow(gc4);

        IFeature gc5 = featureModel.mutate().addFeature("GC5");
        gcNode5 = childNode2.mutate().addFeatureBelow(gc5);

        IFeature gc6 = featureModel.mutate().addFeature("GC6");
        gcNode6 = childNode2.mutate().addFeatureBelow(gc6);

        IFeature gc7 = featureModel.mutate().addFeature("GC7");
        gcNode7 = gcNode4.mutate().addFeatureBelow(gc7);

        IFeature gc8 = featureModel.mutate().addFeature("GC8");
        gcNode8 = gcNode4.mutate().addFeatureBelow(gc8);

        IFeature gc9 = featureModel.mutate().addFeature("GC9");
        gcNode9 = gcNode7.mutate().addFeatureBelow(gc9);

        IFeature gc10 = featureModel.mutate().addFeature("GC10");
        gcNode10 = gcNode7.mutate().addFeatureBelow(gc10);

        // Print initial setup to confirm
        System.out.println("Tree structure after setup:");
        printTree(root, "");
    }

    @AfterEach
    public void tearDown() {
        System.out.println("Tree structure after test:");
        printTree(root, "");
    }

    private void printTree(IFeatureTree node, String indent) {
    	System.out.println(Trees.traverse(node, new  TreePrinter()));
    }

    @Test
    public void testMoveChildBelowGrandchildShouldFail() {
        
        
        System.out.println("Initial tree structure:");
        printTree(root, "");

        assertEquals("Child1", childNode1.getFeature().getName().orElse(""));
        assertEquals("GC1", gcNode1.getFeature().getName().orElse(""));
        assertTrue(childNode1.hasChild(gcNode1));
        assertTrue(root.hasChild(childNode1));

        // Attempt to move a node below one of its own descendants
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            childNode1.mutate().moveNode(gcNode1);
        });
        String expectedMessage1 = "Cannot move a node below itself or one of its own descendants.";
        String actualMessage1 = exception1.getMessage();
        assertTrue(actualMessage1.contains(expectedMessage1));

        System.out.println("\nTree structure after attempting invalid move:");
        printTree(root, "");

        assertTrue(childNode1.hasChild(gcNode1));
        assertTrue(root.hasChild(childNode1));

        // Attempt to move a node below itself
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            childNode1.mutate().moveNode(childNode1);
        });
        String expectedMessage2 = "Cannot move a node below itself or one of its own descendants.";
        String actualMessage2 = exception2.getMessage();
        assertTrue(actualMessage2.contains(expectedMessage2));

        System.out.println("\nTree structure after attempting invalid addition:");
        printTree(root, "");

        assertTrue(root.hasChild(childNode1));
        assertTrue(childNode1.hasChild(gcNode1));
    }

    
    @Test
    public void testMoveChild1AboveItself() {
        System.out.println("Initial tree structure:");
        printTree(root, "");

        // Ensure the feature name is correct before the move
        assertEquals("Child1", childNode1.getFeature().getName().orElse(""), "Feature name of childNode1 should be 'Child1'");

        // Ensure childNode1 is a child of root
        assertTrue(root.hasChild(childNode1), "Root should contain Child1 before move");

        System.out.println("\nAttempting to move Child1 above itself:");
        
        // This should throw an IllegalArgumentException because a node cannot be moved above itself
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            childNode1.mutate().addFeatureAbove(childNode1.getFeature());
        });

        // Ensure the correct exception message
        String expectedMessage = "Cannot move a node above itself.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage), "Exception message should indicate that moving a node above itself is not allowed");

        System.out.println("\nTree structure after attempting to move Child1 above itself:");
        printTree(root, "");

        // Ensure the tree structure is unchanged
        assertTrue(root.hasChild(childNode1), "Root should still have Child1 as a child after the move attempt");
        assertEquals("Child1", childNode1.getFeature().getName().orElse(""), "The name of Child1 should remain unchanged after the move attempt");
    }


    

    @Test
    public void testSwapGrandchildren() {
        System.out.println("Initial tree structure:");
        printTree(root, "");

        // Perform the swap operation using the swap method from the IFeatureTree interface
        gcNode1.mutate().swap(gcNode4);

        System.out.println("\nTree structure after swapping GC1 and GC4:");
        printTree(root, "");

        // Verify the swap
        assertEquals("GC4", childNode1.getChildren().get(0).getFeature().getName().orElse(""));
        assertEquals("GC1", childNode2.getChildren().get(0).getFeature().getName().orElse(""));
    }

    
    
    @Test
    public void testMoveGrandchild7ToChild2() {
        System.out.println("Initial tree structure:");
        printTree(root, "");

        System.out.println("\nMoving GC7 from below GC4 to below Child2:");
        gcNode7.mutate().moveNode(childNode2);

        System.out.println("\nTree structure after moving GC7:");
        printTree(root, "");

        
        // Re-fetch the children list to avoid potential stale data
        List<IFeatureTree> gcNode4Children = new ArrayList<>(gcNode4.getChildren());
        List<IFeatureTree> childNode2Children = new ArrayList<>(childNode2.getChildren());

        
        // Assert that GC4 no longer has GC7 as a child
        assertFalse(gcNode4Children.contains(gcNode7), "GC4 should no longer have GC7 as a child.");
        // Assert that Child2 now has GC7 as a child
        assertTrue(childNode2Children.contains(gcNode7), "Child2 should now have GC7 as a child.");
    }



    @Test
    public void testMoveGrandchild8ToChild2() {
        System.out.println("Initial tree structure:");
        printTree(root, "");

        System.out.println("\nMoving GC8 from below GC4 to below Child2:");
        gcNode8.mutate().moveNode(childNode2);

        System.out.println("\nTree structure after moving GC8:");
        printTree(root, "");

//        
        // Re-fetch the children list to avoid potential stale data
        List<IFeatureTree> gcNode4Children = new ArrayList<>(gcNode4.getChildren());
        List<IFeatureTree> childNode2Children = new ArrayList<>(childNode2.getChildren());

//        
        // Assert that GC4 no longer has GC8 as a child
        assertFalse(gcNode4Children.contains(gcNode8), "GC4 should no longer have GC8 as a child.");
        // Assert that Child2 now has GC8 as a child
        assertTrue(childNode2Children.contains(gcNode8), "Child2 should now have GC8 as a child.");
    }


    
    @Test
    public void testMoveGrandchild7ToRoot() {
        System.out.println("Initial tree structure:");
        printTree(root, "");

        System.out.println("\nMoving GC7 from below GC4 to below Root:");
        gcNode7.mutate().moveNode(root);

        System.out.println("\nTree structure after moving GC7:");
        printTree(root, "");

        // Verify the move
        assertFalse(gcNode4.getChildren().contains(gcNode7), "GC4 should no longer have GC7 as a child.");
        assertTrue(root.getChildren().contains(gcNode7), "Root should now have GC7 as a child.");

        // Additional debug information
        System.out.println("Name of moved node: " + gcNode7.getFeature().getName().orElse("Unknown"));
        assertEquals("GC7", gcNode7.getFeature().getName().orElse("Unknown"), "The name of GC7 should remain unchanged.");
    }


    @Test
    public void testMoveGrandchild8ToRoot() {
        System.out.println("Initial tree structure:");
        printTree(root, "");

        System.out.println("\nMoving GC8 from below GC4 to below Root:");
        gcNode8.mutate().moveNode(root);

        System.out.println("\nTree structure after moving GC8:");
        printTree(root, "");

        // Verify the move
        assertFalse(gcNode4.getChildren().contains(gcNode8), "GC4 should no longer have GC8 as a child.");
        assertTrue(root.getChildren().contains(gcNode8), "Root should now have GC8 as a child.");

        // Additional debug information
        System.out.println("Name of moved node: " + gcNode8.getFeature().getName().orElse("Unknown"));
        assertEquals("GC8", gcNode8.getFeature().getName().orElse("Unknown"), "The name of GC8 should remain unchanged.");
    }


    @Test
    public void testMoveGrandchild5ToRoot() {
        System.out.println("Initial tree structure:");
        printTree(root, "");

        System.out.println("\nMoving GC5 from below Child2 to below Root:");
        gcNode5.mutate().moveNode(root);

        System.out.println("\nTree structure after moving GC5:");
        printTree(root, "");

        // Verify the move
        assertFalse(childNode2.getChildren().contains(gcNode5), "Child2 should no longer have GC5 as a child.");
        assertTrue(root.getChildren().contains(gcNode5), "Root should now have GC5 as a child.");

        // Additional debug information
        System.out.println("Name of moved node: " + gcNode5.getFeature().getName().orElse("Unknown"));
        assertEquals("GC5", gcNode5.getFeature().getName().orElse("Unknown"), "The name of GC5 should remain unchanged.");

        // Verify the integrity of other nodes
        assertTrue(gcNode4.getChildren().contains(gcNode7), "GC4 should still have GC7 as a child.");
        assertTrue(gcNode4.getChildren().contains(gcNode8), "GC4 should still have GC8 as a child.");
        assertTrue(gcNode7.getChildren().contains(gcNode9), "GC7 should still have GC9 as a child.");
        assertTrue(gcNode7.getChildren().contains(gcNode10), "GC7 should still have GC10 as a child.");
    }


    private void printTreeForTestMoveGrandchild5ToRoot(IFeatureTree node, String indent) {
        System.out.println(indent + node.getFeature().getName().orElse(""));
        for (IFeatureTree child : node.getChildren()) {
            printTreeForTestMoveGrandchild5ToRoot(child, indent + "  ");
        }
    }


    
}


    


