package com.test;

import com.emreh.creator.CreateSimpleReport;
import com.emreh.enums.ExportType;
import com.emreh.model.ColumnModelDetails;
import com.test.model.ChildModel;
import com.test.model.ParentModel;
import com.test.model.SubChildModel;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    static final List<ColumnModelDetails> modelDetailLists = new ArrayList<ColumnModelDetails>() {
        private static final long serialVersionUID = 2278804983970104997L;

        {
            add(new ColumnModelDetails("Parent Name", "nameParent", true, true, -1));
            add(new ColumnModelDetails("Parent last Name", "lastNameParent", true));
            add(new ColumnModelDetails("child Model", "childModelList", false));
            add(new ColumnModelDetails("Child Name", "childModelList.childName", true, true, -1));
            add(new ColumnModelDetails("child Last Name", "childModelList.childLastName", true));
            add(new ColumnModelDetails("childModel_ Sub Child Model", "childModelList.subChildModelList", true));
            add(new ColumnModelDetails("childModel_ Sub Child Model _ sub Child Name", "childModelList.subChildModelList.subChildName", true, false, -1));
            add(new ColumnModelDetails("childModel_ Sub Child Model _ sub Child Last Name", "childModelList.subChildModelList.subChildLastName", true));
        }
    };

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
