package com.test;

import com.sadad.creator.CreateSimpleReport;
import com.sadad.enums.ExportType;
import com.sadad.model.ModelDetails;
import com.test.model.ChildModel;
import com.test.model.ParentModel;
import com.test.model.SubChildModel;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TestReportCreatorTest {

    @Test
    public void test() throws IOException {
        CreateSimpleReport createSimpleReport = new CreateSimpleReport().setDetails(modelDetailLists).setModel(init()).builder();
        { // Excel
            File currDir = new File(".");
            String path = currDir.getAbsolutePath();
            String fileLocation = path.substring(0, path.length() - 1) + "temp.xlsx";

            InputStream in = (InputStream) createSimpleReport.getExportFileReport(ExportType.EXCEL, "tester");
            File targetFile = new File(fileLocation);
            OutputStream outStream = Files.newOutputStream(targetFile.toPath());

            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(outStream);
        }

        { // CSV
            File currDir = new File(".");
            String path = currDir.getAbsolutePath();
            String fileLocation = path.substring(0, path.length() - 1) + "temp.csv";

            InputStream in = (InputStream) createSimpleReport.getExportFileReport(ExportType.CSV, "tester");

            File targetFile = new File(fileLocation);
            OutputStream outStream = Files.newOutputStream(targetFile.toPath());

            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(outStream);
        }
    }

    static final List<ModelDetails> modelDetailLists = new ArrayList<ModelDetails>() {
        private static final long serialVersionUID = 2278804983970104997L;

        {
        add(new ModelDetails("Parent Name", "nameParent", true));
        add(new ModelDetails("Parent last Name", "lastNameParent", true));
        add(new ModelDetails("child Model", "childModelList", false));
        add(new ModelDetails("Child Name", "childModelList.childName", true));
        add(new ModelDetails("child Last Name", "childModelList.childLastName", true));
        add(new ModelDetails("childModel_ Sub Child Model", "childModelList.subChildModelList", true));
        add(new ModelDetails("childModel_ Sub Child Model _ sub Child Name", "childModelList.subChildModelList.subChildName", true));
        add(new ModelDetails("childModel_ Sub Child Model _ sub Child Last Name", "childModelList.subChildModelList.subChildLastName", true));
    }};

    private static List<ParentModel> init() {
        List<ParentModel> parentModelList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            ParentModel parentModel = new ParentModel();

            parentModel.setNameParent("Parent Name " + i);
            parentModel.setLastNameParent("Parent Last Name " + i);

            List<ChildModel> childModelList = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                ChildModel childModel = new ChildModel();

                childModel.setChildName("Child Name " + i + "_" + j);
                childModel.setChildLastName("Child Last Name " + i + "_" + j);

                List<SubChildModel> subChildModelList = new ArrayList<>();
                for (int k = 0; k < 10; k++) {
                    SubChildModel subChildModel = new SubChildModel();

                    subChildModel.setSubChildName("Sub Child Model Name" + i + "_" + j + "_" + k);
                    subChildModel.setSubChildLastName("Sub Child Model Last Name" + i + "_" + j + "_" + k);

                    subChildModelList.add(subChildModel);
                }

                childModel.setSubChildModelList(subChildModelList);
                childModelList.add(childModel);
            }

            parentModel.setChildModelList(childModelList);
            parentModelList.add(parentModel);
        }

        return parentModelList;
    }
}
