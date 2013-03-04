package fi.vm.sade.dbunit.listener;

import fi.vm.sade.dbunit.annotation.DataSetLocation;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.operation.TransactionOperation;
import org.hibernate.internal.SessionImpl;
import org.springframework.core.io.Resource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.persistence.EntityManager;
import java.sql.Connection;

public class JTACleanInsertTestExecutionListener extends TransactionalTestExecutionListener {

    public void beforeTestMethod(TestContext testContext) throws Exception {
        super.beforeTestMethod(testContext);

        // location of the data set
        String dataSetResourcePath = null;

        // first, the annotation on the test class
        DataSetLocation dsLocation = testContext.getTestInstance().getClass().getAnnotation(DataSetLocation.class);

        if (dsLocation != null) {
            // found the annotation
            dataSetResourcePath = dsLocation.value();
        }

        if (dataSetResourcePath != null) {
            Resource dataSetResource = testContext.getApplicationContext().getResource(dataSetResourcePath);
            FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
            flatXmlDataSetBuilder.setColumnSensing(true);
            IDataSet dataSet = flatXmlDataSetBuilder.build(dataSetResource.getInputStream());
            ReplacementDataSet replacementDataSet = new ReplacementDataSet(dataSet);
            replacementDataSet.addReplacementObject("[NULL]", null);

            LocalContainerEntityManagerFactoryBean emf = testContext.getApplicationContext().getBean(
                    org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.class);

            EntityManager entityManager = (EntityManager) emf.getObject().createEntityManager();

            // entityManager.getTransaction().begin();
            SessionImpl session = (SessionImpl) entityManager.getDelegate();
            Connection jdbcConn = session.connection();
            IDatabaseConnection con = new DatabaseConnection(jdbcConn);
//            dataSet = new FilteredDataSet(new DatabaseSequenceFilter(con), dataSet);
//            new TransactionOperation(DatabaseOperation.DELETE_ALL).execute(con, dataSet);
            new TransactionOperation(DatabaseOperation.CLEAN_INSERT).execute(con,
                    new FilteredDataSet(new DatabaseSequenceFilter(con),replacementDataSet));
            // entityManager.getTransaction().commit();
            con.close();
        }
    }
}
