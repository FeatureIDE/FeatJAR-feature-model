package de.featjar.feature.model.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.io.json.JSONAnalysisFormat;
import de.featjar.feature.model.io.yaml.YAMLAnalysisFormat;

public class YAMLExportTest {
	
	LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
	
	public AnalysisTree<?> createDefaultTree() {
        AnalysisTree<?> innereanalysisTree = new AnalysisTree<>(
                "avgNumOfAtomsPerConstraints",
                new AnalysisTree<>("xo", 3.3),
                new AnalysisTree<>("numOfLeafFeatures", (float) 12.4));

        AnalysisTree<?> analysisTree = new AnalysisTree<>(
                "Analysis",
                new AnalysisTree<>("numOfLeafFeatures", (float) 12.4),
                new AnalysisTree<>("numOfTopFeatures", 3.3),
                new AnalysisTree<>("treeDepth", 3),
                new AnalysisTree<>("avgNumOfChildren", 3),
                new AnalysisTree<>("numInOrGroups", 7),
                new AnalysisTree<>("numInAltGroups", 5),
                new AnalysisTree<>("numOfAtoms", 8),
                new AnalysisTree<>("avgNumOfAsss", 4),
                innereanalysisTree);
        return analysisTree;
    }
	
	@Test
	public void YAMLTest() throws IOException{
    	AnalysisTree<?> analysisTree = createDefaultTree();
        IO.save(analysisTree, Paths.get("filename.yaml"), new YAMLAnalysisFormat());
        AnalysisTree<?> outputAnalysisTree =
                IO.load(Paths.get("filename.yaml"), new YAMLAnalysisFormat()).get();
        analysisTree.sort();
        outputAnalysisTree.sort();
        assertTrue(
                Trees.equals(analysisTree, outputAnalysisTree),
                "firstTree\n" + analysisTree.print() + "\nsecond tree\n" + outputAnalysisTree.print());
	}
	
	@Test
	public void JSONSerialize() throws IOException {
        LinkedHashMap<String, Object> innerMap = new LinkedHashMap<String, Object>();
        innerMap.put("xo", 3.3);
        innerMap.put("numOfLeafFeatures", (float) 12.4);
        data.put("numOfTopFeatures", 3.3);
        data.put("numOfLeafFeatures", (float) 12.4);
        data.put("treeDepth", 3);
        data.put("avgNumOfChildren", 3);
        data.put("numInOrGroups", 7);
        data.put("numInAltGroups", 5);
        data.put("avgNumOfAtomsPerConstraints", innerMap);
        data.put("numOfAtoms", 8);
        data.put("avgNumOfAsss", 4);

        AnalysisTree<?> analsyisTree = createDefaultTree();
        YAMLAnalysisFormat yamlFormat = new YAMLAnalysisFormat();
        Yaml yaml = new Yaml();
        System.out.println("yamlFormat.serialize(analsyisTree).get() : \n" + yamlFormat.serialize(analsyisTree).get());
        String yamlFromserializerDumpString = yaml.dump(yamlFormat.serialize(analsyisTree).get());
        System.out.println(yamlFromserializerDumpString);
        
        AInputMapper inputMapper = AInputMapper.of(Paths.get("filename.yaml"), IO.DEFAULT_CHARSET);        
        
        Yaml yaml1 = new Yaml();
    	HashMap<String, Object> yamlHashMap = (HashMap<String, Object>) yaml1.load(inputMapper.get().text());
    	inputMapper = AInputMapper.of(Paths.get("filename.yaml"), IO.DEFAULT_CHARSET); 
    	Yaml yaml2 = new Yaml();
    	
    	String yamlLoadFromDump = yaml2.load(yamlFromserializerDumpString);
    	String yamlFromFileInputMapper = inputMapper.get().text();
    	System.out.println("yamlLoadFromDump: \n" + yamlLoadFromDump.getClass());
    	System.out.println("yamlFromFileInputMapper: \n" + yamlFromFileInputMapper.getClass());
    	System.out.println("yamlFromFileInputMapper: \n" + yamlFromFileInputMapper);
    	System.out.println("yamlLoadFromDump: \n" + yamlLoadFromDump);
    	System.out.println("yamlFromserializerDumpString: \n" + yamlFromserializerDumpString);
    	
    	System.out.println("yamlFromFileInputMapper 1: \n" + yamlFromFileInputMapper.toString());
    	System.out.println("yamlLoadFromDump 1: \n" + yamlLoadFromDump.toString());
    	System.out.println("yamlFromserializerDumpString 1: \n" + yamlFromserializerDumpString);
    	
    	yaml2.load(yamlFromserializerDumpString);
    	AnalysisTree<?> analsyisTreeAfterConversion = Result.of(AnalysisTree.hashMapListYamlToTree(yamlHashMap)).get();
        

        analsyisTree.sort();
        analsyisTreeAfterConversion.sort();
        assertTrue(
                Trees.equals(analsyisTree, analsyisTreeAfterConversion),
                "firstTree\n" + analsyisTree.print() + "\nsecond tree\n" + analsyisTreeAfterConversion.print());
        AnalysisTree<?> manualAnalysisTree = createDefaultTree();
        manualAnalysisTree.sort();
        assertTrue(
                Trees.equals(manualAnalysisTree, analsyisTreeAfterConversion),
                "firstTree\n" + manualAnalysisTree.print() + "\nsecond tree\n" + analsyisTreeAfterConversion.print());
    }
	
}
