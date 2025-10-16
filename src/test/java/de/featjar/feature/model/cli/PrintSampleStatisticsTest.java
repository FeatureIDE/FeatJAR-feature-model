package de.featjar.feature.model.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.io.FeatureModelFormats;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.BooleanAssignmentListFormats;

public class PrintSampleStatisticsTest {
	private final String fmPath = "src/test/java/de/featjar/feature/model/cli/resources/DB_FeatureModel.xml";
	private final String configPath = "src/test/java/de/featjar/feature/model/cli/resources/DB_configs.csv";
	PrintSampleStatistics printSampleStats = new PrintSampleStatistics();

	@Test
    void inputTest() {

        int exit_code = FeatJAR.runTest(
                "printSampleStats", "--inputFM", fmPath,"--inputConfig", configPath);
        assertEquals(0, exit_code);
    }

    @Test
    void noInput() {

        assertEquals(1, FeatJAR.runTest("printSampleStats", "--inputFM"));
        assertEquals(1, FeatJAR.runTest("printSampleStats"));
    }
    
    @Test
    void outputWithFileValidExtension() throws IOException {

        int exit_code = FeatJAR.runTest(
                "printSampleStats",
                "--inputFM",
                fmPath,
                "--inputConfig",
                configPath,
                "--output",
                "model.csv");
        assertEquals(0, exit_code);
        assertTrue(Files.exists(Paths.get("model.csv")) && !Files.isDirectory(Paths.get("model.csv")));
        Files.deleteIfExists(Paths.get("model.csv"));
    }

    @Test
    void outputWithFileInvalidExtension() {

        int exit_code = FeatJAR.runTest(
                "printSampleStats",
                "--inputFM",
                fmPath,
                "--inputConfig",
                configPath,
                "--output",
                "model.pdf");
        assertEquals(1, exit_code);
    }

    @Test
    void outputWithoutFileExtension() {

        int exit_code = FeatJAR.runTest(
                "printSampleStats",
                "--inputFM",
                fmPath,
                "--inputConfig",
                configPath,
                "--output",
                "desktop/folder");
        assertEquals(1, exit_code);
    }

    @Test
    void printComparison() {
    	FeatJAR.initialize();
        Result<BooleanAssignmentList> loadedConfig = IO.load(Paths.get(configPath), BooleanAssignmentListFormats.getInstance());
        Result<IFeatureModel> loadedFM = IO.load(Paths.get(fmPath), FeatureModelFormats.getInstance());
        
		IFeatureModel model = (IFeatureModel) loadedFM.orElseThrow();
		BooleanAssignmentList booleanAssignmentList = (BooleanAssignmentList) loadedConfig.orElseThrow();
		
        String content = "{Number of Configurations=6, Number of Variables=9, Distribution of feature selection={selected=24, deselected=20, undefined=10}, Feature counter={ConfigDB_selected=6, ConfigDB_deselected=0, ConfigDB_undefined=0, API_selected=2, API_deselected=1, API_undefined=3, OS_selected=0, OS_deselected=0, OS_undefined=6, Get_selected=6, Get_deselected=0, Get_undefined=0, Put_selected=2, Put_deselected=3, Put_undefined=1, Delete_selected=1, Delete_deselected=5, Delete_undefined=0, Windows_selected=3, Windows_deselected=3, Windows_undefined=0, Linux_selected=2, Linux_deselected=4, Linux_undefined=0, Transactions_selected=2, Transactions_deselected=4, Transactions_undefined=0}, Uniformity={ConfigDB_selected=0.0, ConfigDB_deselected=0.0, ConfigDB_undefined=0.0, API_selected=-0.3333333, API_deselected=0.0, API_undefined=0.33333334, OS_selected=-1.0, OS_deselected=0.0, OS_undefined=1.0, Get_selected=0.46153843, Get_deselected=-0.46153846, Get_undefined=0.0, Put_selected=0.05128205, Put_deselected=-0.3846154, Put_undefined=0.33333334, Delete_selected=-0.2820513, Delete_deselected=0.2820513, Delete_undefined=0.0, Windows_selected=0.16666669, Windows_deselected=-0.16666666, Windows_undefined=0.0, Linux_selected=-0.16666666, Linux_deselected=0.16666669, Linux_undefined=0.0, Transactions_selected=-0.12820512, Transactions_deselected=0.12820512, Transactions_undefined=0.0}}";
        String comparison =
                printSampleStats.collectStats(booleanAssignmentList, model).toString();
        assertEquals(content, comparison);
        FeatJAR.deinitialize();
    }

