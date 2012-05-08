package fi.vm.sade.test.util;

import java.sql.Connection;

import javax.persistence.EntityManager;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.internal.SessionImpl;
import org.springframework.core.io.Resource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import fi.vm.sade.dbunit.annotation.DataSetLocation;

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
            IDataSet dataSet = new FlatXmlDataSetBuilder().build(dataSetResource.getInputStream());

            LocalContainerEntityManagerFactoryBean emf = testContext.getApplicationContext().getBean(
                    org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.class);

            EntityManager entityManager = (EntityManager) emf.getObject().createEntityManager();

            // entityManager.getTransaction().begin();
            SessionImpl session = (SessionImpl) entityManager.getDelegate();
            Connection jdbcConn = session.connection();
            IDatabaseConnection con = new DatabaseConnection(jdbcConn);
            // DatabaseOperation.DELETE_ALL.execute(con, dataSet);
            DatabaseOperation.CLEAN_INSERT.execute(con, dataSet);
            // entityManager.getTransaction().commit();
            con.close();
        }
    }
}
