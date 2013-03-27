package fi.vm.sade.generic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schemacrawler.schema.TableType;
import schemacrawler.schemacrawler.InclusionRule;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaInfoLevel;
import schemacrawler.tools.executable.BaseExecutable;
import schemacrawler.tools.integration.graph.GraphExecutable;

import javax.sql.DataSource;
import java.util.Collections;

/**
 * Documents current database schema to an ER diagram.
 */
public class SchemaDiagramGeneratorExecutable {
    private final Logger log = LoggerFactory.getLogger(SchemaDiagramGeneratorExecutable.class);
    private DataSource dataSource;

    public void init() {
        if (dataSource == null) {
            throw new RuntimeException("dataSource is not set");
        }

        try {
            // settings
            final SchemaCrawlerOptions options = new SchemaCrawlerOptions();
            options.setTableTypes(Collections.singletonList(TableType.table));  // only tables
            options.setColumnInclusionRule(InclusionRule.INCLUDE_ALL);          // all columns
            options.setTableInclusionRule(InclusionRule.INCLUDE_ALL);           // all tables
            final SchemaInfoLevel schemaInfo = SchemaInfoLevel.detailed();
            schemaInfo.setRetrieveDatabaseInfo(true);                           // database engine info
            schemaInfo.setRetrieveJdbcDriverInfo(false);                        // do not fetch, url contains invalid characters
            schemaInfo.setRetrieveRoutineInformation(false);                    // do not fetch, driver does not support
            schemaInfo.setRetrieveAdditionalTableAttributes(true);              // TODO also table comments
            schemaInfo.setRetrieveAdditionalColumnAttributes(true);             // TODO also column comments
            options.setSchemaInfoLevel(schemaInfo);
            options.setRoutineTypes(Collections.EMPTY_LIST);                    // see above about routines

            // TODO fix me! does not append comments from database meta data into the exported diagram
            /*
            final InformationSchemaViews schemaViews = new InformationSchemaViews();
            schemaViews.setAdditionalColumnAttributesSql("select null as TABLE_CATALOG, " +
                    "nspname as TABLE_SCHEMA, " +
                    "relname as TABLE_NAME, obj_description(cl.oid) as TABLE_DESCRIPTION " +
                    "from pg_class cl inner join pg_namespace ns on cl.relnamespace = ns.oid " +
                    "WHERE relkind = 'r' and nspname = 'public'");
            options.setInformationSchemaViews(schemaViews);
             */

            // execute crawling and transformation
            final BaseExecutable executable = new GraphExecutable();
            executable.setSchemaCrawlerOptions(options);
            executable.execute(dataSource.getConnection());
        } catch (final Exception e) {
            log.error("Could not generate database schema diagram: [{}]", e.toString());
        }
    }

    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
}