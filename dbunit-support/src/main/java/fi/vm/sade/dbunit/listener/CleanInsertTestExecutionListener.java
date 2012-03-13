/**
 * 
 */
package fi.vm.sade.dbunit.listener;

import java.sql.Connection;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import fi.vm.sade.dbunit.annotation.DataSetLocation;

/**
 * Spring framework transactional test extension for JUnit4. Cleans the database for DBUnit tests
 * and inserts data set defined in {@link DataSetLocation} annotation.
 * 
 * @author tommiha
 *
 */
public class CleanInsertTestExecutionListener extends
		TransactionalTestExecutionListener {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	/* (non-Javadoc)
	 * @see org.springframework.test.context.TestExecutionListener#beforeTestMethod(org.springframework.test.context.TestContext)
	 */
	public void beforeTestMethod(TestContext testContext) throws Exception {
		super.beforeTestMethod(testContext);
		
		// location of the data set
		String dataSetResourcePath = null;
	
		// first, the annotation on the test class
		DataSetLocation dsLocation = testContext.getTestInstance().getClass()
				.getAnnotation(DataSetLocation.class);
		
		if (dsLocation != null) {
			// found the annotation
			dataSetResourcePath = dsLocation.value();
			log.info("Annotated test, using data set: " + dataSetResourcePath);
		}
	
		if (dataSetResourcePath != null) {
			Resource dataSetResource = testContext.getApplicationContext()
					.getResource(dataSetResourcePath);
			IDataSet dataSet = new FlatXmlDataSetBuilder().build(dataSetResource.getInputStream());
			DataSource dataSource = testContext.getApplicationContext().getBean(DataSource.class);
			Connection conn = DataSourceUtils.getConnection(dataSource);
			IDatabaseConnection dbConn = new DatabaseConnection(conn);
			try {
				DatabaseOperation.CLEAN_INSERT.execute(dbConn, dataSet);
				log.info("Performed data set initialization.");
			} finally {
				DataSourceUtils.releaseConnection(conn, dataSource);
			}
		} else {
			log.info(testContext.getClass().getName() + " does not have any data set, no data injection.");
		}
	}
}