    @Test
    void prettyStringBuilder() {

    	FeatJAR.initialize();
        Result<BooleanAssignmentList> loadedConfig = IO.load(Paths.get(configPath), BooleanAssignmentListFormats.getInstance());
        Result<IFeatureModel> loadedFM = IO.load(Paths.get(fmPath), FeatureModelFormats.getInstance());
        
		IFeatureModel model = (IFeatureModel) loadedFM.orElseThrow();
		BooleanAssignmentList booleanAssignmentList = (BooleanAssignmentList) loadedConfig.orElseThrow();

        String comparison = 
        		 "\n                CONSTRAINT RELATED STATS\n"
        		+ "                 \n"
        		+ "Number of Configurations                 : 6\n"
        		+ "Number of Variables                      : 9\n"
        		+ "Distribution of feature selection       \n"
        		+ "           selected                      : 24\n"
        		+ "           deselected                    : 20\n"
        		+ "           undefined                     : 10\n"
        		+ "Feature counter                         \n"
        		+ "           ConfigDB_selected             : 6\n"
        		+ "           ConfigDB_deselected           : 0\n"
        		+ "           ConfigDB_undefined            : 0\n"
        		+ "           API_selected                  : 2\n"
        		+ "           API_deselected                : 1\n"
        		+ "           API_undefined                 : 3\n"
        		+ "           OS_selected                   : 0\n"
        		+ "           OS_deselected                 : 0\n"
        		+ "           OS_undefined                  : 6\n"
        		+ "           Get_selected                  : 6\n"
        		+ "           Get_deselected                : 0\n"
        		+ "           Get_undefined                 : 0\n"
        		+ "           Put_selected                  : 2\n"
        		+ "           Put_deselected                : 3\n"
        		+ "           Put_undefined                 : 1\n"
        		+ "           Delete_selected               : 1\n"
        		+ "           Delete_deselected             : 5\n"
        		+ "           Delete_undefined              : 0\n"
        		+ "           Windows_selected              : 3\n"
        		+ "           Windows_deselected            : 3\n"
        		+ "           Windows_undefined             : 0\n"
        		+ "           Linux_selected                : 2\n"
        		+ "           Linux_deselected              : 4\n"
        		+ "           Linux_undefined               : 0\n"
        		+ "           Transactions_selected         : 2\n"
        		+ "           Transactions_deselected       : 4\n"
        		+ "           Transactions_undefined        : 0\n"
        		+ "Uniformity                              \n"
        		+ "           ConfigDB_selected             : 0.0\n"
        		+ "           ConfigDB_deselected           : 0.0\n"
        		+ "           ConfigDB_undefined            : 0.0\n"
        		+ "           API_selected                  : -0.3333333\n"
        		+ "           API_deselected                : 0.0\n"
        		+ "           API_undefined                 : 0.33333334\n"
        		+ "           OS_selected                   : -1.0\n"
        		+ "           OS_deselected                 : 0.0\n"
        		+ "           OS_undefined                  : 1.0\n"
        		+ "           Get_selected                  : 0.46153843\n"
        		+ "           Get_deselected                : -0.46153846\n"
        		+ "           Get_undefined                 : 0.0\n"
        		+ "           Put_selected                  : 0.05128205\n"
        		+ "           Put_deselected                : -0.3846154\n"
        		+ "           Put_undefined                 : 0.33333334\n"
        		+ "           Delete_selected               : -0.2820513\n"
        		+ "           Delete_deselected             : 0.2820513\n"
        		+ "           Delete_undefined              : 0.0\n"
        		+ "           Windows_selected              : 0.16666669\n"
        		+ "           Windows_deselected            : -0.16666666\n"
        		+ "           Windows_undefined             : 0.0\n"
        		+ "           Linux_selected                : -0.16666666\n"
        		+ "           Linux_deselected              : 0.16666669\n"
        		+ "           Linux_undefined               : 0.0\n"
        		+ "           Transactions_selected         : -0.12820512\n"
        		+ "           Transactions_deselected       : 0.12820512\n"
        		+ "           Transactions_undefined        : 0.0\n"
        		+ "";
        
        LinkedHashMap<String, Object> map = printSampleStats.collectStats(booleanAssignmentList, model);
        assertEquals(comparison, printSampleStats.buildStringPrettyStats(map).toString());
        FeatJAR.deinitialize();
    }
}
